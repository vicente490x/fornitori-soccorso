-- Migrazione 12 / 06-11-2026: tariffa notturna dalle 20:00 incluse alle 08:00 escluse.
-- Additiva e idempotente: aggiorna solo la configurazione tariffaria, mai i costi delle missioni già chiuse.
-- Preserva lo storico della migrazione 10 (22:00) anziché modificarla retroattivamente.
BEGIN;
UPDATE pricing_config
SET night_start = TIME '20:00'
WHERE id = 1 AND night_start IS DISTINCT FROM TIME '20:00';
COMMIT;
