# Checklist per l'ambiente reale

Per portare in un ambiente reale la versione con operatività missioni, dashboard e nuovo bot.
**Nessun passo è stato eseguito:** né questa checklist né [`pulizia_dati_prova.sql`](./pulizia_dati_prova.sql) sono mai stati provati su un database.

## Perché quest'ordine

- Senza la migrazione 11 il backend nuovo **non parte** (`ddl-auto=validate`).
- Lo scheduler non è un processo a parte: **parte con il backend**. Circa 60 secondi dopo l'avvio propone agli autisti tutte le missioni live in `nuova`, `non_accettata` e `scaduta`, anche vecchie, quindi la pulizia va fatta prima.
- La migrazione 11 non ha uno script di ritorno: per tornare indietro serve il backup.

## Prima di iniziare

- [ ] Decidere chi esegue i passi e quando; avvisare centrale e autisti del fermo.
- [ ] Controllare che il database sia già alla migrazione 10. **Non** rieseguire `01_schema.sql` e `02_seed.sql`.
- [ ] Controllare se esistono missioni live reali: da questo dipende l'`ambito` della pulizia.
- [ ] Provare i passi 3-6 su una copia del database, comprese le anteprime.

## Procedura

1. [ ] **Fermare il backend e il bot**, così nessuno crea o modifica missioni durante i passi seguenti.

2. [ ] **Fare il backup** e verificare che il file esista e non sia vuoto.

   ```bash
   pg_dump -Fc -d <database> -f fornitori_prima_migrazione_11.dump
   ```

3. [ ] **Applicare la migrazione 11**, con un ruolo proprietario delle tabelle (non `fornitori_app`).

   ```bash
   psql -d <database> -v ON_ERROR_STOP=1 -f database/11_operativita_missioni.sql
    psql -d <database> -v ON_ERROR_STOP=1 -f database/12_night_rate_20.sql
   ```

4. [ ] **Verificare la migrazione:** la query deve restituire 3 righe.

   ```sql
   SELECT table_name, column_name FROM information_schema.columns
   WHERE (table_name, column_name) IN (('missions','telefono_sul_posto'), ('drivers','disponibile'), ('customers','cognome'));
   ```

5. [ ] **Pulizia, anteprima:** non salva nulla. Leggere le missioni che verrebbero cancellate e i clienti elencati.

   ```bash
   psql -d <database> -f docs/deploy/pulizia_dati_prova.sql
   ```

   Se tutte le missioni live sono di prova, aggiungere `-v ambito=tutte`.

6. [ ] **Pulizia, conferma:** solo se l'anteprima è quella attesa. Alla fine `ancora_assegnabili` deve essere 0.

   ```bash
   psql -d <database> -v conferma=SI -f docs/deploy/pulizia_dati_prova.sql
   ```

   Con `-v ambito=aperte`, cioè il default, le missioni `accettata` ed `eseguita` restano.

7. [ ] **Controllare la configurazione:** token del bot (`FORNITORI_CHAT_TOKEN`), credenziali del database, profilo `prod`. Sostituire le password dimostrative del seed, se presenti.

8. [ ] **Avviare il backend nuovo.** Lo scheduler parte con lui. Per controllare prima l'applicazione senza proposte automatiche, avviarlo una prima volta con l'argomento `--fornitori.missions.auto-assign=false`, poi riavviarlo senza.

9. [ ] **Avviare il bot nuovo** su un server sempre acceso, dalla cartella `chatbot/`, con `TELEGRAM_BOT_TOKEN`, `FORNITORI_CHAT_TOKEN` e `FORNITORI_API_URL` impostati. Backend e bot vanno aggiornati insieme: un bot vecchio rifiuta ancora le targhe da verificare.

   ```bash
   python3 -m fornitori_chatbot --telegram --http
   ```

   Controllare in @BotFather che il token sia del bot `Fornitori_Soccorso_Bot`, quello aperto dal sito. Senza `--http` il bot usa dati di prova.

## Controlli dopo l'avvio

- [ ] Login di responsabile e autista; dashboard senza missioni inattese.
- [ ] Nei primi minuti nessuna proposta su missioni vecchie, sia nella pagina Missioni sia nel log del backend.
- [ ] Una richiesta di prova completa dal bot con polizza verificata: proposta, accettazione, chiusura, avvisi.
- [ ] Una richiesta con targa non verificabile: `verifica_polizza`, conferma o respingi dalla centrale.
- [ ] La pagina Costi mostra importi coerenti.

## Se qualcosa va storto

1. Fermare backend e bot.
2. Ripristinare il backup su un database vuoto, controllando il risultato prima di usarlo:

   ```bash
   pg_restore -d <database_vuoto> fornitori_prima_migrazione_11.dump
   ```

3. Riavviare la versione precedente di backend e bot: quella nuova non parte su un database senza migrazione 11.
