# Product backlog FORNITORI

**Versione:** 2.1 · **Data:** 05/11/2026 · **Responsabile:** team

| ID | Priorità | Epic / storia | Responsabili | Dipendenze | Stato |
|---|---|---|---|---|---|
| B-01 | P0 | Seed, schema, auth e anagrafiche | Vincenzo Minervini | Dati del corso | Implementato |
| B-02 | P0 | Java 25, pipeline e contratti condivisi | Vincenzo Minervini | JDK 25 | Implementato |
| B-03 | P1 | Login e anagrafiche web | Francesco Valeriano, Giuseppe Cangellosi, Marika Sancilio | B-01 | Implementato |
| B-04 | P0 | API missione e dettaglio centrale | Vincenzo Minervini | B-01 | Implementato |
| B-05 | P0 | Proposta, accettazione, rifiuto, timeout e concorrenza | Vincenzo Minervini | B-04 | Implementato |
| B-06 | P0 | Chiusura, tariffa e dashboard | Vincenzo Minervini | B-05 | Implementato; fascia 20:00–08:00, timestamp da confermare col docente |
| B-07 | P1 | Chatbot guidato e creazione richiesta via API | Marco Colucci | API chat | Implementato; sola targa limite noto |
| B-08 | P1 | Geocodifica, percorso, km stimati ed effettivi distinti | Marco Colucci | B-04 | Implementato |
| B-09 | P1 | Outbox e notifiche Telegram con riserva persistente | Marika Sancilio, Francesco Valeriano, Giuseppe Cangellosi | B-05, B-06 | Implementato |
| B-10 | P0 | Sicurezza, allineamento e demo | Tutti | B-03–B-09 | Prova Telegram da telefono da ripetere; acceptance del docente da acquisire |
| B-11 | P1 | UI centrale e autista, avvisi, rettifica, correzione officina, storico proposte | Francesco Valeriano, Giuseppe Cangellosi, Marika Sancilio | API missioni | Implementato |
| B-12 | P1 | Mappa e percorso nel client | Marco Colucci | B-08 | Implementato |
| B-13 | P2 | Webhook e provider WhatsApp | Marika Sancilio, Francesco Valeriano, Giuseppe Cangellosi | — | Fuori ambito |
| B-14 | P2 | Gestione utenti da interfaccia | Vincenzo Minervini | — | Fuori ambito |
| B-15 | P0 | Operatività: assegnazione automatica, gestione manuale, verifica polizza, annullamento, supporto, disponibilità | Vincenzo Minervini | B-05, B-06 | Implementato |
| B-16 | P1 | Dashboard operativa, costi e schermate per l'operatività | Francesco Valeriano, Giuseppe Cangellosi, Marika Sancilio | B-15 | Implementato |
| B-17 | P1 | Bot: telefono sul posto, problema a pulsanti, polizza da verificare, avviso di annullamento | Marco Colucci | B-15 | Implementato; prova reale con Telegram da ripetere |

## Refinement

Ogni storia ha scenario utente, dati e permessi, acceptance, dipendenze e responsabile prima di entrare nello
Sprint. I cambi di API passano dal [contratto integrato](../team/INTEGRATION_CONTRACTS.md).
