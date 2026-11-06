# Functional Design — FORNITORI

**Codice:** FOR-FUD-001 · **Versione:** 2.1 · **Data:** 05/11/2026
**Responsabili:** team per componente · **Fonte API normativa:** [`api/openapi.yaml`](../../api/openapi.yaml)

**Attori:** RESPONSABILE, AUTISTA autenticato, cliente via bot, servizio chatbot, dispatcher
notifiche, assegnazione automatica.

## UC-001 — Accesso operatore

**Attore:** responsabile o autista. **Prerequisiti:** utente applicativo configurato.
**Flusso:** il client ottiene il CSRF → invia il login JSON con il token → il backend autentica
e imposta la sessione → il client legge identità e ruolo.
**Eccezioni:** credenziali non valide `401`; CSRF mancante `403`; sessione revocata al logout;
ruolo non autorizzato `403`; troppi tentativi `429`.
**API:** `/api/v1/auth/csrf`, `/login`, `/me`, `/logout`. **Stato:** IMPLEMENTATO.

## UC-002 — Consultare anagrafiche e cercare un cliente

**Attore:** RESPONSABILE. **Flusso:** il client chiama la risorsa → il service applica filtri
e ruoli → restituisce DTO senza credenziali.
**Eccezioni:** `401`, `403`; un risultato vuoto non inventa dati.
**API:** `GET /api/v1/{autisti,mezzi,compagnie,clienti,officine}`. **Stato:** IMPLEMENTATO.

## UC-003 — Inserire un cliente

**Attore:** RESPONSABILE. **Flusso:** dati validati → controllo duplicati e compagnia →
persistenza → DTO. **Eccezioni:** validazione `400`; targa duplicata `409` con messaggio
"Targa già presente"; `401/403`. **API:** `POST /api/v1/clienti`. **Stato:** IMPLEMENTATO.

## UC-004 — Creare una missione

**Attore:** RESPONSABILE o servizio chatbot. **Prerequisiti:** per la centrale, cliente noto; il
traino richiede un'officina. **Flusso centrale:** validazione → codice e stato `nuova` →
persistenza → assegnazione automatica. **Flusso chatbot:** lookup, dialogo guidato, creazione
tramite API servizio: `nuova` con polizza verificata, `verifica_polizza` altrimenti. Il backend
ricava il tipo di intervento dal problema e, per il traino, sceglie l'officina più vicina in linea d'aria.
**Eccezioni:** token errato, input errato, rate limit, duplicato, cliente inesistente, provider
non disponibile; nessuna missione falsa.
**API:** `POST /api/v1/missioni`, `POST /api/v1/chat/missioni`, `POST /api/v1/chat/lookup-targa`.
**Stato:** IMPLEMENTATO; la sola targa è un limite noto della demo.

## UC-005 — Proporre una missione a un autista

**Attore:** assegnazione automatica; RESPONSABILE per la proposta manuale. **Prerequisiti:** missione
proponibile e autista disponibile.
**Flusso automatico:** dopo creazione, rifiuto, scadenza o conferma della polizza il sistema propone
la missione all'autista disponibile, senza missione attiva e non ancora interpellato, con il punto
di partenza più vicino in linea d'aria; ogni 60 secondi riprende le missioni rimaste senza autista.
**Flusso manuale:** dalla scheda, "Invia proposta" a un autista scelto oppure "Proponi al più vicino",
anche in `gestione_manuale`. Proposta e scadenza vengono da configurazione; l'autista la vede
nella sua area entro 15 secondi. **Eccezioni:** autista occupato o non disponibile, oppure stato
incompatibile, `409`. **API:** `POST /api/v1/missioni/{id}/proposte`,
`POST /api/v1/missioni/{id}/proposta-automatica`. **Stato:** IMPLEMENTATO.

## UC-006 — Accettare, rifiutare o far scadere una proposta

