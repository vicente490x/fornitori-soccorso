# Disegno del database

**Codice:** FOR-DB-001 · **Versione:** 2.1 · **Data:** 05/11/2026
Fonte schema: gli script in `database/`. Diagramma: [`database_er.mmd`](../diagrams/database_er.mmd).

## Modello

Gli account sono in `app_users`; un `drivers` collega un account a un mezzo. `customers` fa
riferimento a una compagnia assicurativa. Una `missions` appartiene a un cliente, che può mancare
solo in `verifica_polizza` o se la centrale ha respinto la pratica, e può riferirsi ad autista e
officina. `mission_proposals` conserva i tentativi; note ed eventi sono separati per
l'audit; il pricing è una configurazione singola; `notifications_outbox` conserva invio ed esito.

| Tabella | Scopo e relazioni principali |
|---|---|
| `app_users` | Utenti autenticabili, email univoca, hash BCrypt e ruolo |
| `drivers` | Autista legato a un solo utente e a un solo mezzo; disponibilità e officina di riferimento, punto di partenza e rientro |
| `vehicles` | Targa univoca, tipologia, portata e coordinate della base |
| `insurance_companies` | Compagnie assicurative dei clienti |
| `customers` | Cliente (nome e cognome), targa normalizzata univoca, compagnia e scadenza polizza |
| `workshops` | Officine e coordinate |
| `missions` | Cliente, autista e officina, stato, località, date, chilometri, costi e flag fixture; telefono sul posto, tipo di problema, targa dichiarata e dati dell'annullamento |
| `mission_proposals` | Tentativi di proposta e scadenza, con storico |
| `mission_notes` | Note collegate a missione e utente, di tipo `AVVISO` o `SUPPORTO`; per il supporto, chi l'ha gestito e quando |
| `mission_events` | Audit della missione con dettagli JSON |
| `pricing_config` | Parametri tariffari e durata della proposta |
| `notifications_outbox` | Eventi da inviare, stato di invio e tentativi |
| `chat_mission_requests` | Legame requestId, sessione, destinatario e missione; impronta anti-duplicati |
| `chat_lookup_grants` | Autorizzazione di 15 minuti dopo un lookup: lega la sessione al cliente o, con polizza da verificare, alla targa |

## Integrità

- PK identity, FK e chiavi uniche per email, targa, `targa_norm`.
- Check su ruolo, stati, tipi di intervento, coordinate (entrambe nulle o entrambe valorizzate),
  costo, chilometri e note.
- `missions.is_fixture` distingue i 30 dati storici dalle operazioni live.
- Indici parziali limitano autista attivo e proposta corrente.
- `NUMERIC` per moneta e distanze, `TIMESTAMPTZ` per gli istanti, `JSONB` per eventi e outbox.

## Stato di invio delle notifiche

`notifications_outbox.stato` vale `PENDING`, `SENDING`, `SENT`, `FAILED` o `SKIPPED`.
`prossimo_tentativo_il` indica la fine della riserva o il momento del nuovo tentativo. Le missioni
della centrale generano righe `SKIPPED` senza telefono. Vedi
[guida notifiche](../team/MEMBRO_5_NOTIFICATIONS.md).

## Transazioni e concorrenza

Transizioni di missione e outbox sono nei service; i repository eseguono lock pessimistici e la
riserva delle notifiche usa `FOR UPDATE SKIP LOCKED`. I vincoli DB sono una protezione ulteriore e
non sostituiscono autorizzazione o validazione.

## Baseline del seed

10 utenti, 8 autisti, 8 mezzi, 4 compagnie, 40 clienti, 6 officine e 30 missioni, con 16 eseguite,
6 aperte, 4 scadute, 4 rifiutate e €1.509,71. Il file Excel del docente non è nel repository.

## Setup locale

Usare PostgreSQL 18 e un database vuoto, di proprietà dell'utente applicativo, con le credenziali
nell'ambiente. Applicare tutti gli script in ordine:

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

**Fascia tariffaria corrente:** `night_start = 20:00`, `night_end = 08:00` in `pricing_config` (si applica all'ora locale `Europe/Rome` di accettazione). Lo script 10 è conservato come migrazione storica; lo script 12 ripristina l'inizio alle 20:00 senza ricalcolare missioni chiuse.

Hibernate usa `validate`: senza lo script 11 l'applicazione non parte. Il 09 è additivo: vincoli di coerenza e indici. L'11 aggiunge
gli stati `verifica_polizza`, `gestione_manuale` e `annullata`, e le colonne descritte sopra. Su un database esistente
applicare solo gli script mancanti, dopo un backup. `08_cleanup_and_delivery.sql` rimuove
`chat_sessions` e `drivers.stato_seed`: dopo di esso non rieseguire 01 e 02. Se fallisce la
validazione di `missions_origin_pair` in 07, verificare i dati: non inventare coordinate. Non
modificare le fixture per far passare l'assertion.
