# Registro rischi e impedimenti

**Codice:** FOR-RSK-001 · **Versione:** 2.1 · **Data:** 05/11/2026
Scale qualitative: Alto / Medio / Basso.

| ID | Rischio | Prob. | Impatto | Mitigazione | Responsabili | Stato |
|---|---|---:|---:|---|---|---|
| R-01 | PDF del docente ed Excel originale non sono nel repository | Alta | Alto | Acquisirli prima di approvare BUR e dataset | Team | Aperto |
| R-02 | Contratto OpenAPI e comportamento possono divergere | Media | Medio | Aggiornare il contratto insieme al codice | Vincenzo Minervini | Mitigato |
| R-03 | Una modifica può introdurre regressioni | Media | Alto | La pipeline esegue tutti i test su un database temporaneo `fornitori_e2e_ci` | Tutti | Mitigato; pipeline verde su `main` al 05/11/2026 |
| R-04 | Nominatim e OSRM pubblici possono non rispondere | Media | Medio | Errori espliciti; la chiusura missione resta possibile | Marco Colucci | Mitigato |
| R-05 | Setup locale errato: script non applicati | Media | Medio | Seguire gli script del README; Hibernate `validate` lo segnala all'avvio | Tutti | Mitigato |
| R-06 | La targa non prova la titolarità | Alta | Alto | Limite noto della demo: solo dati sintetici | Marco Colucci | Accettato per la demo |
| R-07 | Lo stato della conversazione in RAM si perde al riavvio | Alta | Basso | Le missioni restano nel backend; il cliente ricomincia il dialogo | Marco Colucci | Accettato per la demo |
| R-08 | Il dataset storico include assegnazioni incoerenti con le regole live | Media | Medio | `is_fixture` isola lo storico | Vincenzo Minervini | Mitigato |
| R-09 | Requisiti approvati e verbali Scrum non dimostrati | Media | Medio | Raccogliere verbali e sign-off reali | Team | Aperto |
| R-10 | DOCX rigenerati ma non ispezionati visivamente in Microsoft Word | Media | Medio | File DOCX rigenerati e verificati come archivio Office Open XML; apertura, tabelle e paginazione da verificare prima della consegna definitiva | Team | Aperto, impaginazione da collaudare |
| R-11 | Messaggio duplicato se il bot si ferma tra invio ed esito | Bassa | Basso | Riserva persistente; consegna "almeno una volta" | Marika Sancilio, Francesco Valeriano, Giuseppe Cangellosi | Accettato per la demo |
| R-12 | Il ruolo `fornitori_app` è proprietario delle tabelle e può modificarne la struttura | Bassa | Medio | Separare proprietario e ruolo applicativo, dopo un backup (V-13) | Vincenzo Minervini | Aperto |
| R-13 | HTTPS e reverse proxy non sono stati provati | Media | Alto | Controllo della configurazione all'avvio in `prod`; prova su un server reale prima dell'uso (V-15) | Vincenzo Minervini | Aperto |
| R-14 | Al primo avvio lo scheduler propone agli autisti anche vecchie missioni di prova | Media | Alto | Pulire i dati prima dell'avvio, seguendo la [checklist](../deploy/CHECKLIST_AMBIENTE_REALE.md) | Vincenzo Minervini | Mitigato; checklist mai eseguita |
| R-15 | Il bot non risponde se il processo Python non gira su un server sempre acceso: Vercel non può ospitarlo | Media | Alto | Processo dedicato con `TELEGRAM_BOT_TOKEN`, `FORNITORI_CHAT_TOKEN` e backend raggiungibile | Marco Colucci | Aperto |

`R-01`, `R-06`, `R-12` e `R-13` vanno risolti prima di dichiarare l'idoneità a un uso reale.
