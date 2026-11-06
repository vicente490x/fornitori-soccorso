# Manuale utente FORNITORI

**Codice:** FOR-USER-001 · **Versione:** 2.2 · **Data:** 05/11/2026
**Destinatari:** responsabile centrale, autista e cliente
**Responsabili:** Francesco Valeriano, Giuseppe Cangellosi, Marika Sancilio, Marco Colucci

## Prima di iniziare

Il client web è presente in `src/main/resources/static/` ed è servito dal backend
Spring Boot. Per usare l'area riservata occorrono un ambiente avviato, database
caricato e credenziali autorizzate fornite dall'amministratore. Non sono incluse
credenziali demo in questa guida. Il percorso cliente usa il chatbot Telegram,
separato dalla pagina pubblica informativa.

## Responsabile centrale

### Accesso

1. Aprire l'URL dell'applicazione fornito dall'amministratore.
2. Selezionare **Area operatori**.
3. Inserire email e password autorizzate e premere **Accedi**.
4. Se l'accesso riesce, si apre **Dashboard operativa**. L'errore di credenziali non valide
   viene mostrato sotto il modulo; non ripetere login con account non autorizzati.

### Dashboard operativa

I sei riquadri riepilogativi hanno dimensioni uniformi. Su telefono sono disposti
su due colonne, su tablet e PC compatti su tre colonne e da 1200 px su una riga
di sei colonne. Ogni riquadro continua ad aprire le missioni filtrate per categoria.
La barra superiore ha uno sfondo opaco per mantenere leggibili i comandi durante
lo scorrimento. Il focus da tastiera sui riquadri è evidenziato.


- In alto, la **Coda critica** elenca le missioni che richiedono un intervento: polizza da verificare, rifiutate da tre autisti, richieste di supporto aperte. Ogni riga ha le azioni dirette. Sopra l'elenco, i contatori **Verifica polizza**, **Rifiutate da 3 autisti** e **Supporto aperto** mostrano da dove nascono le critiche e aprono le missioni corrispondenti.
- I riquadri **Critiche**, **Da assegnare**, **KO**, **OK**, **Pending** e **Annullate** aprono l'elenco delle missioni filtrato. Lo stesso vale per le barre del grafico **Missioni per stato**.
- La pagina si aggiorna da sola ogni 30 secondi mentre è aperta; **Aggiorna dati** ricarica subito le informazioni.

### Costi e analisi

- Mostra il totale generale, le missioni eseguite e quelle annullate con costo attribuito, il costo per autista e per mese, con la tabella dei dati sotto ogni grafico.
- **Guadagni per autista** riporta i totali sulle missioni eseguite. Il selettore in alto passa da **Operativa** a **Storico di riferimento**.
- In fondo alla pagina ci sono i conteggi delle anagrafiche.

### Anagrafiche

- La navigazione laterale apre **Clienti**, **Autisti**, **Mezzi**, **Compagnie** e **Officine**. Le pagine sono consultabili; la ricerca consente di filtrare e per i clienti cerca nome o targa.
- Per registrare un cliente aprire **Clienti → Nuovo cliente**. Sono obbligatori nome, telefono, targa, compagnia assicurativa, numero e scadenza della polizza; cognome, modello veicolo e città sono facoltativi. Premere **Salva cliente**; se il server accetta la richiesta la finestra si chiude e compare **Cliente aggiunto correttamente**. In caso di errore, il messaggio indica il problema, per esempio **Targa già presente**. Il telefono accetta cifre, un `+` iniziale e i separatori comuni.

### Creare una missione

1. Aprire **Missioni → + Nuova missione**.
2. Selezionare il cliente e il tipo d'intervento. Per **Traino**, selezionare anche l'officina; per la riparazione sul posto l'officina non è richiesta.
3. Selezionare il canale d'origine, inserire luogo di fermo e problema/note. Il valore del canale è un metadato: scegliere WhatsApp non attiva un provider WhatsApp.
4. Premere **Crea missione**. Il sistema propone la missione all'autista disponibile più vicino. Se l'autista rifiuta o non risponde, passa al successivo.
5. Dopo tre esiti negativi da tre autisti diversi la missione va in **Gestione manuale** e compare nella coda critica: nella scheda scegliere un autista e premere **Invia proposta**, oppure **Proponi al più vicino**. Gli stessi comandi servono se una missione resta senza autista.

### Consultare e correggere una missione

- I filtri **Operativa / Storico di riferimento** e **Tutti gli stati** selezionano le missioni. Ogni scheda riporta su una riga cliente, telefono, targa e telefono sul posto; **Dettagli cliente** apre il resto dell'anagrafica.
- **Mostra avvisi** carica gli avvisi e le richieste di supporto dell'autista.
- **Mostra mappa** apre il percorso con deposito (D), luogo di fermo (F) e officina (O), e i km di andata, traino, ritorno e totali.
- **Mostra storico proposte** elenca autista, esito e data di ogni tentativo.
- **Aggiorna intervento** corregge tipo e officina, finché la missione è nuova, rifiutata, scaduta o in gestione manuale.
- **Correggi costo** rettifica una missione eseguita o annullata con un autista attribuito: serve un motivo, il costo calcolato resta visibile.
- **Annulla missione** vale per ogni stato non finale e chiede un motivo. Se l'autista aveva accettato, il costo sui km stimati totali gli viene attribuito; altrimenti il costo è 0 €. La scheda mostra poi chi ha annullato, quando, il motivo e l'importo.

