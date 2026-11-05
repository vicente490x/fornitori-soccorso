# Frontend: dashboard operativa, centrale e autista

## Aggiornamento layout dashboard — 05/11/2026

- Riquadri uniformi: 2 colonne su telefono, 3 nelle larghezze intermedie,
  6 da 1200 px; nessun riquadro isolato a larghezza piena.
- Spaziatura, numeri e testi coerenti; focus visibile da tastiera.
- Barra superiore opaca durante lo scorrimento.
- In ambiente demo la barra informativa e quella di navigazione occupano
  righe distinte, anche quando il testo informativo va a capo.

> **Record storico:** branch già fuso in `main`. Descrive la situazione al momento del merge. Dopo il merge il riquadro "Rifiutate da 3 autisti" è stato tolto: i riquadri sono 6 e il dato resta nel contatore della coda critica. Lo stato attuale è nel [manuale utente](../manuals/MANUALE_UTENTE.md).

Branch `feature/frontend-dashboard`. **Va fuso dopo `feature/backend-operativita`.**

**Hash di partenza:** `d6ab902` (`origin/main`). L'atteso era `eb62c65`: i due commit in mezzo aggiungono e tolgono `docs/prompts/`, quindi `git diff eb62c65 d6ab902` è vuoto. Stessi contenuti, stessa base del backend.

File toccati: `src/main/resources/static/{index.html,app.js,styles.css}` e questo documento.

## Modifiche

| Requisito | Cosa cambia |
|---|---|
| A, dashboard | `#dashboardPage` è la dashboard operativa: coda critica in alto con azioni dirette, 7 riquadri che aprono l'elenco filtrato, istogramma per stato (anch'esso cliccabile). Si aggiorna ogni 30 s e si ferma fuori dalla pagina o a scheda nascosta |
| A, costi | Nuova sezione "Costi e analisi": totali, costo per autista, costo per mese, eseguite e annullate con costo attribuito, "Guadagni per autista" (con la scelta della vista) e conteggi anagrafici. Le pagine anagrafiche restano nel menu |
| B, missioni | "Luogo di fermo" ovunque; pulsanti affiancati; riga cliente compatta con dettagli espandibili; filtro e colori per i nuovi stati |
| B/I, mappa | Marker distinti per forma, colore e lettera (D, F, O), legenda, popup, riepilogo km andata / traino / ritorno / totali. Se deposito e officina coincidono, il secondo marker si sposta di lato |
| C, avvisi e supporto | Segnalazioni rinominate "Avvisi"; "Richiedi supporto alla centrale" con modale; campanella rinominata "Notifiche centrale" con il tipo e i SUPPORTO in testa; supporto evidenziato in coda, card e campanella |
| D, annullamento | Modale con motivo obbligatorio e avviso economico per ruolo e stato; riepilogo (chi, quando, motivo, importo) su card e storico; rettifica disponibile anche sugli annullamenti con autista |
| E, polizza | Conferma con ricerca cliente o creazione (si torna poi alla verifica con il cliente già selezionato); respingi; campo Cognome nel modale cliente |
| G/I, autista | Contatti cliente e persona sul posto con link `tel:`, tipo di problema, interruttore di disponibilità, "Km totali (andata + ritorno)" precompilato |

## Scelte

