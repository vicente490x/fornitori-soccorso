# Membro 3 — Chatbot

**Responsabile:** Marco Colucci · **Versione:** 2.1 · **Data:** 05/11/2026

## Scopo

Flusso guidato da parole chiave, senza dipendenza da AI, in [`chatbot/`](../../chatbot/README.md).
Il dialogo segue il foglio `DialoghiChatbot` del docente, con le differenze indicate sotto.

## Dialogo

Stati: `START → WAIT_PHONE → WAIT_LOCATION → WAIT_PROBLEM → START`, con `AIUTO`,
`ANNULLA` e `STATO` validi in ogni stato. Un dialogo inattivo scade dopo 30 minuti; 3 input
non validi lo fanno ricominciare.

1. Il cliente scrive la targa, che viene normalizzata.
2. Il bot chiede il telefono della persona sul posto, poi cerca la targa nel backend.
3. Con polizza verificata mostra nome e compagnia; se non è verificabile avvisa che la copertura
   è in controllo e prosegue comunque. In entrambi i casi chiede indirizzo o posizione.
4. Il cliente sceglie il problema con i tasti Telegram; il tipo di intervento e, per il traino,
   l'officina più vicina li decide il backend.
5. Il bot crea la missione: `nuova` con polizza verificata, `verifica_polizza` altrimenti.
6. `STATO` risponde sull'ultima richiesta aperta della chat con una frase comprensibile,
   mai con lo stato interno né con il nome dell'autista.
7. Errori gestiti: input non valido, servizio indisponibile, rate limit.

Il riconoscimento della targa vale 15 minuti. Se scade durante il dialogo, il bot lo rifà
una volta da solo e prosegue, senza far ripetere nulla al cliente.

## API

Il bot non accede al DB e usa `/api/v1/chat/*` con `FORNITORI_CHAT_TOKEN`. La ricerca
`GET /api/v1/clienti?search=...` resta riservata alla centrale.

## Esecuzione

In Preview Vercel (`main`), il chatbot è gestito dal webhook Java
`/api/v1/telegram/webhook` con conversazioni persistenti nel database Neon demo.
Il bot corretto è [@SoccorsoStradaleBot](https://t.me/SoccorsoStradaleBot):
il token `TELEGRAM_BOT_TOKEN` in Preview deve appartenere esattamente a questo bot.
Il server verifica lo username tramite `getMe` prima di registrare il webhook.
Il comando Python `python3 -m fornitori_chatbot --telegram --http` è destinato
al lavoro locale, non va eseguito contemporaneamente al webhook pubblico.
Le notifiche automatiche asincrone richiedono ancora un dispatcher dedicato.

## Limiti noti della demo

- **La sola targa identifica il cliente.** Chi la conosce vede nome e compagnia e può creare
  una richiesta. È una scelta della demo, allineata ai dialoghi del docente, e non è adatta
  a dati reali.
- Il dialogo chiede il telefono della persona sul posto; il dialogo 7 usa frasi diverse per
  non mostrare gli stati interni.
- Il dialogo 10, con Gemini, è facoltativo e non implementato.
- I dialoghi in corso stanno in RAM: un riavvio li azzera.

**Riferimenti:** [privacy](../architecture/SECURITY_DESIGN.md),
[contratti](./INTEGRATION_CONTRACTS.md), [sequenza](../diagrams/chatbot_sequence.mmd).
