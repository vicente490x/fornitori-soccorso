# Documentazione Word · manifest versione 3.0

**Aggiornamento:** 06/11/2026 · **Sorgente:** branch `main` di [vicente490x/fornitori-soccorso](https://github.com/vicente490x/fornitori-soccorso).

I nove Word principali e cinque Word integrativi riportati qui sono stati **rigenerati dai rispettivi Markdown canonici** con impaginazione e titoli coerenti. Sono stati verificati i pacchetti ZIP/OOXML, ma non ancora la resa pagina per pagina in Microsoft Word.

| Documento Word | Sorgente canonica |
|---|---|
| [00_INDICE_DOCUMENTAZIONE.docx](./00_INDICE_DOCUMENTAZIONE.docx) | [Indice](../00_INDICE_GENERALE.md) |
| [BUR_FORNITORI.docx](./BUR_FORNITORI.docx) | [BUR](../requirements/BUR_FORNITORI.md) |
| [FUD_FORNITORI.docx](./FUD_FORNITORI.docx) | [FUD](../requirements/FUD_FORNITORI.md) |
| [TAD_FORNITORI.docx](./TAD_FORNITORI.docx) | [TAD](../architecture/TAD_FORNITORI.md) |
| [MANUALE_TECNICO.docx](./MANUALE_TECNICO.docx) | [Manuale tecnico](../manuals/MANUALE_TECNICO.md) |
| [MANUALE_UTENTE.docx](./MANUALE_UTENTE.docx) | [Manuale utente](../manuals/MANUALE_UTENTE.md) |
| [GUIDA_TEAM.docx](./GUIDA_TEAM.docx) | [Guida team](../team/README.md) |
| [PROJECT_MANAGEMENT_PLAN.docx](./PROJECT_MANAGEMENT_PLAN.docx) | [Piano di progetto](../management/PROJECT_MANAGEMENT_PLAN.md) |
| [AUDIT_SICUREZZA.docx](./AUDIT_SICUREZZA.docx) | [Audit backend](../security/BACKEND_SECURITY_AUDIT.md) |

## Word integrativi

| Documento Word | Sorgente canonica |
|---|---|
| [VERIFICA_DOCUMENTALE.docx](./VERIFICA_DOCUMENTALE.docx) | [QA e prove documentali](../quality/VERIFICA_DOCUMENTALE_2026-11-06.md) |
| [TRACCIABILITA_REQUISITI.docx](./TRACCIABILITA_REQUISITI.docx) | [Matrice requisiti](../requirements/REQUIREMENTS_TRACEABILITY.md) |
| [REGOLE_BUSINESS.docx](./REGOLE_BUSINESS.docx) | [Regole business](../requirements/BUSINESS_RULES.md) |
| [REGISTRO_RISCHI.docx](./REGISTRO_RISCHI.docx) | [Rischi](../management/RISK_REGISTER.md) |

L'indice generale `00_INDICE_DOCUMENTAZIONE.docx` è stato rigenerato per includere la nuova sezione «Consegna e verifica».

## Regola tariffaria applicabile

- **Fascia:** 20:00 incluse–08:00 escluse, fuso `Europe/Rome`, timestamp di accettazione (conferma docente ancora aperta).
- **Supplemento:** 25%; uscita €50 + €1,20/km, arrotondamento `HALF_UP` a due cifre.
- **Database:** `database/12_night_rate_20.sql`; Neon demo già configurato a 20:00–08:00.
- **Storico:** prezzi delle missioni chiuse non ricalcolati.

## Controlli prima di consegna/uso reale

1. Aprire in Microsoft Word alcuni documenti lunghi con tabelle (FUD, TAD) e verificarne paginazione/leggibilità; aggiornare eventuali indici generati.
2. Ripetere `./mvnw clean verify` e i test Python sul commit corrente con PostgreSQL 18 isolato.
3. Verificare sul browser la demo (401, 403, 503, riprova), e le integrazioni Telegram/mappe.
4. Risolvere o accettare formalmente le criticità in `docs/security/VULNERABILITY_REGISTER.md` e acquisire i requisiti/docente.

I PDF di presentazione e `docs/quality/registri.xlsx` sono snapshot precedenti: la loro revisione non è certificata da questa rigenerazione Word.