- **Grafici in HTML/CSS nativi, non Chart.js.** Bastano barre su una sola scala: niente nuova dipendenza né hash `integrity` da mantenere, si adattano con il CSS (a 380 px l'istogramma passa a barre orizzontali) e i valori restano testo leggibile. Ogni grafico ha la vista in tabella.
- **Un solo colore per serie**, rosso solo per gli stati critici, già nominati in etichetta.
- **Filtri della dashboard applicati nel browser:** `GET /missioni?stato=` accetta un solo valore, mentre KO, pending e critiche ne richiedono più d'uno (e il supporto non è uno stato).
- **"Segna come gestito" passa da un modale** che mostra il testo della richiesta: chi chiude ha letto cosa chiude. Per questo `noteId` degli avvisi non serve al frontend.
- **Le SUPPORTO aperte contano sempre come da leggere** e non usano l'ultimo id letto: la voce "Segna come letti" non può nasconderle.
- **Dati mancanti:** sezioni nascoste o un messaggio esplicito, mai numeri ricavati. Senza `disponibile` da `/auth/me` l'interruttore non appare.
- **Cognome facoltativo:** gli anagrafici esistenti non lo hanno e il contratto non lo rende obbligatorio.

## Verifica

Ambiente: merge locale usa-e-getta con `origin/feature/backend-operativita` (`779d4e2`), PostgreSQL temporaneo con migrazioni 01→11, account di seed con password di prova solo in quel database. Branch, worktree e database sono stati eliminati e nulla è stato pushato. OSRM e Nominatim erano quelli reali.

| Controllo | Backend reale | Mock del contratto |
|---|---|---|
| `node --check app.js` | ✅ | |
| `ContractAndProviderTests` sul merge (scansiona le chiamate di `app.js`) | ✅ | |
| Login dal form, dashboard, categorie, grafico | ✅ | ✅ |
| Polizza da chat (`DA_VERIFICARE`) → nuovo cliente → conferma → proposta automatica | ✅ | ✅ |
| Respingi polizza → annullata, costo 0 | ✅ | ✅ |
| Filtri dai riquadri (KO, critiche, tre rifiuti) | ✅ tre rifiuti | ✅ |
| Tre rifiuti reali da tre autisti → `gestione_manuale` → "Proponi al più vicino" dalla coda | ✅ | ✅ |
| Mappa con 3 marker, legenda, km | ✅ | ✅ |
| Avviso e supporto dall'autista; supporto in coda e campanella; segna gestito | ✅ | ✅ |
| Annullamento centrale dopo accettazione: costo attribuito, poi rettifica | ✅ | ✅ |
| Annullamento centrale prima dell'accettazione: costo 0 | ✅ | |
| Annullamento autista: 0 €, presente nello storico | ✅ | ✅ |
| Vista autista: contatti dalla proposta, disponibilità, chiusura con km totali | ✅ | ✅ |
| Costi: totali, per autista, per mese, vista di riferimento | ✅ | ✅ |
| HTML ostile nei dati mostrato come testo | | ✅ |
| Aggiornamento periodico: 1 chiamata in 33 s sulla dashboard, 0 fuori | | ✅ |
| Compatibilità con le risposte del vecchio backend: nessun errore né dato inventato | | ✅ |
| 380 px: nessuno sforamento su dashboard, missioni e costi (la tabella clienti scorre nel suo riquadro), autista | ✅ autista | ✅ |
| Console senza errori (solo i 401 attesi del controllo sessione) | ✅ | ✅ |
| Contrasto dei nuovi colori: testo ≥ 5,3:1, barre ≥ 4,9:1 (calcolato) | | |

**Non verificato:**

- La proposta che scade davvero (`scaduta` reale, 10 minuti): provata solo la resa dello stato.
- La suite Java completa sul merge: eseguita solo `ContractAndProviderTests`.
- Il bot: `feature/telegram-bot` non è stato fuso; le missioni da chat sono state create chiamando l'API.
- L'ultimo ritocco (etichetta "Intervento" sulle missioni senza `tipoProblema`, testo del riquadro supporto, separatore dei km) è passato da `node --check` ma non è stato rivisto nel browser.

## Richieste di modifica al contratto

Nessuna blocca il merge.

1. **`tipo` degli avvisi e `km` di `/maps/route`:** già segnalati in `BACKEND.md`. Il frontend accetta sia `rifiutata`/`scaduta` sia `RIFIUTO`/`SCADENZA`, e `kmTotali`, `km` o `kmStimati`.
2. **`cognome` in `POST /clienti`:** non è elencato nella sezione 4 del contratto, ma il backend lo gestisce. Da aggiungere al testo.
3. **`disponibilita: "non_disponibile"` in `/autisti`:** introdotto dal backend, non è nel contratto. Il frontend lo mostra e disabilita l'autista nella proposta manuale.

## Per il merge

- **Pagina pubblica non aggiornata (`index.html`, fuori dall'incarico):** dice ancora che al cliente viene scritto il **nome** dell'autista, e descrive il vecchio dialogo del bot (scelta intervento e descrizione, senza telefono sul posto). Contraddice le sezioni 3 e 6 del contratto: va allineata quando è fuso anche il bot.
- **Colori esistenti:** alcune etichette preesistenti (`.field-label`, `#929aab` su bianco) restano sotto 3:1. Non le ho toccate per non cambiare lo stile fuori dal perimetro.
