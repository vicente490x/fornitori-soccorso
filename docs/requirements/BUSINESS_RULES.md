# Regole di business — fonte consolidata

Le regole del modello persistito e le transizioni sono descritte nel
[Functional Design](./FUD_FORNITORI.md); scelte e questioni aperte nel
[registro decisioni](../management/DECISION_LOG.md).

## Valori implementati (non approvazione docente)

- Stati missione: `nuova`, `proposta`, `accettata`, `eseguita`, `non_accettata`, `scaduta`,
  `verifica_polizza`, `gestione_manuale`, `annullata`; le fixture storiche restano isolate e
  immutabili, note comprese.
- Scadenza proposta: valore seed 10 minuti; lo scheduler valuta solo le missioni live.
- Assegnazione automatica all'autista disponibile, senza missione attiva e non ancora interpellato per
  quella missione, con il punto di partenza più vicino. Al terzo autista distinto che rifiuta o lascia
  scadere, `gestione_manuale`: decide la centrale.
- Una pratica con polizza non verificabile resta in `verifica_polizza` senza cliente: la centrale la
  conferma con un cliente con polizza attiva oppure la respinge, con costo 0.
- Annullamento con motivo obbligatorio. Centrale: ogni stato non finale; se l'autista aveva accettato,
  il costo sui km stimati totali gli resta attribuito, altrimenti è 0. Autista: solo la propria
  missione accettata, con compenso 0.
- Tariffa seed: uscita €50, €1,20/km, supplemento 25%. Fascia notturna dalle **20:00 incluse**
  alle **08:00 escluse**, sull'ora di accettazione, in `Europe/Rome`; arrotondamento `HALF_UP`.
- Un'accettazione scaduta è impedita; la riproposizione conserva ogni esito di proposta.
- La rettifica richiede ruolo centrale e motivazione; vale per le missioni eseguite e per quelle annullate con costo attribuito; costo calcolato e finale sono distinti.
- L'intervento e l'officina sono correggibili solo su missioni nuove, rifiutate, scadute o in gestione manuale.
- Il supporto chiesto dall'autista resta aperto finché la centrale non lo segna come gestito.
- Nome, cognome e telefono del cliente e telefono della persona sul posto visibili all'autista assegnato dalla proposta; targa solo dopo l'accettazione; nessun contatto nello storico.
- Notifiche al cliente (accettazione, completamento, annullamento) solo per le missioni create dal bot, senza mai il nome dell'autista.

La fascia operativa è stata riallineata il 06/11/2026 all'indicazione del PDF del docente (20:00–08:00). Il timestamp di riferimento (accettazione) resta da confermare col docente.
Le missioni dello storico restano immutabili: 3 su 30 risultano notturne perché accettate tra le 20:00
e le 22:00, quando era applicata la fascia precedente. Lo script 10 testimonia la temporanea configurazione 22:00–08:00; il 12 imposta nuovamente le 20:00, senza modificare gli importi storici.
