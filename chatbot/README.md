# Soccorso Stradale Bot

**Responsabile:** Marco Colucci (Membro 3)

Dialogo guidato in Python, senza dipendenze esterne (solo libreria standard, Python 3.10
o superiore), allineato ai dialoghi del docente. Non accede al database: parla
con il backend solo tramite gli endpoint `/api/v1/chat/*` di `api/openapi.yaml`.

**Stato:** il bot Python conserva il polling per sviluppo locale. Il webhook Preview
è implementato invece in Java nel backend, con sessioni e risposte su PostgreSQL.
WhatsApp resta fuori ambito.

## Provarlo

Dalla cartella `chatbot/`:

```bash
python3 -m fornitori_chatbot
```

La console usa dati inventati, stampati all'avvio. `/pos 41.2 16.6` simula la posizione
condivisa, `/esci` termina.

## Collegarlo al backend reale

Backend e bot devono avere lo stesso `FORNITORI_CHAT_TOKEN`: generalo una volta con
`openssl rand -hex 32` e non scriverlo in file del repo. Il database deve avere tutti gli script
`database/` applicati in ordine: vedi il [README principale](../README.md).

Nel terminale del backend, dalla radice del repo:

```bash
./mvnw spring-boot:run
```

In quello del bot, dalla cartella `chatbot/`:

```bash
FORNITORI_API_URL=http://localhost:8080 python3 -m fornitori_chatbot --http
```

Con il backend reale valgono le targhe di `database/02_seed.sql`, per esempio `PW 660 SW`.

## Collegarlo a Telegram

1. Su Telegram apri **@BotFather**, invia `/newbot` e copia il token che ti restituisce.
2. Dalla cartella `chatbot/`, incolla il token al prompt (non viene mostrato) e avvia:

```bash
read -rs TELEGRAM_BOT_TOKEN && export TELEGRAM_BOT_TOKEN
```

```bash
python3 -m fornitori_chatbot --telegram
```

3. Apri il tuo bot su Telegram e premi **Avvia**. Per il backend reale aggiungi `--http`, con
   `FORNITORI_API_URL` e `FORNITORI_CHAT_TOKEN` impostati: senza `--http` il bot usa dati di prova.

Il bot resta attivo finché il comando è in esecuzione. Usa il long polling, quindi non
serve ngrok né un indirizzo pubblico; risponde solo nelle chat private. Il token non va
mai scritto in file del repo o in chat.

In Preview Vercel si usa il nuovo endpoint Java `/api/v1/telegram/webhook`, non
il processo Python. È necessario applicare la migrazione `database/13_telegram_webhook.sql`
al database demo. Il sito apre `t.me/Fornitori_Soccorso_Bot`: il token deve
appartenere a quel bot. Non eseguire polling Python contemporaneamente al webhook.

## Test

Dalla cartella `chatbot/`:

```bash
python3 -m unittest discover
python3 -m compileall -q fornitori_chatbot
```

## Struttura

| File | Contenuto |
|---|---|
| `engine.py` | Macchina a stati: `ChatEngine.handle(messaggio)` restituisce le risposte |
| `messages.py` | Tutti i testi mostrati al cliente |
| `backend.py` | Interfaccia verso il backend e tipi di errore |
| `fake_backend.py` | Backend finto per sviluppo e demo da console |
| `http_backend.py` | Backend reale via HTTP |
| `telegram_channel.py` | Canale Telegram |
| `notifications.py` | Invio al cliente delle notifiche accodate dal backend |
| `__main__.py` | Avvio da console o Telegram |

## Dialogo

`START → WAIT_PHONE → WAIT_LOCATION → WAIT_PROBLEM → START`:
targa → telefono sul posto → lookup polizza → posizione → problema → creazione.
`AIUTO`, `ANNULLA` e `STATO` valgono in
ogni stato; un messaggio che non è una targa riceve il testo di aiuto.

- Dopo la targa: «Inserisca il numero di telefono della persona presente sul posto.»
  Sono ammessi cifre, spazi e un eventuale `+` iniziale, secondo la regex
  `^\+?[0-9][0-9 ]{5,19}$`. Gli spazi vengono rimossi prima dell'invio;
  anche il numero normalizzato deve rispettare la regex.
- Con polizza da verificare: «Attenda... Stiamo verificando la copertura assicurativa del veicolo.
  Intanto condivida la posizione o scriva l'indirizzo.» Il dialogo prosegue con `clienteId` nullo.
