# Rapporto di verifica documentale e rilascio — 06/11/2026

**Repo:** [vicente490x/fornitori-soccorso](https://github.com/vicente490x/fornitori-soccorso) · **Target:** `main` · **Stato:** documentazione aggiornata, rilascio didattico/demo; NON certificato per attività di soccorso reali.

## Campo di applicazione

La base autorevole è composta da: [BUR](../requirements/BUR_FORNITORI.md), [FUD](../requirements/FUD_FORNITORI.md), [TAD](../architecture/TAD_FORNITORI.md), [regole business](../requirements/BUSINESS_RULES.md), [matrice requisiti](../requirements/REQUIREMENTS_TRACEABILITY.md), [manuale utente](../manuals/MANUALE_UTENTE.md), [manuale tecnico](../manuals/MANUALE_TECNICO.md), [audit sicurezza](../security/BACKEND_SECURITY_AUDIT.md), [report test sicurezza](../security/SECURITY_TEST_REPORT.md), [registro decisioni](../management/DECISION_LOG.md), [rischi](../management/RISK_REGISTER.md), [backlog](../agile/PRODUCT_BACKLOG.md) e [guida team](../team/README.md).

## Regola tariffaria in vigore

| Parametro | Valore |
|---|---|
| Fascia | 20:00 incluse fino alle 08:00 escluse |
| Fuso | Europe/Rome |
| Istante considerato dal software | Accettazione missione |
| Uscita | 50,00 € |
| Tariffa chilometrica | 1,20 €/km |
| Supplemento | 25% |
| Arrotondamento | HALF_UP a 2 decimali |
| Migrazione per database esistenti | `database/12_night_rate_20.sql` |
| Importi chiusi | Invariati |

Verifica applicata al database **Neon della demo**: lettura successiva all'update mostra `night_start = 20:00:00`, `night_end = 08:00:00` e `maggiorazione_notturna = 0.2500`.

## Evidenze riscontrate

- I file Java e SQL indicano che `PricingService` legge `night_start/night_end` da `pricing_config`.
- Il test parametrico `MissionLifecycleTests.nightSurchargeFollowsTheAcceptanceTime` contiene i casi 19:59, 20:00, 21:59, 22:00, 07:59, 08:00, con importi attesi 62,00 € / 77,50 € per 10 km.
- I nove Word principali sono stati rigenerati dai Markdown attuali, con verifica della struttura ZIP/OOXML (non equivalenza visiva in Word).
- Il precedente rilascio documentale risultava `READY` su Vercel; lo stato di ogni commit successivo deve essere verificato separatamente.
- Le interrogazioni al connettore GitHub non hanno restituito run GitHub Actions per i commit di documentazione consultati. **Non significa che i test falliscano, ma non possiamo considerarli superati.**

## Gate per la validazione finale

| Gate | Evidenza necessaria | Stato |
|---|---|---|
| QA-01 | `./mvnw clean verify` con PostgreSQL 18 isolato | Non verificato sul commit corrente |
| QA-02 | Test chatbot Python | Non verificati sul commit corrente |
| QA-03 | Accettazione delle 20:00 e 08:00 in HTTP reale | Da eseguire |
| QA-04 | Flussi browser centrale/autista: 401, 403, 503 e retry | Da eseguire |
| QA-05 | Apertura/impaginazione docx in Word/LibreOffice | Da validare |
| QA-06 | Requisiti e timestamp approvati dal docente | Da acquisire |
| QA-07 | Chiusura o accettazione formale vulnerabilità aperte | Da acquisire |
| QA-08 | Presentazioni PDF e vecchio workbook allineati | Non certificato |

## Nota sulle fonti dei test

Gli esiti di 176 test Java e 30 test bot nel [report storico](../security/SECURITY_TEST_REPORT.md) sono datati 05/11/2026 e non costituiscono una nuova esecuzione sul codice corrente.

## Criterio di completamento

La documentazione può essere consegnata come **baseline di progetto/dimostrazione** con questa nota di limitazioni, non come prova di collaudo finale per un impiego operativo reale. I Markdown sono la fonte autorevole. Nessuna approvazione del docente o risultato di test è stato inventato.
