# FORNITORI — indice generale e registro documentale

**Codice:** FOR-IDX-001 · **Versione:** 3.0 · **Data:** 06/11/2026 · **Responsabili:** team
**Stato:** baseline documentale, non approvata dal docente
**Repository:** [vicente490x/fornitori-soccorso](https://github.com/vicente490x/fornitori-soccorso)

## Per iniziare

- [Business Requirements (BUR)](./requirements/BUR_FORNITORI.md)
- [Functional Design (FUD)](./requirements/FUD_FORNITORI.md)
- [Technical Architecture (TAD)](./architecture/TAD_FORNITORI.md)
- [Sprint](./agile/README.md) · [backlog](./agile/PRODUCT_BACKLOG.md)
- [Matrice di tracciabilità](./requirements/REQUIREMENTS_TRACEABILITY.md)
- [Manuale utente](./manuals/MANUALE_UTENTE.md) · [Manuale tecnico](./manuals/MANUALE_TECNICO.md)
- [Guide del team](./team/) · [Word aggiornati](./word/) ([manifest](./word/README.md))
- [Checklist per l'ambiente reale](./deploy/CHECKLIST_AMBIENTE_REALE.md)
- [Presentazione](./presentazione/canva/LEGGIMI.md) · [Diagrammi](./diagrams/README.md)
- Note sulle modifiche, come record storico: [backend](./changes/BACKEND.md) · [frontend](./changes/FRONTEND.md) · [chatbot](./changes/CHATBOT.md)

## Catalogo

| Codice | Titolo | Categoria | Markdown autorevole | Word | Responsabili |
|---|---|---|---|---|---|
| FOR-IDX-001 | Indice generale e registro | Navigazione | `docs/00_INDICE_GENERALE.md` | `docs/word/00_INDICE_DOCUMENTAZIONE.docx` | Team |
| FOR-BUR-001 | Business Requirements | Requisiti | `docs/requirements/BUR_FORNITORI.md` | `docs/word/BUR_FORNITORI.docx` | Team, da confermare col docente |
| FOR-FUD-001 | Functional Design | Requisiti | `docs/requirements/FUD_FORNITORI.md` | `docs/word/FUD_FORNITORI.docx` | Team |
| FOR-TAD-001 | Technical Architecture | Architettura | `docs/architecture/TAD_FORNITORI.md` | `docs/word/TAD_FORNITORI.docx` | Vincenzo Minervini |
| FOR-PMP-001 | Piano gestione progetto | Management | `docs/management/PROJECT_MANAGEMENT_PLAN.md` | `docs/word/PROJECT_MANAGEMENT_PLAN.docx` | Team |
| FOR-TEAM-001 | Guide del team e contratti condivisi | Operativo | `docs/team/README.md` | `docs/word/GUIDA_TEAM.docx` | Membri 1-5 |
| FOR-USER-001 | Manuale utente | Manuale | `docs/manuals/MANUALE_UTENTE.md` | `docs/word/MANUALE_UTENTE.docx` | Frontend e chatbot |
| FOR-TECH-001 | Manuale tecnico | Manuale | `docs/manuals/MANUALE_TECNICO.md` | `docs/word/MANUALE_TECNICO.docx` | Vincenzo Minervini |
| FOR-SEC-002 | Audit di sicurezza del backend | Sicurezza | `docs/security/` | `docs/word/AUDIT_SICUREZZA.docx` | Vincenzo Minervini |
| FOR-TRACE-001 | Matrice requisiti | Tracciabilità | `docs/requirements/REQUIREMENTS_TRACEABILITY.md` | — | Team |
| FOR-RSK-001 | Registro rischi | Management | `docs/management/RISK_REGISTER.md` | — | Team |
| FOR-DEC-001 | Registro decisioni | Management | `docs/management/DECISION_LOG.md` | — | Team |
| FOR-DATA-001 | Matrici di lavoro | Workbook | `docs/quality/registri.xlsx` | — | Team |

## Fonti

| Fonte | Autorità | Limiti |
|---|---|---|
| `pom.xml`, codice Java e Python, configurazione | Implementazione presente | — |
| `api/openapi.yaml` | Contratto HTTP | Le operazioni `designed` sono fuori ambito |
| `database/*.sql` | Schema e dati dimostrativi | Da applicare in ordine |
| Specifiche PDF/Excel del docente | Fonte primaria prevista | Non presenti nel repository: requisiti da confermare |

## Word

L'indice dei Word è un campo aggiornabile: in Microsoft Word, clic destro sull'indice,
**Aggiorna campo → Aggiorna intero indice**.

## Aggiornamento tariffa notturna — 06/11/2026

- **Regola in vigore:** 20:00 incluse – 08:00 escluse, ora locale `Europe/Rome` all'accettazione, supplemento 25%.
- Seed `database/02_seed.sql` e migrazione per database già attivi `database/12_night_rate_20.sql` (non modifica gli importi chiusi).
- Canonici aggiornati: [regole di business](./requirements/BUSINESS_RULES.md), [FUD](./requirements/FUD_FORNITORI.md), [TAD](./architecture/TAD_FORNITORI.md), [manuale tecnico](./manuals/MANUALE_TECNICO.md), [manuale utente](./manuals/MANUALE_UTENTE.md), [registro decisioni](./management/DECISION_LOG.md).
- **Word principali:** 9 DOCX rigenerati il 06/11/2026 a partire dai Markdown canonici; struttura dei file verificata, ma apertura/impaginazione in Microsoft Word da collaudare.
- **PDF di presentazione e vecchio Excel:** sono materiali storici non rigenerati in repository. Non considerarli automaticamente allineati; usare i Markdown aggiornati, i nuovi Word e la [verifica documentale](./quality/VERIFICA_DOCUMENTALE_2026-11-06.md).

## Consegna e verifica

- [Manifest dei documenti Word](./word/README.md), 9 DOCX rigenerati.
- [Verifica documentale e release](./quality/VERIFICA_DOCUMENTALE_2026-11-06.md), con evidenze controllate e gate ancora aperti.
- [Casi di test della fascia 20:00–08:00](../src/test/java/it/exprivia/fornitori/MissionLifecycleTests.java); **non** sono dichiarati eseguiti su GitHub Actions dopo l'ultima modifica.
- [Registro rischi](./management/RISK_REGISTER.md) e [vulnerabilità aperte](./security/VULNERABILITY_REGISTER.md) per distinguere la demo dall'uso reale.
