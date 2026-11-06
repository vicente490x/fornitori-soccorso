package it.exprivia.fornitori.service;

import it.exprivia.fornitori.common.MissionConflictException;
import it.exprivia.fornitori.common.MissionNotFoundException;
import it.exprivia.fornitori.dto.ChatDtos;
import it.exprivia.fornitori.dto.MissionDtos;
import it.exprivia.fornitori.entity.ChatMissionRequest;
import it.exprivia.fornitori.entity.Customer;
import it.exprivia.fornitori.entity.InsuranceCompany;
import it.exprivia.fornitori.entity.Mission;
import it.exprivia.fornitori.enums.ProblemType;
import it.exprivia.fornitori.entity.NotificationOutbox;
import it.exprivia.fornitori.repository.ChatMissionRequestRepository;
import it.exprivia.fornitori.repository.ChatLookupGrantRepository;
import it.exprivia.fornitori.repository.CustomerRepository;
import it.exprivia.fornitori.repository.InsuranceCompanyRepository;
import it.exprivia.fornitori.repository.MissionRepository;
import it.exprivia.fornitori.repository.NotificationOutboxRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Rome");
    private static final List<String> CHAT_CHANNELS = List.of("telegram", "whatsapp");
    private static final List<String> OPEN_STATES = List.of(
            "nuova", "proposta", "accettata", "non_accettata", "scaduta", "verifica_polizza", "gestione_manuale");
    private static final String CANCELLED = "annullata";
    private static final String IN_VERIFICATION = "verifica_polizza";
    private static final int MIN_PLATE_LENGTH = 5;
    private static final int MAX_PLATE_LENGTH = 10;
    private static final int MAX_NOTIFICATION_ATTEMPTS = 5;
    private static final int MAX_LOOKUPS_PER_MINUTE = 100;
    private static final int MAX_LOOKUPS_PER_MINUTE_BY_SESSION = 10;
    private static final int NOTIFICATION_LEASE_MINUTES = 5;
    private static final int NOTIFICATION_RETRY_SECONDS = 30;

    private final NotificationOutboxRepository outbox;
    private final CustomerRepository customers;
    private final InsuranceCompanyRepository companies;
    private final MissionRepository missions;
    private final ChatMissionRequestRepository requests;
    private final MissionService missionService;
    private final ChatLookupLimiter limiter;
    private final NearestWorkshopResolver workshopResolver;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final ChatLookupGrantRepository grants;
    private final RequestRateLimiter globalLimiter;

    public ChatService(
            NotificationOutboxRepository outbox,
            CustomerRepository customers,
            InsuranceCompanyRepository companies,
            MissionRepository missions,
            ChatMissionRequestRepository requests,
            MissionService missionService,
            ChatLookupLimiter limiter,
            NearestWorkshopResolver workshopResolver,
            PlatformTransactionManager transactionManager,
            Clock clock,
            ChatLookupGrantRepository grants,
            RequestRateLimiter globalLimiter) {
        this.outbox = outbox;
        this.customers = customers;
        this.companies = companies;
        this.missions = missions;
        this.requests = requests;
        this.missionService = missionService;
        this.limiter = limiter;
        this.workshopResolver = workshopResolver;
        this.transactions = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.grants = grants;
        this.globalLimiter = globalLimiter;
    }

    @Transactional
    public ChatDtos.VehicleLookupResponse lookup(ChatDtos.VehicleLookupRequest request) {
        globalLimiter.check("chat-lookup", MAX_LOOKUPS_PER_MINUTE);
        globalLimiter.check("chat-lookup-session:" + request.sessionId(), MAX_LOOKUPS_PER_MINUTE_BY_SESSION);
        if (limiter.isBlocked(request.sessionId())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many lookup attempts");
        }
        String plate = normalize(request.targa());
        Optional<Customer> customer = customers.findByTargaNorm(plate).filter(this::hasActivePolicy);
        if (customer.isEmpty()) {
            limiter.recordFailure(request.sessionId());
            // La polizza la verificherà la centrale; un testo che non è una targa non apre nessuna pratica.
            if (validPlate(plate)) {
                grants.grant(request.sessionId(), null, plate, now().plusMinutes(15), now());
            } else {
                grants.revoke(request.sessionId());
            }
            return ChatDtos.VehicleLookupResponse.notVerified();
        }
        grants.grant(request.sessionId(), customer.get().getId(), plate, now().plusMinutes(15), now());
        String company = companies.findById(customer.get().getCompanyId())
                .map(InsuranceCompany::getNome)
                .orElseThrow(() -> new IllegalStateException("Customer company is missing"));
        String customerName = customer.get().getNome();
        if (customer.get().getCognome() != null && !customer.get().getCognome().isBlank()) {
            customerName += " " + customer.get().getCognome();
        }
        return new ChatDtos.VehicleLookupResponse(
                true, customer.get().getId(), customerName, company,
                ChatDtos.VehicleLookupResponse.VERIFIED);
    }

    /** La ricerca dell'officina chiama un servizio esterno: resta fuori dalla transazione che blocca il cliente. */
    public ChatDtos.MissionCreatedResponse createMission(ChatDtos.CreateMissionRequest received) {
        String channel = validChannel(received.canaleOrigine());
        ChatDtos.CreateMissionRequest request = withInterventionDecided(received);
        // Prima di chiamare il geocoder esterno: senza un lookup valido la richiesta non ha motivo di costare nulla.
        if (!requests.existsById(request.requestId()) && !isGranted(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lookup richiesto o scaduto");
        }
        ChatDtos.CreateMissionRequest resolved = needsNearestWorkshop(request)
                ? workshopResolver.withNearestWorkshop(request)
                : request;
        return transactions.execute(status -> persistMission(resolved, fingerprint(received), channel));
    }

    /** Con tipoProblema decide il backend; senza, è una richiesta del vecchio formato e deve essere completa. */
    private static ChatDtos.CreateMissionRequest withInterventionDecided(ChatDtos.CreateMissionRequest request) {
        if (request.tipoProblema() == null) {
            if (request.clienteId() == null || isBlank(request.tipoIntervento()) || isBlank(request.descrizioneProblema())) {
                throw badRequest("Richiesta incompleta");
            }
            return request;
        }
        ProblemType problem = ProblemType.fromCode(request.tipoProblema())
                .orElseThrow(() -> badRequest("Tipo di problema non valido"));
        if (isBlank(request.targa()) || !validPlate(normalize(request.targa()))) {
            throw badRequest("Targa non valida");
        }
        if (isBlank(request.telefonoSulPosto())) {
            throw badRequest("Telefono sul posto obbligatorio");
        }
        return request.withIntervention(problem.intervention(), problem.label());
    }

    private boolean isGranted(ChatDtos.CreateMissionRequest request) {
        String plate = request.targa() == null ? null : normalize(request.targa());
        return grants.permits(request.sessionId(), request.clienteId(), plate, now());
    }

    private static boolean validPlate(String plate) {
        return plate.length() >= MIN_PLATE_LENGTH && plate.length() <= MAX_PLATE_LENGTH;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private boolean needsNearestWorkshop(ChatDtos.CreateMissionRequest request) {
        return request.officinaId() == null
                && "traino".equals(request.tipoIntervento().trim().toLowerCase(Locale.ROOT))
                && !requests.existsById(request.requestId());
    }

    /**
     * L'impronta anti-duplicato si calcola sulla richiesta ricevuta dal bot, non su quella arricchita con
     * officina e coordinate: un nuovo invio identico deve dare lo stesso risultato, non 409.
     */
    private ChatDtos.MissionCreatedResponse persistMission(
            ChatDtos.CreateMissionRequest request, String requestFingerprint, String channel) {
        // Serializza i tentativi concorrenti con lo stesso requestId, così il secondo trova il primo.
        Customer customer = null;
        if (request.clienteId() == null) {
            grants.lockRequest(request.requestId());
        } else {
            customer = customers.lockById(request.clienteId()).orElseThrow(() -> badRequest("Cliente inesistente"));
        }

        Optional<ChatMissionRequest> existing = requests.findById(request.requestId());
        if (existing.isPresent()) {
            Mission mission = missions.findById(existing.get().getMissionId())
                    .orElseThrow(() -> new IllegalStateException("Chat request mission is missing"));
            // Dopo la conferma della polizza la missione ha un cliente che la richiesta originale non poteva indicare.
            if ((request.clienteId() != null && !request.clienteId().equals(mission.getCustomerId()))
                    || !request.sessionId().equals(existing.get().getSessionId())
                    || !request.destinatario().trim().equals(existing.get().getDestinatario())
                    || !channel.equals(mission.getCanaleOrigine())
                    || (existing.get().getRequestFingerprint() != null
                        && !requestFingerprint.equals(existing.get().getRequestFingerprint()))) {
                throw new MissionConflictException("Chiave di idempotenza già usata");
            }
            return created(mission.getId(), mission.getCodice(), mission.getStato());
        }

        if ((customer != null && !hasActivePolicy(customer)) || !isGranted(request)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lookup richiesto o scaduto");
        }

        MissionDtos.MissionResponse created = missionService.createFromChat(
                new MissionDtos.CreateMissionRequest(
                        request.clienteId(),
                        request.tipoIntervento(),
                        request.descrizioneProblema(),
                        request.origineIndirizzo(),
                        request.origineLat(),
                        request.origineLng(),
                        request.officinaId(),
                        null,
                        channel),
                isBlank(request.telefonoSulPosto()) ? null : request.telefonoSulPosto().trim(),
                request.tipoProblema(),
                isBlank(request.targa()) ? null : normalize(request.targa()));
        ChatMissionRequest stored = new ChatMissionRequest(
                request.requestId(), created.id(), request.sessionId(), request.destinatario().trim(), now());
        stored.setRequestFingerprint(requestFingerprint);
        requests.save(stored);
        // Un lookup autorizza una sola pratica; il retry con lo stesso requestId passa dal ramo sopra.
        grants.revoke(request.sessionId());
        return created(created.id(), created.codice(), created.stato());
    }

    private static ChatDtos.MissionCreatedResponse created(Long id, String code, String state) {
        return new ChatDtos.MissionCreatedResponse(id, code, state, IN_VERIFICATION.equals(state));
    }

    @Transactional(readOnly = true)
    public ChatDtos.MissionStatusResponse currentStatus(ChatDtos.MissionStatusRequest request) {
        Mission mission = latestForChat(validChannel(request.canale()), request.destinatario().trim());
        OffsetDateTime updatedAt = Stream.of(
                        mission.getChiusaIl(), mission.getAccettataIl(), mission.getPropostaIl(), mission.getCreataIl())
                .filter(Objects::nonNull)
                .max(OffsetDateTime::compareTo)
                .orElse(mission.getCreataIl());
        // Il cliente non riceve mai il nome dell'autista: il campo resta per compatibilità.
        return new ChatDtos.MissionStatusResponse(mission.getCodice(), mission.getStato(), null, updatedAt);
    }

    /** La pratica aperta più recente; altrimenti l'ultima, ma solo se annullata: chi chiede deve saperlo. */
    private Mission latestForChat(String channel, String recipient) {
        List<String> states = Stream.concat(OPEN_STATES.stream(), Stream.of(CANCELLED, "eseguita")).toList();
        List<Mission> rows = missions.findOpenByChat(channel, recipient, states);
        return rows.stream()
                .filter(mission -> OPEN_STATES.contains(mission.getStato()))
                .findFirst()
                .or(() -> rows.stream().findFirst().filter(mission -> CANCELLED.equals(mission.getStato())))
                .orElseThrow(MissionNotFoundException::new);
    }

    /**
     * PENDING -> SENDING nella stessa transazione del lock. Una riga SENDING con riserva scaduta indica un
     * dispatcher caduto: conta come tentativo e torna disponibile.
     */
    @Transactional
    public ChatDtos.NotificationsResponse claimNotifications(String channel) {
        OffsetDateTime now = now();
        List<ChatDtos.NotificationResponse> claimed = new java.util.ArrayList<>();
        for (NotificationOutbox notification : outbox.lockClaimable(validChannel(channel), now)) {
            if ("SENDING".equals(notification.getStato())) {
                notification.recordFailure("Riserva scaduta senza esito", MAX_NOTIFICATION_ATTEMPTS, now);
            }
            if ("PENDING".equals(notification.getStato())) {
                notification.claim(now.plusMinutes(NOTIFICATION_LEASE_MINUTES));
                claimed.add(toNotification(notification));
            }
        }
        return new ChatDtos.NotificationsResponse(claimed);
    }

    @Transactional
    public void reportNotification(Long id, ChatDtos.NotificationResultRequest result) {
        NotificationOutbox notification = outbox.lockById(id)
                .filter(candidate -> CHAT_CHANNELS.contains(candidate.getChannel()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        // Il tentativo lega l'esito alla riserva: un dispatcher in ritardo non può cambiare quella successiva.
        if (!"SENDING".equals(notification.getStato()) || notification.getTentativi() != result.tentativo()) {
            return;
        }
        if (result.inviata()) {
            notification.markSent(now());
        } else {
            notification.recordFailure(
                    result.errore(), MAX_NOTIFICATION_ATTEMPTS, now().plusSeconds(NOTIFICATION_RETRY_SECONDS));
        }
    }

    private static ChatDtos.NotificationResponse toNotification(NotificationOutbox notification) {
        Map<String, Object> payload = notification.getPayload();
        return new ChatDtos.NotificationResponse(
                notification.getId(),
                (String) payload.get("eventType"),
                notification.getDestinatario(),
                (String) payload.get("codice"),
                null,
                notification.getTentativi());
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String fingerprint(ChatDtos.CreateMissionRequest request) {
        // Length-prefix each canonical field to avoid separator ambiguity; no plaintext payload persisted.
        StringBuilder canonical = new StringBuilder();
        // Il vecchio formato conserva la stessa impronta: un nuovo invio di una richiesta già salvata non dà 409.
        List<Object> fields = request.tipoProblema() == null
                ? java.util.Arrays.asList(request.clienteId(), request.sessionId(),
                        request.destinatario().trim(), validChannel(request.canaleOrigine()),
                        request.tipoIntervento().trim().toLowerCase(Locale.ROOT), request.descrizioneProblema().trim(),
                        request.origineIndirizzo().trim(), request.origineLat(), request.origineLng(), request.officinaId())
                : java.util.Arrays.asList(request.clienteId(), request.sessionId(),
                        request.destinatario().trim(), validChannel(request.canaleOrigine()),
                        request.tipoProblema(), normalize(request.targa()), request.telefonoSulPosto().trim(),
                        request.origineIndirizzo().trim(), request.origineLat(), request.origineLng(), request.officinaId());
        for (Object value : fields) {
            String text = value instanceof java.math.BigDecimal number
                    ? number.stripTrailingZeros().toPlainString() : Objects.toString(value, "");
            canonical.append(text.length()).append(':').append(text);
        }
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private boolean hasActivePolicy(Customer customer) {
        LocalDate today = LocalDate.ofInstant(clock.instant(), BUSINESS_ZONE);
        return customer.getScadenzaPolizza() != null && !customer.getScadenzaPolizza().isBefore(today);
    }

    private static String validChannel(String value) {
        String channel = value.trim().toLowerCase(Locale.ROOT);
        if (!CHAT_CHANNELS.contains(channel)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Canale non valido");
        }
        return channel;
    }

    private static String normalize(String value) {
        return value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
