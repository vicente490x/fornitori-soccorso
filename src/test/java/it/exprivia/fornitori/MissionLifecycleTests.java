package it.exprivia.fornitori;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.exprivia.fornitori.dto.MissionDtos;
import it.exprivia.fornitori.service.MissionService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/** Punti 6-13 e 17: ciclo della missione, costi, concorrenza e vincoli del database. */
@EnabledIfEnvironmentVariable(named = "FORNITORI_TEST_DATABASE", matches = "isolated")
class MissionLifecycleTests extends IsolatedPostgresTest {
    @Autowired MissionService missions;

    @Test
    void createdMissionIsLiveAndAudited() throws Exception {
        long mission = createMission(login(RESPONSABILE));
        assertThat(jdbc.queryForObject("select stato from missions where id = ? and not is_fixture", String.class, mission))
                .isEqualTo("nuova");
        assertThat(eventCount(mission, "MISSION_CREATED")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "\"tipoIntervento\":\"traino\"",
        "\"canaleOrigine\":\"fax\"",
        "\"clienteId\":99999",
        "\"origineLat\":91",
        "\"origineLat\":1E-100000",
        "\"origineLng\":181",
        "\"kmStimati\":-1",
        "\"descrizioneProblema\":\"\""})
    void invalidMissionIsRejected(String override) throws Exception {
        String field = override.substring(0, override.indexOf(':'));
        String base = "{\"clienteId\":1,\"tipoIntervento\":\"riparazione_sul_posto\",\"descrizioneProblema\":\"x\","
                + "\"origineIndirizzo\":\"Via Roma 1\",\"origineLat\":41.2,\"origineLng\":16.6,\"canaleOrigine\":\"centrale\"}";
        String body = base.replaceFirst(field + ":[^,}]*", java.util.regex.Matcher.quoteReplacement(override));
        if (!body.contains(override)) {
            body = base.replace("}", "," + override + "}");
        }
        perform(login(RESPONSABILE), post("/api/v1/missioni"), body).andExpect(status().isBadRequest());
    }

    @Test
    void proposalUsesConfiguredTimeoutAndRejectsBusyDriver() throws Exception {
        Actor manager = login(RESPONSABILE);
        long first = createMission(manager);
        long second = createMission(manager);
        propose(manager, first, 1);
        assertThat(jdbc.queryForObject("select extract(epoch from scadenza_il - proposta_il) / 60 from missions where id = ?",
                Double.class, first)).isEqualTo(10.0);
        perform(manager, post("/api/v1/missioni/{id}/proposte", second), "{\"autistaId\":1}").andExpect(status().isConflict());
        perform(manager, post("/api/v1/missioni/{id}/proposte", first), "{\"autistaId\":2}").andExpect(status().isConflict());
        perform(manager, get("/api/v1/autisti")).andExpect(jsonPath("$[0].disponibilita").value("occupato"));
    }

    @Test
    void rejectionFreesTheDriverAndKeepsHistoryAndAlerts() throws Exception {
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        perform(login(AUTISTA_1), post("/api/v1/missioni/{id}/reject", mission))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stato").value("non_accettata"));
        propose(manager, mission, 2);
        perform(manager, get("/api/v1/missioni/{id}/proposte", mission))
                .andExpect(jsonPath("$[0].stato").value("non_accettata"))
                .andExpect(jsonPath("$[1].stato").value("proposta"));
        perform(manager, get("/api/v1/centrale/avvisi")).andExpect(jsonPath("$[0].tipo").value("RIFIUTO"));
    }

