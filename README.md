# FORNITORI — gestione del soccorso stradale

Project work ITS Developer 4.0 per coordinare richieste di soccorso stradale, centrale operativa
e autisti. Comprende backend Java/Spring, client web con mappa, workflow delle missioni e chatbot
Telegram. Requisiti e acceptance del docente restano da confermare: PDF ed Excel originali non sono
nel repository.

Apri l'[indice della documentazione](./docs/00_INDICE_GENERALE.md).

## Demo personale

Questa copia è gestita autonomamente da `vicente490x`. Non sincronizza automaticamente
le modifiche con la repository originale di `coluccimarco`.

- [Demo condivisa](https://fornitori-soccorso-demo.vercel.app/).
- [Accesso dimostrativo centrale/autista](https://fornitori-soccorso-demo.vercel.app/demo.html).
- [Configurazione e collegamento Vercel](docs/deploy/DEMO_PERSONALE.md).

La demo usa un database Neon dedicato e dati sintetici condivisi tra i visitatori.
L'ambiente richiesto è Preview. Il dominio stabile, da solo, non identifica il tipo
di deployment e non autorizza una pubblicazione in Production.

## Stato

| Area | Stato |
|---|---|
| Backend: sessione, CSRF, ruoli, anagrafiche, missioni, tariffe, note, dashboard | Implementato |
| Operatività missioni: assegnazione automatica, gestione manuale dopo 3 esiti negativi, verifica polizza, annullamento, supporto autista, disponibilità | Implementato |
| UI web: pagina pubblica, dashboard operativa, costi, centrale e area autista, mappa Leaflet | Implementato |
| Mappe: geocodifica, percorso e km stimati con Nominatim e OSRM | Implementato |
| Chatbot Telegram e API `chat/*` | Implementato; la sola targa identifica il cliente, limite noto della demo |
| Notifiche al cliente con stato di invio persistente | Implementato per le missioni create dal bot |
| WhatsApp, webhook in ingresso, gestione utenti | Fuori ambito |

## Requisiti

- JDK 25 LTS.
- PostgreSQL 18 o compatibile, con `psql`.
- Maven Wrapper incluso.
- Python 3.10 o superiore per il chatbot.

## Stack

| Area | Tecnologia |
|---|---|
| Backend | Java 25, Spring Boot 4.1.2, Spring Security, Spring Data JPA |
| Build | Maven Wrapper 3.9.9 |
| Persistenza | PostgreSQL 18 |
| Contratto | REST/JSON, OpenAPI 3.0.3 |
| Client | HTML, CSS, JavaScript, Bootstrap 5.3, Leaflet, serviti da Spring Boot |
| Chatbot | Python, sola libreria standard, Telegram in long polling |
| Mappe | Nominatim e OSRM pubblici |

## Configurazione

La password PostgreSQL non è nel repository. Impostarla localmente (PowerShell):

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/fornitori"
$env:SPRING_DATASOURCE_USERNAME = "fornitori_app"
$env:SPRING_DATASOURCE_PASSWORD = "<password configurata localmente>"
```

Su macOS e Linux usare `export` con gli stessi nomi.

## Database

Creare un database vuoto di proprietà dell'utente applicativo:

```powershell
createdb -O fornitori_app fornitori
```

Eseguire tutti gli script in ordine:

```powershell
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/01_schema.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/02_seed.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/05_chat_requests.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/06_chat_recipient.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/07_backend_hardening.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/08_cleanup_and_delivery.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/09_integrity_and_indexes.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/10_night_rate_22.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/11_operativita_missioni.sql
psql -v ON_ERROR_STOP=1 -U fornitori_app -d fornitori -f database/12_night_rate_20.sql
```

La tariffa notturna è **20:00 incluse – 08:00 escluse**, fuso `Europe/Rome`, sulla data/ora di accettazione della missione. Per un database già esistente eseguire **solo** la migrazione `12_night_rate_20.sql` (dopo gli script precedenti); non ricalcolare le missioni già concluse.

Hibernate è in `validate`: senza lo script 11 l'applicazione non si avvia. Su un database esistente applicare
solo gli script mancanti, nell'ordine, dopo un backup; per un ambiente già in uso seguire la
[checklist](./docs/deploy/CHECKLIST_AMBIENTE_REALE.md). Dopo 08 non rieseguire 01 e 02:
`08_cleanup_and_delivery.sql` rimuove `chat_sessions` e `drivers.stato_seed`.

Le righe del docente in `missions` sono marcate `is_fixture=TRUE` e costituiscono lo storico di riferimento
al 28/10/2026: non vengono mai modificate.

## Build e avvio

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Su macOS e Linux usare `./mvnw`. Aprire `http://localhost:8080/`: la pagina pubblica è accessibile senza
login, l'area riservata usa la sessione e le API dello stesso server.

La pipeline [backend-ci.yml](./.github/workflows/backend-ci.yml) compila il progetto, applica gli script a
un PostgreSQL effimero, esegue tutti i test su un database `fornitori_e2e_ci` e verifica l'avvio
dell'applicazione, sempre con un ruolo senza privilegi di amministratore.

## Test

I test su database cancellano i dati live: partono solo con `FORNITORI_TEST_DATABASE=isolated` e un database `fornitori_e2e*`. Comandi e risultati in
[SECURITY_TEST_REPORT.md](./docs/security/SECURITY_TEST_REPORT.md).

## API

Il contratto è in [api/openapi.yaml](./api/openapi.yaml): 42 operazioni, 38 implementate e 4 fuori ambito.
Mappa delle superfici in [API_SPECIFICATION.md](./docs/architecture/API_SPECIFICATION.md).

Le API centrali richiedono `RESPONSABILE`; le mutazioni richiedono il token di `GET /api/v1/auth/csrf`
nell'header `X-CSRF-TOKEN`. Le API `chat/*` usano `Authorization: Bearer` con `FORNITORI_CHAT_TOKEN`, da
impostare uguale su backend e chatbot; se manca, rispondono `401`. Vedi [chatbot/README.md](./chatbot/README.md).

## Chatbot in produzione

Il bot risponde solo se il processo Python è avviato su un server sempre acceso, con `TELEGRAM_BOT_TOKEN`
impostato e con il backend raggiungibile. Dalla cartella `chatbot/`:

```bash
python3 -m fornitori_chatbot --telegram --http
```

`--http` collega il bot al backend reale tramite `FORNITORI_API_URL` e `FORNITORI_CHAT_TOKEN`; senza questa
opzione il bot usa dati di prova. Vercel ospita solo il frontend e non può far girare il bot in polling.

## Struttura

```text
src/main/java/it/exprivia/fornitori/
├── config/       # Spring Security e sessione
├── controller/   # endpoint REST
├── dto/          # payload di richiesta e risposta
├── entity/       # entità JPA
├── enums/        # ruoli applicativi
├── repository/   # accesso ai dati
├── service/      # logica applicativa
└── common/       # gestione condivisa degli errori
src/main/resources/static/   # client web
database/                    # script SQL, da applicare in ordine
chatbot/fornitori_chatbot/   # bot Telegram
```

Il flusso è `controller → service → repository → entity`; i DTO evitano di esporre le entità.

## Credenziali demo e sicurezza

Il seed contiene solo hash BCrypt delle password d'esempio; le credenziali per la demo vanno consultate nei
materiali del docente. Questa è una demo con dati sintetici: la sola targa identifica il cliente e il progetto
non è pronto per dati reali. Vedi [sicurezza](./docs/architecture/SECURITY_DESIGN.md).

## Team

| Area | Responsabili | Guida |
|---|---|---|
| Backend | Vincenzo Minervini | [Guida](./docs/team/MEMBRO_1_BACKEND.md) |
| Frontend | Francesco Valeriano, Giuseppe Cangellosi, Marika Sancilio | [Guida](./docs/team/MEMBRO_2_FRONTEND.md) |
| Chatbot | Marco Colucci | [Guida](./docs/team/MEMBRO_3_CHATBOT.md) |
| Mappe | Marco Colucci | [Guida](./docs/team/MEMBRO_4_MAPS.md) |
| Notifiche | Marika Sancilio, Francesco Valeriano, Giuseppe Cangellosi | [Guida](./docs/team/MEMBRO_5_NOTIFICATIONS.md) |

Altri riferimenti: [manuale tecnico](./docs/manuals/MANUALE_TECNICO.md),
[manuale utente](./docs/manuals/MANUALE_UTENTE.md), [database](./docs/architecture/DATABASE_DESIGN.md),
[sicurezza del backend](./docs/security/BACKEND_SECURITY_AUDIT.md), [Word editabili](./docs/word/).

## Pacchetto documentale (06/11/2026)

I **nove documenti Word** principali sono stati rigenerati da Markdown e sono in [docs/word/](docs/word/). L'[indice](docs/00_INDICE_GENERALE.md) è la mappa delle fonti; la [verifica documentale](docs/quality/VERIFICA_DOCUMENTALE_2026-11-06.md) distingue ciò che è provato dai gate ancora aperti. La fascia notturna corrente è 20:00 (inclusa)–08:00 (esclusa), supplemento 25% su ora di accettazione in `Europe/Rome`. I costi storici restano invariati.

**I PDF di presentazione e il vecchio workbook restano storici:** non assumere che riflettano l'ultima regola senza un controllo specifico. Il merge e la build Vercel non sostituiscono un'esecuzione CI completa né una verifica manuale in browser.