**Attore:** AUTISTA destinatario; scheduler per la scadenza. **Flusso:** l'autista risponde prima
della deadline → il service controlla destinatario, stato e tempo. Rifiuto e scadenza generano
un avviso per la centrale e passano la missione all'autista successivo; al terzo autista distinto
che rifiuta o lascia scadere la missione va in `gestione_manuale` e compare nella coda critica.
Con meno di 3 autisti che possono rispondere resta in `non_accettata` o `scaduta` e serve la centrale.
**Eccezioni:** risposta a o dopo la deadline `409`; altro autista `403`.
**API:** `/accept`, `/reject`, `GET /api/v1/centrale/avvisi`. **Stato:** IMPLEMENTATO.

## UC-007 — Avviso, correzione e chiusura

**Attore:** AUTISTA assegnato; RESPONSABILE per correggere intervento e officina.
**Flusso:** eventuale avviso → km totali, proposti dalla stima del percorso → il service valida e
calcola il costo → completa e crea evento e outbox. La centrale corregge tipo e officina dalla
scheda missione, finché la missione è nuova, rifiutata, scaduta o in gestione manuale.
**Eccezioni:** missione non chiudibile, km non validi, ruolo errato; una nota su una missione
dello storico, o dell'autista su una missione non ancora accettata, dà `409`. **API:** `/intervento`, `/complete`, `/notes`. **Stato:** IMPLEMENTATO.

## UC-008 — Calcolare o rettificare il costo

**Attore:** service per il calcolo; RESPONSABILE per la rettifica, dalla scheda missione.
**Regola:** base 50 + 1,20/km, supplemento 25% dalle **20:00 incluse** alle **08:00 escluse**, `HALF_UP`, `Europe/Rome`.
La rettifica vale per le missioni eseguite e per quelle annullate con costo attribuito; richiede un
motivo e conserva costo calcolato e audit.
**API:** `/complete`, `PATCH /cost`. **Stato:** IMPLEMENTATO; inizio fascia riallineato alle 20:00 dal 06/11/2026; resta da confermare col docente il timestamp di accettazione come riferimento.

## UC-009 — Consultare dashboard e guadagni

**Attore:** RESPONSABILE; AUTISTA per i propri dati. **Flusso:** aggregati per modalità reference
o live; l'endpoint autista si limita all'utente autenticato. La dashboard operativa mostra la coda
critica (polizza da verificare, tre esiti negativi, supporto aperto), con un contatore per ogni
motivo; 6 riquadri (Critiche, Da assegnare, KO, OK, Pending, Annullate) e un grafico per stato, che
aprono l'elenco filtrato. Costi e analisi riportano totali, costo per autista e per mese.
**API:** `GET /api/v1/dashboard`, `/api/v1/autista/missioni`, `/api/v1/autista/storico`.
**Stato:** IMPLEMENTATO.

## UC-010 — Conversazione cliente e stato

**Attore:** cliente via Telegram o console. **Flusso:** targa → telefono della persona sul posto →
lookup: nome e compagnia, oppure avviso che la polizza è da verificare → posizione o indirizzo →
problema con cinque pulsanti → creazione. Il tipo di intervento lo decide il backend. `STATO` risponde
per la chat con una frase comprensibile; `AIUTO` e `ANNULLA` sono comandi del dialogo.
**Eccezioni:** polizza non verificabile (la pratica prosegue in `verifica_polizza`), formato non
valido, rate limit, servizio non disponibile. Se il riconoscimento della targa scade (15 minuti), il
bot lo rinnova da solo. Ogni riconoscimento vale per una sola richiesta; le ricerche di targa sono
limitate a 10 al minuto per sessione e a 5 targhe non riconosciute.
**API:** `/api/v1/chat/lookup-targa`, `/missioni`, `/stato`. **Stato:** IMPLEMENTATO.

## UC-011 — Notifiche di presa in carico, completamento e annullamento