    @Test
    void expiredProposalCannotBeAcceptedAndCanBeReassigned() throws Exception {
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        expireProposal(mission);
        perform(login(AUTISTA_1), post("/api/v1/missioni/{id}/accept", mission)).andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("select stato from missions where id = ?", String.class, mission)).isEqualTo("scaduta");
        propose(manager, mission, 2);
        perform(login(AUTISTA_2), post("/api/v1/missioni/{id}/accept", mission)).andExpect(status().isOk());
    }

    @Test
    void schedulerExpiresLiveProposalsAndLeavesFixturesAlone() throws Exception {
        String fixturesBefore = fixturesDigest();
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        expireProposal(mission);
        assertThat(missions.expireDueProposals()).isEqualTo(1);
        assertThat(jdbc.queryForObject("select stato from missions where id = ?", String.class, mission)).isEqualTo("scaduta");
        assertThat(fixturesDigest()).isEqualTo(fixturesBefore);
    }

    @Test
    void aMissionWithoutOpenProposalDoesNotBlockOtherExpirations() throws Exception {
        Actor manager = login(RESPONSABILE);
        long broken = createMission(manager);
        long healthy = createMission(manager);
        propose(manager, broken, 1);
        propose(manager, healthy, 2);
        expireProposal(broken);
        expireProposal(healthy);
        jdbc.update("update mission_proposals set stato = 'scaduta', risposta_il = now() where mission_id = ?", broken);
        missions.expireDueProposals();
        assertThat(jdbc.queryForObject("select stato from missions where id = ?", String.class, healthy)).isEqualTo("scaduta");
        assertThat(jdbc.queryForObject("select stato from missions where id = ?", String.class, broken)).isEqualTo("scaduta");
    }

    @Test
    void completionCalculatesCostOnTheServerAndClosesOnce() throws Exception {
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        Actor driver = login(AUTISTA_1);
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":1}").andExpect(status().isConflict());
        perform(driver, post("/api/v1/missioni/{id}/accept", mission)).andExpect(status().isOk());
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":-1}").andExpect(status().isBadRequest());
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":1.234}").andExpect(status().isBadRequest());
        jdbc.update("update missions set accettata_il = (timestamp '2026-11-04 22:00:00' at time zone 'Europe/Rome') where id = ?", mission);
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":18.25,\"costoFinale\":1}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoFinale").value(89.88))
                .andExpect(jsonPath("$.notturno").value(true));
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":1}").andExpect(status().isConflict());
        perform(manager, post("/api/v1/missioni/{id}/proposte", mission), "{\"autistaId\":2}").andExpect(status().isConflict());
        assertThat(jdbc.queryForObject("select count(*) from notifications_outbox where mission_id = ?", Integer.class, mission)).isEqualTo(2);
    }

    @Test
    void configuredNightWindowStartsAtTwentyAndEndsAtEight() {
        assertThat(jdbc.queryForObject("select night_start::text from pricing_config where id = 1", String.class))
                .isEqualTo("20:00:00");
        assertThat(jdbc.queryForObject("select night_end::text from pricing_config where id = 1", String.class))
                .isEqualTo("08:00:00");
    }

    @ParameterizedTest
    @CsvSource({"19:59,false,62.00", "20:00,true,77.50", "21:59,true,77.50", "22:00,true,77.50", "07:59,true,77.50", "08:00,false,62.00"})
    void nightSurchargeFollowsTheAcceptanceTime(String time, boolean night, double cost) throws Exception {
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        Actor driver = login(AUTISTA_1);
        perform(driver, post("/api/v1/missioni/{id}/accept", mission)).andExpect(status().isOk());
        jdbc.update("update missions set accettata_il = (timestamp '2026-11-04 " + time + ":00' at time zone 'Europe/Rome') where id = ?", mission);
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":10}")
                .andExpect(jsonPath("$.notturno").value(night)).andExpect(jsonPath("$.costoFinale").value(cost));
    }

    @Test
    void costCorrectionKeepsCalculatedValueAndAudit() throws Exception {
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        Actor driver = login(AUTISTA_1);
        perform(driver, post("/api/v1/missioni/{id}/accept", mission)).andExpect(status().isOk());
        jdbc.update("update missions set accettata_il = (timestamp '2026-11-04 12:00:00' at time zone 'Europe/Rome') where id = ?", mission);
        perform(driver, post("/api/v1/missioni/{id}/complete", mission), "{\"kmEffettivi\":10}").andExpect(status().isOk());
        perform(manager, patch("/api/v1/missioni/{id}/cost", mission), "{\"costoFinale\":50,\"motivo\":\"x\"}").andExpect(status().isBadRequest());
        perform(manager, patch("/api/v1/missioni/{id}/cost", mission), "{\"costoFinale\":50,\"motivo\":\"abbuono\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costoCalcolato").value(62.00))
                .andExpect(jsonPath("$.costoFinale").value(50));
        assertThat(eventCount(mission, "MISSION_COST_ADJUSTED")).isEqualTo(1);
        assertThat(jdbc.queryForObject("select rettifica_utente_id is not null from missions where id = ?", Boolean.class, mission)).isTrue();
        perform(driver, get("/api/v1/autista/storico")).andExpect(jsonPath("$.totaleEur").value(50));
    }

    @Test
    void fixturesCannotBeChanged() throws Exception {
        Actor manager = login(RESPONSABILE);
        long fixture = fixtureMission();
        String before = fixturesDigest();
        perform(manager, post("/api/v1/missioni/{id}/proposte", fixture), "{\"autistaId\":4}").andExpect(status().isNotFound());
        perform(manager, patch("/api/v1/missioni/{id}/cost", fixture), "{\"costoFinale\":1,\"motivo\":\"abc\"}").andExpect(status().isNotFound());
        perform(manager, post("/api/v1/missioni/{id}/notes", fixture), "{\"testo\":\"x\"}").andExpect(status().isConflict());
        assertThat(fixturesDigest()).isEqualTo(before);
        assertThat(jdbc.queryForObject("select count(*) from mission_notes where mission_id = ?", Integer.class, fixture)).isZero();
    }

    @Test
    void aDriverCannotReceiveTwoProposalsConcurrently() throws Exception {
        Actor manager = login(RESPONSABILE);
        long first = createMission(manager);
        long second = createMission(manager);
        List<Boolean> outcomes = race(
                () -> missions.propose(RESPONSABILE, first, new MissionDtos.ProposeMissionRequest(5L)),
                () -> missions.propose(RESPONSABILE, second, new MissionDtos.ProposeMissionRequest(5L)));
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(jdbc.queryForObject("select count(*) from missions where driver_id = 5 and not is_fixture and stato = 'proposta'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void aMissionCannotBeProposedToTwoDriversConcurrently() throws Exception {
        long mission = createMission(login(RESPONSABILE));
        List<Boolean> outcomes = race(
                () -> missions.propose(RESPONSABILE, mission, new MissionDtos.ProposeMissionRequest(5L)),
                () -> missions.propose(RESPONSABILE, mission, new MissionDtos.ProposeMissionRequest(6L)));
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(jdbc.queryForObject("select count(*) from mission_proposals where mission_id = ? and stato = 'proposta'",
                Integer.class, mission)).isEqualTo(1);
    }

    @Test
    void concurrentAcceptancesProduceOneTransition() throws Exception {
        Actor manager = login(RESPONSABILE);
        long mission = createMission(manager);
        propose(manager, mission, 1);
        List<Boolean> outcomes = race(() -> missions.accept(AUTISTA_1, mission), () -> missions.accept(AUTISTA_1, mission));
        assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        assertThat(jdbc.queryForObject("select count(*) from notifications_outbox where mission_id = ?", Integer.class, mission)).isEqualTo(1);
        assertThat(eventCount(mission, "MISSION_ACCEPTED")).isEqualTo(1);
    }

    @Test
    void databaseRejectsInconsistentStatesEvenWithoutTheService() throws Exception {
        Actor manager = login(RESPONSABILE);
        long first = createMission(manager);
        long second = createMission(manager);
        propose(manager, first, 1);
        assertThatThrownBy(() -> jdbc.update("update missions set driver_id = 1, stato = 'proposta' where id = ?", second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update missions set stato = 'inventato' where id = ?", second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update missions set customer_id = 999999 where id = ?", second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update missions set origine_lat = 41, origine_lng = null where id = ?", second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update missions set stato = 'eseguita' where id = ?", second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("update missions set stato = 'proposta', driver_id = null where id = ?", second))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("insert into notifications_outbox(event_key, mission_id, channel, destinatario, payload) "
                + "values ('x', ?, 'sms', 'y', '{}')", second)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private int eventCount(long mission, String type) {
        return jdbc.queryForObject("select count(*) from mission_events where mission_id = ? and event_type = ?",
                Integer.class, mission, type);
    }

    private String fixturesDigest() {
        return jdbc.queryForObject("select md5(string_agg(id || stato || coalesce(driver_id::text, '') || "
                + "coalesce(costo_finale::text, ''), ',' order by id)) from missions where is_fixture", String.class);
    }

    /** Lancia le due operazioni insieme, in transazioni reali: true se è riuscita, false se è stata respinta. */
    private List<Boolean> race(Callable<?> first, Callable<?> second) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Callable<?> task : List.of(first, second)) {
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        task.call();
                        return true;
                    } catch (RuntimeException refused) {
                        return false;
                    }
                }));
            }
            start.countDown();
            List<Boolean> outcomes = new ArrayList<>();
            for (Future<Boolean> future : futures) {
                outcomes.add(future.get());
            }
            return outcomes;
        }
    }
}
