# Gattini e gestione degli errori

Un piccolo gattino e un pulsante di recupero compaiono sugli errori temporanei (rete, 408, 5xx) nei caricamenti di dashboard, missioni, costi, area autista, storico e nella demo.

- Login/permessi, validazioni e conflitti (401/403/400/409) restano messaggi ordinari.
- I pulsanti ripetono il caricamento solo su clic; nessun retry automatico di operazioni mutanti.
- Tutto il testo dinamico usa textContent e non innerHTML.
- Nessuna modifica agli errori JSON API, alla sicurezza applicativa o al database.
- La mascotte SVG e il CSS sono ospitati localmente.

Verifica: node --check src/main/resources/static/app.js; node --check src/main/resources/static/demo.js; node --check src/main/resources/static/rescue-ui.js; node --test scripts/test-rescue-ui.mjs. Testare manualmente un 503 temporaneo e riprovare, poi 401 e 403.
