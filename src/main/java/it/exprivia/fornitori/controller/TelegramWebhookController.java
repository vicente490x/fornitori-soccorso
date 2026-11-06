package it.exprivia.fornitori.controller;

import it.exprivia.fornitori.common.MissionNotFoundException;
import it.exprivia.fornitori.dto.ChatDtos;
import it.exprivia.fornitori.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * Telegram webhook demo: il bot non usa polling né processi permanenti.
 * Lo stato del dialogo e la coda delle risposte sono in PostgreSQL (migrazione 13).
 * Unicamente il database demo e l'ambiente Preview devono abilitare il webhook.
 */
@Profile("demo")
@RestController
@RequestMapping("/api/v1/telegram")
public class TelegramWebhookController {
    private static final Logger LOG = LoggerFactory.getLogger(TelegramWebhookController.class);
    private static final String PUBLIC_URL = "https://fornitori-soccorso-demo.vercel.app";
    private static final Pattern PLATE = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{5,10}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 ]{5,19}$");
    private static final String WELCOME = "Ciao, sono Soccorso Stradale Bot. Per attivare un soccorso scrivi la targa del veicolo; per conoscere lo stato scrivi STATO.";
    private static final String UNAVAILABLE = "Il servizio non è disponibile in questo momento. Riprova tra poco.";
    private static final String[] PROBLEMS = {
        "RUOTA_CON_SCORTA", "RUOTA_SENZA_SCORTA", "BATTERIA_SCARICA",
        "GUASTO_MOTORE", "INCIDENTE"
    };
    private static final String[] PROBLEM_LABELS = {
        "Ruota forata con ruota di scorta", "Ruota forata senza ruota di scorta",
        "Batteria scarica", "Guasto motore", "Incidente"
    };

    private final JdbcTemplate db;
    private final TransactionTemplate tx;
    private final ChatService chat;
    private final RestClient telegram;
    private final String botToken;
    private final byte[] expectedSecret;

    public TelegramWebhookController(JdbcTemplate db, TransactionTemplate tx, ChatService chat,
            @Value("${TELEGRAM_BOT_TOKEN:}") String botToken) {
        this.db = db;
        this.tx = tx;
        this.chat = chat;
        this.botToken = botToken;
        this.expectedSecret = tokenSecret(botToken).getBytes(StandardCharsets.UTF_8);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(8));
        this.telegram = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + botToken)
                .requestFactory(factory).build();
    }

    /** Registrazione soltanto sulla Preview main: una Production non deve rubare il webhook. */
    @EventListener(ApplicationReadyEvent.class)
    public void registerPreviewWebhook() {
        if (!"preview".equals(System.getenv("VERCEL_ENV"))
                || !"main".equals(System.getenv("VERCEL_GIT_COMMIT_REF"))
                || botToken.isBlank()) {
            return;
        }
        try {
            telegram.post().uri("/setWebhook").body(Map.of(
                    "url", PUBLIC_URL + "/api/v1/telegram/webhook",
                    "secret_token", new String(expectedSecret, StandardCharsets.UTF_8),
                    "allowed_updates", List.of("message", "callback_query"),
                    "drop_pending_updates", false,
                    "max_connections", 1
            )).retrieve().toBodilessEntity();
            LOG.info("Webhook Telegram registrato sulla Preview main");
        } catch (RuntimeException ex) {
            // Non impedire l'avvio del sito per un problema temporaneo di Telegram.
            LOG.warn("Webhook Telegram non registrato: {}", ex.getClass().getSimpleName());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receive(
            @RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
            @RequestBody Map<String, Object> update) {
        if (botToken.isBlank() || secret == null || !MessageDigest.isEqual(
                expectedSecret, secret.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        long updateId = number(update.get("update_id"));
        if (updateId < 0) return ResponseEntity.badRequest().build();
        Map<String, Object> callback = map(update.get("callback_query"));
        Map<String, Object> message = callback != null ? map(callback.get("message")) : map(update.get("message"));
        if (message == null) return ResponseEntity.ok().build();
        Map<String, Object> fromChat = map(message.get("chat"));
        if (fromChat == null || !"private".equals(fromChat.get("type"))) return ResponseEntity.ok().build();
        long chatNumericId = number(fromChat.get("id"));
        if (chatNumericId <= 0) return ResponseEntity.ok().build();
        String chatId = Long.toString(chatNumericId);
        String text = callback != null ? Objects.toString(callback.get("data"), "")
                : Objects.toString(message.get("text"), "");
        Map<String, Object> location = map(message.get("location"));
        Double lat = location == null ? null : decimal(location.get("latitude"));
        Double lng = location == null ? null : decimal(location.get("longitude"));
        try {
            tx.executeWithoutResult(ignored -> process(chatId, updateId, text, lat, lng));
            // Telegram ritenta l'update dopo 503: la deduplicazione è sul database.
            flushReplies(chatId);
            if (callback != null && callback.get("id") instanceof String id) {
                telegram.post().uri("/answerCallbackQuery").body(Map.of("callback_query_id", id))
                        .retrieve().toBodilessEntity();
            }
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            LOG.warn("Errore webhook Telegram: {}", ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    private void process(String chatId, long updateId, String text, Double lat, Double lng) {
        db.update("INSERT INTO telegram_conversations(chat_id,session_id) VALUES (?,?) ON CONFLICT DO NOTHING",
                chatId, UUID.randomUUID().toString().replace("-", ""));
        State s = db.queryForObject("SELECT * FROM telegram_conversations WHERE chat_id=? FOR UPDATE",
                (rs, row) -> new State(
                        rs.getString("session_id"), rs.getString("stage"), rs.getString("plate"),
                        rs.getString("phone"), rs.getObject("customer_id", Long.class),
                        rs.getString("origin_address"), rs.getBigDecimal("latitude"), rs.getBigDecimal("longitude"),
                        rs.getString("request_id"), rs.getLong("last_update_id"),
                        rs.getInt("failures"), rs.getObject("touched_at", OffsetDateTime.class)), chatId);
        if (s == null || updateId <= s.lastUpdateId) return;
        if (s.touchedAt != null && s.touchedAt.isBefore(OffsetDateTime.now().minusMinutes(30))) s.reset();
        List<Reply> replies = reply(s, chatId, text.trim(), lat, lng);
        db.update("""
                UPDATE telegram_conversations SET session_id=?, stage=?, plate=?, phone=?, customer_id=?,
                    origin_address=?, latitude=?, longitude=?, request_id=?, last_update_id=?,
                    failures=?, touched_at=NOW() WHERE chat_id=?
                """, s.sessionId, s.stage, s.plate, s.phone, s.customerId,
                s.address, s.latitude, s.longitude, s.requestId, updateId, s.failures, chatId);
        int position = 0;
        for (Reply response : replies) {
            db.update("""
                    INSERT INTO telegram_outbound(chat_id,update_id,sequence_number,body,problem_buttons)
                    VALUES (?,?,?,?,?) ON CONFLICT DO NOTHING
                    """, chatId, updateId, position++, response.text(), response.buttons());
        }
    }

    private List<Reply> reply(State s, String chatId, String input, Double lat, Double lng) {
        String command = input.toLowerCase(Locale.ROOT);
        if ("/start".equals(command) || "annulla".equals(command)
                || "cancel".equals(command) || "stop".equals(command)) {
            s.reset();
            return List.of(new Reply("/start".equals(command) ? WELCOME :
                    "Richiesta annullata. Scrivi la targa quando vuoi attivare un soccorso.", false));
        }
        if ("aiuto".equals(command) || "help".equals(command)) {
            return List.of(new Reply("START".equals(s.stage) ? WELCOME :
                    "Rispondi alla domanda che ti ho fatto, oppure scrivi ANNULLA per ricominciare da capo.", false));
        }
        if ("stato".equals(command) || command.contains("a che punto")) {
            try {
                ChatDtos.MissionStatusResponse found = chat.currentStatus(
                        new ChatDtos.MissionStatusRequest("telegram", chatId));
                return List.of(new Reply("La tua richiesta " + found.codice() + ": " +
                        stateLabel(found.stato()) + ".", false));
            } catch (MissionNotFoundException ex) {
                return List.of(new Reply("Non risultano richieste aperte. Scrivi la targa per attivare un soccorso.", false));
            }
        }
        switch (s.stage) {
            case "START" -> {
                String plate = input.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
                if (!PLATE.matcher(plate).matches()) return List.of(new Reply(WELCOME, false));
                s.plate = plate;
                s.stage = "WAIT_PHONE";
                return List.of(new Reply("Inserisca il numero di telefono della persona presente sul posto.", false));
            }
            case "WAIT_PHONE" -> {
                if (!PHONE.matcher(input).matches() || !PHONE.matcher(input.replace(" ", "")).matches()) {
                    return invalid(s, "Numero non valido. Inserisca cifre e, se necessario, il prefisso +.");
                }
                try {
                    ChatDtos.VehicleLookupResponse customer = chat.lookup(
                            new ChatDtos.VehicleLookupRequest(s.sessionId, s.plate));
                    s.phone = input.replace(" ", "");
                    s.customerId = customer.verificato() ? customer.clienteId() : null;
                    s.stage = "WAIT_LOCATION";
                    s.failures = 0;
                    String response = customer.verificato()
                            ? "Targa riconosciuta (" + customer.clienteNome() + ", " + customer.compagniaNome()
                                    + "). Condividi la posizione o scrivi l'indirizzo."
                            : "Stiamo verificando la copertura assicurativa. Intanto condividi la posizione o scrivi l'indirizzo.";
                    return List.of(new Reply(response, false));
                } catch (ResponseStatusException ex) {
                    return List.of(new Reply(ex.getStatusCode().value() == 429 ?
                            "Troppi tentativi. Riprova tra qualche minuto." : UNAVAILABLE, false));
                }
            }
            case "WAIT_LOCATION" -> {
                if (lat != null && lng != null && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                    s.latitude = BigDecimal.valueOf(lat);
                    s.longitude = BigDecimal.valueOf(lng);
                    s.address = String.format(Locale.ROOT, "Posizione condivisa (%.6f, %.6f)", lat, lng);
                } else if (input.length() >= 5 && input.length() <= 500 &&
                        input.codePoints().anyMatch(Character::isLetter)) {
                    s.address = input;
                    s.latitude = null;
                    s.longitude = null;
                } else {
                    return invalid(s, "Scrivi via e città oppure condividi la posizione.");
                }
                s.stage = "WAIT_PROBLEM";
                s.failures = 0;
                return List.of(new Reply("Qual è il problema?", true));
            }
            case "WAIT_PROBLEM" -> {
                String kind = input;
                if (input.matches("[1-5]")) kind = PROBLEMS[Integer.parseInt(input) - 1];
                boolean valid = false;
                for (String p : PROBLEMS) if (p.equals(kind)) valid = true;
                if (!valid) return invalid(s, "Seleziona una delle cinque tipologie di problema.", true);
                if (s.requestId == null) s.requestId = UUID.randomUUID().toString().replace("-", "");
                ChatDtos.CreateMissionRequest request = new ChatDtos.CreateMissionRequest(
                        s.sessionId, s.requestId, s.customerId,
                        null, null, s.address, s.latitude, s.longitude, null,
                        "telegram", chatId, s.plate, s.phone, kind);
                try {
                    ChatDtos.MissionCreatedResponse mission;
                    try {
                        mission = chat.createMission(request);
                    } catch (ResponseStatusException ex) {
                        if (ex.getStatusCode().value() != 403) throw ex;
                        ChatDtos.VehicleLookupResponse renewed = chat.lookup(
                                new ChatDtos.VehicleLookupRequest(s.sessionId, s.plate));
                        s.customerId = renewed.verificato() ? renewed.clienteId() : null;
                        request = new ChatDtos.CreateMissionRequest(
                                s.sessionId, s.requestId, s.customerId, null, null,
                                s.address, s.latitude, s.longitude, null,
                                "telegram", chatId, s.plate, s.phone, kind);
                        mission = chat.createMission(request);
                    }
                    s.reset();
                    if (mission.inVerifica())
                        return List.of(new Reply("La centrale sta verificando la polizza. Codice richiesta "
                                + mission.codice() + ".", false));
                    return List.of(new Reply("Richiesta registrata. Codice " + mission.codice()
                            + ". Scrivi STATO per aggiornamenti.", false));
                } catch (ResponseStatusException ex) {
                    int status = ex.getStatusCode().value();
                    if (status == 422) {
                        s.stage = "WAIT_LOCATION";
                        return List.of(new Reply("Indirizzo non trovato. Scrivilo nuovamente o condividi la posizione.", false));
                    }
                    if (status == 429) return List.of(new Reply("Troppi tentativi. Riprova più tardi.", false));
                    if (status == 400 || status == 409) {
                        s.reset();
                        return List.of(new Reply("La richiesta non è stata accettata. Contatta la centrale.", false));
                    }
                    return List.of(new Reply(UNAVAILABLE, false));
                }
            }
            default -> {
                s.reset();
                return List.of(new Reply(WELCOME, false));
            }
        }
    }

    private static List<Reply> invalid(State s, String text) { return invalid(s, text, false); }
    private static List<Reply> invalid(State s, String text, boolean buttons) {
        s.failures++;
        if (s.failures >= 3) {
            s.reset();
            return List.of(new Reply("Non riesco a capire. Contatta la centrale operativa per assistenza.", false));
        }
        return List.of(new Reply(text, buttons));
    }

    private void flushReplies(String chatId) {
        List<Outgoing> queue = db.query("""
                SELECT id, body, problem_buttons FROM telegram_outbound
                WHERE chat_id=? AND sent_at IS NULL ORDER BY id LIMIT 20
                """, (rs, row) -> new Outgoing(rs.getLong("id"), rs.getString("body"),
                        rs.getBoolean("problem_buttons")), chatId);
        for (Outgoing msg : queue) {
            java.util.HashMap<String, Object> payload = new java.util.HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", msg.text());
            if (msg.buttons()) {
                List<List<Map<String, String>>> rows = new ArrayList<>();
                for (int i = 0; i < PROBLEMS.length; i++) {
                    rows.add(List.of(Map.of("text", PROBLEM_LABELS[i], "callback_data", PROBLEMS[i])));
                }
                payload.put("reply_markup", Map.of("inline_keyboard", rows));
            }
            telegram.post().uri("/sendMessage").body(payload).retrieve().toBodilessEntity();
            db.update("UPDATE telegram_outbound SET sent_at=NOW() WHERE id=? AND sent_at IS NULL", msg.id());
        }
    }

    private static String stateLabel(String state) {
        return switch (state) {
            case "nuova" -> "ricevuta, stiamo cercando un autista";
            case "proposta" -> "in attesa della conferma di un autista";
            case "accettata" -> "presa in carico";
            case "eseguita" -> "intervento concluso";
            case "annullata" -> "annullata";
            case "verifica_polizza" -> "in verifica della copertura assicurativa";
            default -> "in lavorazione";
        };
    }

    private static String tokenSecret(String token) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(
                    ("fornitori-webhook-v1:" + token).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 non disponibile", ex);
        }
    }
    private static long number(Object value) { return value instanceof Number n ? n.longValue() : -1L; }
    private static Double decimal(Object value) { return value instanceof Number n ? n.doubleValue() : null; }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object obj) {
        return obj instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }
    private record Reply(String text, boolean buttons) {}
    private record Outgoing(long id, String text, boolean buttons) {}
    private static final class State {
        String sessionId, stage, plate, phone, address, requestId;
        Long customerId;
        BigDecimal latitude, longitude;
        long lastUpdateId;
        int failures;
        OffsetDateTime touchedAt;
        State(String sessionId, String stage, String plate, String phone, Long customerId,
                String address, BigDecimal latitude, BigDecimal longitude, String requestId,
                long lastUpdateId, int failures, OffsetDateTime touchedAt) {
            this.sessionId = sessionId; this.stage = stage; this.plate = plate;
            this.phone = phone; this.customerId = customerId; this.address = address;
            this.latitude = latitude; this.longitude = longitude; this.requestId = requestId;
            this.lastUpdateId = lastUpdateId; this.failures = failures; this.touchedAt = touchedAt;
        }
        void reset() {
            sessionId = UUID.randomUUID().toString().replace("-", "");
            stage = "START"; plate = null; phone = null; customerId = null;
            address = null; latitude = null; longitude = null; requestId = null; failures = 0;
        }
    }
}
