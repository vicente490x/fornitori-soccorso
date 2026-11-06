# Demo personale di Vicente490x

Configurazione sorgenti aggiornata il 6 novembre 2026.

## Risorse dedicate

| Risorsa | Destinazione |
|---|---|
| Sorgente autonoma | `vicente490x/fornitori-soccorso`, branch `main` |
| Team Vercel | Wrath (`wrath5`) |
| Progetto Vercel | `fornitori-soccorso-demo` |
| Link condiviso | https://fornitori-soccorso-demo.vercel.app/ |
| Portale dei profili | https://fornitori-soccorso-demo.vercel.app/demo.html |
| Neon | Progetto `fornitori-soccorso-demo`, branch `demo`, database `fornitori_demo` |

Il codice deriva dal project work originale, del quale conserva storia e attribuzioni.
Questa repository è una copia indipendente. Non sono configurati sincronizzazioni,
push o aggiornamenti automatici verso `coluccimarco/fornitori-soccorso`.
I progetti RUSH e gli altri database non fanno parte di questa demo.

## Build riproducibile

La root è la radice della repository. `vercel.json` seleziona il framework `container`
e la regione `fra1`. `Dockerfile.vercel` compila Java 25 con
`./mvnw --batch-mode --no-transfer-progress -Pvercel-demo clean package`.
I test che richiedono PostgreSQL isolato possono risultare saltati in questa build.

Il profilo Maven `vercel-demo` include Spring Session JDBC. Il runtime richiede
i profili Spring `prod,demo`; `prod` abilita cookie sicuri, mentre il target Vercel
deve rimanere Preview. Il profilo Spring non determina l'ambiente Vercel.

Le aggiunte demo sono state recuperate dai sorgenti del deployment Preview
`dpl_3gftr1Ud7NkX4ZvDiyKP68AXkDCW`. Il confronto dei contenuti conferma la stessa
configurazione, pagine demo e logica Java; restano le modifiche responsive già
presenti su `main` nel commit `a78da2d`.

## Variabili e database

Configurare nell'ambiente Preview del solo progetto Vercel:

- `SPRING_PROFILES_ACTIVE=prod,demo`
- `PORT=8080`
- `SPRING_DATASOURCE_URL`, JDBC PostgreSQL con `sslmode=verify-full`
- `SPRING_DATASOURCE_USERNAME`, ruolo runtime dedicato
- `SPRING_DATASOURCE_PASSWORD`, variabile segreta
- `FORNITORI_CHAT_TOKEN`, variabile segreta dedicata

Non salvare credenziali, file `.env` o configurazioni locali `.vercel` in Git.
Usare solo il database dimostrativo già predisposto. Gli aggiornamenti del codice
non devono ricrearlo, rieseguire i seed o modificare database di altri progetti.
Le tabelle sessione esistenti vengono riutilizzate: la creazione automatica dello
schema è disabilitata. Il ruolo runtime non deve acquisire privilegi DDL.

## Collegamento automatico e dominio

Stato alla preparazione di questo documento: la Preview esiste e il dominio stabile
la raggiunge, mentre il collegamento Git del progetto Vercel deve ancora essere
completato e verificato. Il commit di questi file, da solo, non attiva il collegamento.

Configurazione prevista nella dashboard Vercel:

1. Collegare il progetto esistente a `vicente490x/fornitori-soccorso`.
2. Impostare il branch di Production su un branch riservato diverso da `main`,
   senza pubblicarlo. I push su `main` devono generare Preview.
3. Associare il dominio `fornitori-soccorso-demo.vercel.app` alle Preview del branch
   `main`, se consentito dalla piattaforma. Un alias assegnato a un singolo deployment
   non prova che verrà aggiornato automaticamente ai push successivi.
4. Verificare un deployment creato da Git, il suo SHA, lo stato READY e il target
   Preview prima di considerare attiva l'automazione.

Non usare promozioni o `vercel --prod` senza autorizzazione esplicita del proprietario.
Se la piattaforma richiede un passaggio manuale, registrarlo come pendente.

## Verifica dopo ogni aggiornamento

Controllare home, `/demo.html`, `/api/v1/demo/info`, accesso centrale e autista,
lettura delle rispettive API e rifiuto delle API della centrale per l'autista.
Il login e il logout richiedono CSRF e sessioni HTTPS. Separare i controlli HTTP
dal collaudo visivo desktop/mobile e dai test di scrittura sul database.

Il portale consente accesso pubblico a due account sintetici prefissati.
`DemoController` esiste solo con il profilo `demo` e rifiuta database con nome
diverso da `fornitori_demo`. Non inserire dati reali: le modifiche sono condivise.
Il chatbot Telegram non viene distribuito dal container web. I link dimostrativi
rimandano al portale locale senza usare il bot della repository originale.


## Bot Telegram sulla Preview del dominio stabile

**Username corretto:** [@SoccorsoStradaleBot](https://t.me/SoccorsoStradaleBot).
Il token Preview deve corrispondere a questo bot; il controller lo verifica tramite `getMe` prima di registrare il webhook.
I pulsanti Telegram sulla home della demo devono aprire il bot, senza essere reindirizzati a `/demo.html`.

Il progetto Vercel mantiene il dominio `fornitori-soccorso-demo.vercel.app` associato
al branch `main`. `TELEGRAM_BOT_TOKEN` è salvato come Secret per Preview;
`FORNITORI_API_URL` è disponibile in Preview. Nessun token viene versionato.
Il bot Telegram della demo usa un webhook HTTPS Java persistente su Neon
(`database/13_telegram_webhook.sql`) e non il worker Python.

Per attivarlo: verificare il deploy Preview Git del nuovo commit su `main`;
verificare che il dominio stabile punti a **quel** deployment; applicare la sola migrazione 13
al database `fornitori_demo`; aprire il sito per provocare il cold start e la
registrazione del webhook; inviare `/start`, una targa demo, telefono, indirizzo,
problema e `STATO` in Telegram. Controllare i log senza registrare i token.
Il webhook controlla la firma del token segreto HTTP. Non cambiare il target
a Production e non usare un daemon (i daemon Vercel sono orientati a Production).

Nota: l'outbox notifiche accettazione/conclusione del vecchio bot Python
richiede un dispatcher dedicato; non è garantita in Preview con questo webhook.