### Verifica polizza

- Le richieste dal bot con polizza non verificabile arrivano in **Verifica polizza**, con targa dichiarata e telefono sul posto, nella coda critica e nella scheda della missione.
- **Conferma polizza** cerca un cliente con polizza attiva, oppure ne crea uno con **+ Nuovo cliente**. Dopo la conferma la missione passa all'assegnazione automatica.
- **Respingi** annulla la missione: costo 0 € e nessun autista coinvolto.

### Notifiche centrale e supporto

La campanella in alto mostra **Notifiche centrale**: rifiuti, scadenze e richieste di supporto, con le richieste aperte in testa. Si aggiorna ogni 15 secondi. **Apri missione** porta alla scheda; **Segna come letti** azzera il contatore solo in questo browser e non nasconde le richieste di supporto aperte. **Segna come gestito** mostra il testo della richiesta e la chiude.

## Autista

1. Accedere con un account autorizzato al ruolo autista.
2. In **Le mie missioni**, consultare proposta, contatti del cliente e della persona sul posto, luogo di fermo, tipo di problema, mappa, guadagno stimato e tempo residuo. Le nuove proposte compaiono da sole entro 15 secondi. L'interruttore **Disponibile / Non disponibile** indica se si possono ricevere nuove proposte.
3. Premere **Accetta** oppure **Rifiuta**. Se accettata, la missione attiva compare nella stessa pagina, con i contatti e la targa del veicolo.
4. Per informare la centrale, dopo aver accettato, compilare **Avvisi alla centrale** e premere **Invia avviso**. L'esito è visibile nella lista degli avvisi.
5. Per un problema da gestire, premere **Richiedi supporto alla centrale** e descriverlo: la richiesta compare nella coda critica della centrale e resta aperta finché non viene gestita.
6. Per chiudere l'intervento, controllare **Km totali (andata + ritorno)**, proposti dalla stima del percorso, e premere **Chiudi missione · OK**. Solo dopo l'accettazione del server la scheda si aggiorna con conferma di chiusura.
7. **Annulla missione** chiede un motivo: il compenso per quella missione è 0 € e la missione resta nello storico.
8. Aprire **Storico e guadagni** per consultare le missioni completate e annullate e il totale.

Nome, cognome e telefono del cliente e telefono della persona sul posto sono visibili già dalla proposta; la targa compare dopo l'accettazione e nello storico i contatti non compaiono. Se il servizio mappe non risponde, i chilometri si possono inserire comunque.

## Cliente e chatbot

La pagina pubblica contiene informazioni e contatti; non invia direttamente una
richiesta al backend. Per il flusso bot occorre il canale Telegram predisposto dal
team:

1. Aprire **Soccorso Stradale Bot** con il pulsante della pagina pubblica, o da `t.me/Fornitori_Soccorso_Bot`, e premere **Avvia**.
2. Inserire la targa e seguire le domande del dialogo: telefono della persona sul posto, posizione o indirizzo, problema da scegliere con i pulsanti. Il tipo di intervento lo stabilisce il sistema.
3. Usare `AIUTO` per le indicazioni, `ANNULLA` per uscire e `STATO` per consultare la richiesta associata alla chat, se presente.
4. Se il bot segnala indisponibilità, non inviare richieste ripetute; seguire le indicazioni ricevute o contattare la centrale.

Se la polizza non è verificabile la richiesta viene comunque registrata e la centrale ricontatta al numero indicato. Quando un autista accetta, quando l'intervento è concluso e quando la richiesta viene annullata, il bot avvisa nella stessa chat, senza mai indicare il nome dell'autista. Il riconoscimento basato sulla sola targa è un limite noto della demo: non verifica la titolarità del veicolo. Il bot non è un servizio di emergenza. Consultare [`chatbot/README.md`](../../chatbot/README.md) per prerequisiti e limiti del bot.

## Errori e sicurezza

`401` indica sessione/credenziale non valida; `403` permessi insufficienti; `429` troppi
tentativi: attendere un minuto prima di riprovare. Un errore `500` riporta un riferimento da
comunicare al team tecnico. Per errori di validazione o conflitto leggere il messaggio e verificare lo stato prima
di ripetere un'azione. Non condividere password, token o dati personali tramite
canali non autorizzati. Per emergenze stradali usare i canali ufficiali del gestore
stradale o i servizi di emergenza.

## Tariffa notturna (regola in vigore dal 06/11/2026)

La fascia notturna copre le **20:00 incluse fino alle 08:00 escluse** del giorno successivo, in ora italiana (`Europe/Rome`). Il supplemento del 25% si applica al costo calcolato a partire dall'ora di accettazione della missione: alle 19:59 si applica la tariffa diurna, dalle 20:00 la notturna e dalle 08:00 di nuovo la diurna. I costi delle missioni già chiuse non vengono ricalcolati al cambio di configurazione.
