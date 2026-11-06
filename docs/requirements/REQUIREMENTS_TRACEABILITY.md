# Tracciabilità requisiti

**Codice:** FOR-TRACE-001 · **Versione:** 2.1 · **Data:** 05/11/2026
Gli ID `ST-###` sono chiavi introdotte in questa baseline, non ID di ticket originali.

| Requisito | Storia baseline | Componente | Endpoint/artefatto | Responsabili | Stato |
|---|---|---|---|---|---|
| BR-001 | ST-001 — consultare anagrafiche | RegistryController, RegistryService, DB | `/autisti`, `/mezzi`, `/compagnie`, `/clienti`, `/officine` | V. Minervini; F. Valeriano, G. Cangellosi, M. Sancilio | IMPLEMENTATO |
| BR-002 | ST-002 — accesso per ruolo | AuthController, SecurityConfig | `/auth/*` | V. Minervini | IMPLEMENTATO |
| BR-003 | ST-003 — creare e consultare missione | MissionService | `/missioni`, tabella `missions` | V. Minervini | IMPLEMENTATO |
| BR-004 | ST-004 — proporre e mantenere lo storico | MissionService, NearestDriverService | `/proposte`, `/proposta-automatica`, `mission_proposals` | V. Minervini | IMPLEMENTATO |
| BR-005 | ST-005 — timeout e concorrenza | MissionExpiryScheduler, lock e indici | `/accept`, `/reject` | V. Minervini | IMPLEMENTATO |
| BR-006 | ST-006 — chiusura e tariffa | PricingService, MissionService | `/complete` | V. Minervini | IMPLEMENTATO; fascia 20:00–08:00, timestamp da convalidare |
| BR-007 | ST-007 — rettifica motivata | MissionService, UI centrale | `/cost`, `mission_events` | V. Minervini; F. Valeriano, G. Cangellosi, M. Sancilio | IMPLEMENTATO |
| BR-008 | ST-008 — dashboard e storico personale | MissionService, UI | `/dashboard`, `/autista/*` | V. Minervini; F. Valeriano, G. Cangellosi, M. Sancilio | IMPLEMENTATO |
| BR-009 | ST-009 — richiesta da chatbot | Bot Python, ChatController | `/chat/lookup-targa`, `/chat/missioni`, `/chat/stato` | M. Colucci | IMPLEMENTATO; sola targa limite noto |
| BR-010 | ST-010 — notifica Telegram | Outbox, NotificationDispatcher | `/chat/notifiche/claim`, `/esito` | M. Sancilio, F. Valeriano, G. Cangellosi | IMPLEMENTATO |
| BR-011 | ST-011 — mappe e percorso | MapService, Leaflet | `/maps/route` | M. Colucci | IMPLEMENTATO |
| BR-012 | ST-012 — canale WhatsApp | — | Operazioni `designed` | M. Sancilio, F. Valeriano, G. Cangellosi | FUORI AMBITO |
| BR-013 | ST-013 — interfaccia web | Client statico | `src/main/resources/static/` | F. Valeriano, G. Cangellosi, M. Sancilio | IMPLEMENTATO |
| BR-014 | ST-014 — verifica polizza | MissionService, ChatService, UI centrale | `/verifica-polizza`, `/chat/lookup-targa` | V. Minervini; F. Valeriano, G. Cangellosi, M. Sancilio; M. Colucci | IMPLEMENTATO |
| BR-015 | ST-015 — assegnazione automatica e gestione manuale | AutoAssignmentService, NearestDriverService, MissionService | `/proposta-automatica`, `mission_events` | V. Minervini | IMPLEMENTATO |
| BR-016 | ST-016 — annullamento | MissionService, UI, notifiche | `/annulla`, `/cost`, `MISSION_CANCELLED` | V. Minervini; F. Valeriano, G. Cangellosi, M. Sancilio | IMPLEMENTATO |
| BR-017 | ST-017 — supporto e disponibilità | MissionService, CentreAlertService, UI | `/notes`, `/notes/{noteId}/gestita`, `/centrale/avvisi`, `/autista/disponibilita` | V. Minervini; F. Valeriano, G. Cangellosi, M. Sancilio | IMPLEMENTATO |

## Lacune

- Nessun ID di requisito del PDF originale è stato verificato: `BR-*` e `ST-*` sono locali.
- L'accettazione del docente su UI, mappa, canale e identità del richiedente è da confermare.
- Workbook filtrabile in [`../quality/registri.xlsx`](../quality/registri.xlsx).