- «Qual è il problema?» presenta cinque pulsanti, utilizzabili anche scrivendo 1–5:

  | Numero | Problema | Codice inviato |
  |---|---|---|
  | 1 | Ruota forata con ruota di scorta | `RUOTA_CON_SCORTA` |
  | 2 | Ruota forata senza ruota di scorta | `RUOTA_SENZA_SCORTA` |
  | 3 | Batteria scarica | `BATTERIA_SCARICA` |
  | 4 | Guasto motore | `GUASTO_MOTORE` |
  | 5 | Incidente | `INCIDENTE` |

  Il backend decide l'intervento: il bot invia il solo codice, senza descrizione libera.
- Creazione verificata: «Non appena un autista sarà disponibile riceverai un messaggio.»
  Segue un messaggio con codice e indicazione STATO.
- Creazione in verifica: «Attenda... La centrale sta verificando la polizza e la ricontatterà
  al numero indicato. Codice richiesta {codice}.»
- Accettazione: «La tua richiesta {codice} è stata accettata. Entro 45 minuti arriverà l'autista.»
- Annullamento: «La richiesta {codice} è stata annullata. Per assistenza contatti la centrale operativa.»
- Demo: `AB123CD` ed `EF456GH` sono verificate; ad esempio `ZZ999ZZ` simula una polizza da verificare.
- **Tentativi:** 3 input non validi fanno ricominciare il dialogo; il backend risponde
  429 quando interviene il limitatore anti-abuso.
- **Scadenze:** un dialogo inattivo scade dopo 30 minuti.
- **Riconoscimento:** il backend autorizza una sola richiesta, per 15 minuti, dopo la targa; se scade durante il
  dialogo, il bot rifà il controllo una volta e prosegue, anche con polizza da verificare.
- **Anti-duplicati:** `requestId` resta lo stesso nei tentativi dopo un errore di rete,
  e i `message_id` già visti vengono ignorati.
- **Stato:** `STATO` comprende verifica polizza, gestione manuale e annullamento;
  STATO e notifiche non mostrano mai il nome dell'autista, anche se ricevuto dal backend.
  I tipi di notifica sconosciuti sono ignorati e confermati senza ritentare l'invio.
- **Memoria:** i dialoghi in corso stanno in RAM, quindi un riavvio li azzera. Le missioni
  e lo stato stanno nel backend e restano consultabili.

## Aggiungere un canale

WhatsApp e ulteriori webhook restano fuori ambito del client Python. Per aggiungere un canale basta trasformare ogni messaggio in un
`InboundMessage` (modello: `telegram_channel.py`):

```python
engine = ChatEngine(HttpBackend(url, token))
risposte = engine.handle(InboundMessage(
    channel="telegram",       # oppure "whatsapp"
    chat_id="...",            # id della chat, non esce dal bot
    text="...",
    message_id="...",         # per scartare i callback duplicati
    latitude=None, longitude=None,
))
```

## Limiti noti

1. **Nessuna verifica del titolare:** come nei dialoghi del docente, basta la targa. Chi la
   conosce vede nome e compagnia e può creare una richiesta. È un limite noto della demo, non adatto a
   dati reali (`docs/management/DECISION_LOG.md`, D-007).
2. **Traino:** il backend assegna l'officina più vicina in linea d'aria, non per strada. La
   centrale può cambiarla dalla scheda missione, con **Aggiorna intervento**.
3. **Stato:** `STATO` cerca per chat; il telefono sul posto viene chiesto esplicitamente.
4. **Indirizzi:** geocoding e scelta officina restano al backend. Se l'indirizzo non è trovato,
   il bot chiede una nuova posizione.
5. **Dialogo 10 del foglio:** la frase libera con Gemini è facoltativa e non implementata.
6. **Consenso:** i dialoghi del docente non lo prevedono, quindi il bot non lo chiede.
7. **Notifiche al cliente:** con `--telegram` il bot controlla ogni 5 secondi se ci sono
   messaggi di accettazione, conclusione e annullamento da inviare, e li riserva sul backend così che due bot non
   inviino lo stesso messaggio. Partono solo per le missioni create dal bot: per quelle create dalla
   centrale la chat del cliente non è nota. Se il bot è spento restano in coda e partono alla riaccensione.
   Se si ferma proprio tra l'invio e la registrazione dell'esito, il messaggio può ripartire una volta dopo 5 minuti.
