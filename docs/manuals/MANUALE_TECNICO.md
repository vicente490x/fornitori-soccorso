# Manuale tecnico FORNITORI

**Codice:** FOR-TECH-001 · **Versione:** 2.1 · **Data:** 05/11/2026
**Destinatari:** sviluppatori · **Responsabile:** Vincenzo Minervini

## Prerequisiti

- Git e JDK 25; verificare `java -version` e `javac -version`.
- PostgreSQL 18 o compatibile e `psql`.
- Maven Wrapper incluso; non serve installare Maven.
- Python 3.10+ per il chatbot, solo con la libreria standard.
- Node.js non è necessario: il frontend non ha un package manager.

## Configurazione e avvio

Da PowerShell, nella root del repository:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/fornitori"
$env:SPRING_DATASOURCE_USERNAME = "fornitori_app"
$env:SPRING_DATASOURCE_PASSWORD = "<password autorizzata>"
```

Su un database nuovo applicare tutti gli script, in ordine:

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

La migrazione 12 imposta `pricing_config.night_start = '20:00'` senza modificare costi o flag delle missioni chiuse. Su un database operativo aggiornare solo la configurazione tramite lo script 12; la logica Java legge `night_start` e `night_end` dal DB (fuso `Europe/Rome`, accettazione inclusa alle 20:00, 08:00 esclusa).

Su un database esistente applicare solo gli script mancanti, dopo un backup; per un ambiente già in uso seguire la
[checklist](../deploy/CHECKLIST_AMBIENTE_REALE.md). Hibernate usa `validate`: senza lo script 11 l'avvio fallisce con
un errore `Schema validation`. Dopo 08 non rieseguire 01 e 02. Per un uso condiviso, usare
un account per le migrazioni e un account applicativo non amministratore con `USAGE` sullo schema, DML sulle tabelle e
uso delle sequenze.

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

Aprire `http://localhost:8080/`. Per HTTPS usare `SPRING_PROFILES_ACTIVE=prod`: il cookie `Secure` non viene inviato su
HTTP. Sessione inattiva 30 minuti; login limitati a 20 al minuto per IP.

## Proprietà applicative

| Proprietà | Default | Uso |
|---|---|---|
| `fornitori.chat.service-token` (`FORNITORI_CHAT_TOKEN`) | vuoto | Token del chatbot; se vuoto le API chat rispondono 401 |
| `fornitori.missions.expiry-delay-ms` | 30000 | Intervallo del controllo delle scadenze |
| `fornitori.missions.auto-assign` | `true` | Proposte automatiche all'autista più vicino; `false` le spegne |
| `fornitori.missions.assignment-delay-ms` | 60000 | Intervallo con cui si riprendono le missioni rimaste senza autista |
| `fornitori.maps.nominatim-url`, `fornitori.maps.osrm-url` | servizi pubblici | Provider di mappe |
| `fornitori.maps.user-agent` | `FORNITORI-project-work` | User-Agent verso Nominatim |
| `fornitori.maps.viewbox` | Puglia | Area di ricerca; vuota per tutta Italia |

## Struttura frontend

| File | Responsabilità |
|---|---|
| `src/main/resources/static/index.html` | Pagina pubblica, login, area centrale e autista, modali |
| `src/main/resources/static/app.js` | Chiamate API, sessione e CSRF, viste, mappa, aggiornamenti periodici |
| `src/main/resources/static/styles.css` | Layout dell'area riservata, responsive |
| `src/main/resources/static/public-site.css` | Sito pubblico |
| `src/main/resources/static/images/` | Illustrazione, icona del chatbot, favicon |

Il client aggiorna ogni 15 secondi gli avvisi della centrale e le proposte dell'autista, ogni 30 secondi la dashboard
mentre è aperta. Gli errori mostrano `message` del server o il testo del codice `error`.

## Assegnazione automatica

`AutoAssignmentService` propone la missione dopo il commit, su un altro thread, per creazione, rifiuto, scadenza e
conferma della polizza; uno scheduler riprende ogni 60 secondi quelle rimaste senza autista. **Lo scheduler parte con il
backend:** al primo avvio propone tutte le missioni live in `nuova`, `non_accettata` e `scaduta`, anche vecchie, quindi i
dati di prova vanno puliti prima. Al terzo autista distinto che rifiuta o lascia scadere la missione passa a
`gestione_manuale` e decide la centrale.

## Mappe

`GET /api/v1/maps/route?missionId=...&driverId=...` richiede sessione e `X-CSRF-TOKEN`. `MapService` compone il punto di partenza (officina di
riferimento dell'autista o, in mancanza, base del mezzo), il luogo di fermo, l'officina per il traino e il rientro. Se mancano
le coordinate usa Nominatim, con al massimo una richiesta al secondo per istanza; OSRM restituisce geometria e km di andata,
traino, ritorno e totali. La ricerca è limitata alla Puglia: fuori area un indirizzo risulta "non
trovato". Codici di risposta in [`MEMBRO_4_MAPS.md`](../team/MEMBRO_4_MAPS.md).

## Notifiche

Il bot chiama `POST /api/v1/chat/notifiche/claim`, invia e registra l'esito. Stati e tempi sono in
[`MEMBRO_5_NOTIFICATIONS.md`](../team/MEMBRO_5_NOTIFICATIONS.md). Le righe in `FAILED` si leggono su
`notifications_outbox.ultimo_errore`.

## Chatbot

Da `chatbot/`, seguire [`chatbot/README.md`](../../chatbot/README.md) per console, backend HTTP e Telegram. Backend e bot
condividono `FORNITORI_CHAT_TOKEN`. Il bot usa solo `/api/v1/chat/*`, mai il database. Per rispondere ai clienti gira con
`python3 -m fornitori_chatbot --telegram --http`, su un server sempre acceso: senza `--http` usa dati di prova, e Vercel,
che ospita solo il frontend, non può farlo girare in polling.

## Controlli prima di un merge

```powershell
.\mvnw.cmd clean package
git diff --check
```

Poi avviare l'applicazione e provare a mano il flusso modificato.

## Test

I test su database cancellano i dati live: partono solo con `FORNITORI_TEST_DATABASE=isolated` e si fermano se il database non si chiama `fornitori_e2e*`.
Comandi e risultati in [`SECURITY_TEST_REPORT.md`](../security/SECURITY_TEST_REPORT.md).

## Risoluzione dei problemi

- `Schema validation: missing column`: mancano script SQL, applicare tutti quelli in `database/`.
- `401/403`: verificare sessione, token e ruolo.
- `400/409`: leggere il messaggio e lo stato prima di ritentare.
- `503` mappe: servizio esterno non disponibile; i km si possono comunque inserire.
- Non registrare password, bearer token, messaggi Telegram o dati personali.