**Attori:** backend e dispatcher del bot. **Flusso:** evento outbox transazionale → il bot riserva
le righe → invia → registra l'esito. Se il bot è spento, i messaggi restano in coda.
**Eccezioni:** provider non disponibile (nuovo tentativo dopo 30 secondi, massimo 5); missioni
della centrale senza invio (`SKIPPED`); l'esito di una riserva non più valida viene rifiutato.
**API:** `POST /api/v1/chat/notifiche/claim`, `POST /api/v1/chat/notifiche/{id}/esito`.
**Stato:** IMPLEMENTATO per Telegram; WhatsApp fuori ambito.

## UC-012 — Percorso e mappa

**Attore:** autista e centrale. **Flusso:** officina di riferimento dell'autista, o base del mezzo →
luogo di fermo → eventuale officina per il traino → rientro. La mappa riporta i km di andata, traino,
ritorno e totali. La distanza stimata non equivale ai km effettivi e viene proposta alla chiusura.
**API:** `GET /api/v1/maps/route`. **Stato:** IMPLEMENTATO, con mappa Leaflet nella UI.

## UC-013 — Storico proposte

**Attore:** RESPONSABILE. **Flusso:** dalla scheda missione "Mostra storico proposte" elenca
autista, esito e date di ogni tentativo, anche per le missioni dello storico di riferimento.
**Eccezioni:** `401`, `403`, `404`. **API:** `GET /api/v1/missioni/{id}/proposte`.
**Stato:** IMPLEMENTATO.

## UC-014 — Verificare una polizza

**Attore:** RESPONSABILE. **Prerequisiti:** missione in `verifica_polizza`, creata dal bot con targa
non verificabile. **Flusso:** dalla coda critica o dalla scheda la centrale conferma, scegliendo un
cliente con polizza attiva o creandone uno → la missione torna `nuova` e parte l'assegnazione.
Oppure respinge → la missione è `annullata`, con costo 0 e nessun autista coinvolto.
**Eccezioni:** cliente mancante o inesistente `400`; polizza non attiva o stato diverso `409`.
**API:** `POST /api/v1/missioni/{id}/verifica-polizza`. **Stato:** IMPLEMENTATO.

## UC-015 — Annullare una missione

**Attore:** RESPONSABILE per ogni stato non finale; AUTISTA solo per la propria missione accettata.
**Flusso:** motivo obbligatorio → la missione passa ad `annullata` → la scheda riporta chi, quando,
motivo e importo. Se annulla la centrale dopo l'accettazione, il costo sui km stimati totali resta
attribuito all'autista e si può rettificare; se annulla prima, il costo è 0. Se annulla l'autista, il
suo compenso è 0. Il cliente della chat riceve l'avviso.
**Eccezioni:** motivo mancante `400`; altro autista `403`; stato incompatibile `409`.
**API:** `POST /api/v1/missioni/{id}/annulla`. **Stato:** IMPLEMENTATO.

## UC-016 — Richiedere supporto alla centrale

**Attore:** AUTISTA assegnato; RESPONSABILE per la gestione. **Flusso:** dopo l'accettazione l'autista
descrive il problema → la richiesta compare nella coda critica e in testa alle notifiche della centrale
→ la centrale ne legge il testo e la segna come gestita. Resta aperta finché non viene gestita, anche
se la missione si chiude. **Eccezioni:** autista su missione non accettata `409`; ripetere la gestione
dà `200` senza cambiare chi l'ha gestita. **API:** `POST /api/v1/missioni/{id}/notes` con
`tipo=SUPPORTO`, `POST /api/v1/missioni/{id}/notes/{noteId}/gestita`, `GET /api/v1/centrale/avvisi`.
**Stato:** IMPLEMENTATO.

## UC-017 — Indicare la disponibilità

**Attore:** AUTISTA. **Flusso:** l'interruttore "Disponibile / Non disponibile" nell'area autista
aggiorna il profilo. Un autista non disponibile non riceve proposte automatiche e una proposta
manuale a lui dà `409`. **API:** `PATCH /api/v1/autista/disponibilita`. **Stato:** IMPLEMENTATO.

## Diagrammi

Le sequenze e gli stati sono in [`diagrams/`](../diagrams/).
