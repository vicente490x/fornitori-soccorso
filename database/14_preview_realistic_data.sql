-- Preview RecuVeicol: arricchimento anagrafiche sintetiche (06/11/2026).
-- SOLO il database isolato fornitori_demo. Idempotente e non distruttivo.
-- Nomi originali clienti/operatori tratti dal seed storico, dove disponibili.
-- Tutte le persone, targhe, compagnie e i recapiti sono FINZIONI per la presentazione.
-- Non cambiare password, collegamenti fra record, storici fixture, tariffe o permessi.
BEGIN;
DO $realistic$
BEGIN
  IF current_database() <> 'fornitori_demo' THEN
    RAISE NOTICE '14_preview_realistic_data: ignorato fuori da fornitori_demo';
    RETURN;
  END IF;

  UPDATE app_users a SET
    nome = CASE WHEN a.nome ~* 'demo|prova|test' THEN v.nome ELSE a.nome END,
    email = CASE WHEN a.email ~* '@demo\\.(it|example)$' THEN v.email ELSE a.email END
  FROM (VALUES
    (1, 'Maria Rossi', 'centrale@recuveicol.example'),
    (2, 'Paolo Ferri', 'paolo.ferri@recuveicol.example'),
    (3, 'Luca Bianchi', 'luca.bianchi@recuveicol.example'),
    (4, 'Giuseppe De Palma', 'giuseppe.depalma@recuveicol.example'),
    (5, 'Antonio Lorusso', 'antonio.lorusso@recuveicol.example'),
    (6, 'Francesco Amoruso', 'francesco.amoruso@recuveicol.example'),
    (7, 'Vito Caputo', 'vito.caputo@recuveicol.example'),
    (8, 'Michele Spadavecchia', 'michele.spadavecchia@recuveicol.example'),
    (9, 'Nicola Minervini', 'nicola.minervini@recuveicol.example'),
    (10, 'Domenico Azzollini', 'domenico.azzollini@recuveicol.example'),
    (11, 'Elena Rinaldi', 'elena.rinaldi@recuveicol.example'),
    (12, 'Marco De Santis', 'marco.desantis@recuveicol.example')
  ) AS v(id,nome,email)
  WHERE a.id=v.id AND (a.nome ~* 'demo|prova|test' OR a.email ~* '@demo\\.(it|example)$');

  UPDATE drivers d SET nome=v.nome
  FROM (VALUES
    (1, 'Luca Bianchi'),
    (2, 'Giuseppe De Palma'),
    (3, 'Antonio Lorusso'),
    (4, 'Francesco Amoruso'),
    (5, 'Vito Caputo'),
    (6, 'Michele Spadavecchia'),
    (7, 'Nicola Minervini'),
    (8, 'Domenico Azzollini'),
    (9, 'Marco De Santis')
  ) AS v(id,nome)
  WHERE d.id=v.id AND d.nome ~* 'demo|prova|test';

  UPDATE customers c SET
    nome=v.nome,
    cognome=v.cognome
  FROM (VALUES
    (1, 'Lucia', 'Gallo'),
    (2, 'Mario', 'Martino'),
    (3, 'Vincenzo', 'Fontana'),
    (4, 'Mario', 'Fontana'),
    (5, 'Maddalena', 'Lombardi'),
    (6, 'Cosimo', 'Romano'),
    (7, 'Maddalena', 'Mancini'),
    (8, 'Vincenzo', 'Marino'),
    (9, 'Lucia', 'Vitale'),
    (10, 'Nunzio', 'Romano'),
    (11, 'Leonardo', 'Lombardi'),
    (12, 'Onofrio', 'Esposito'),
    (13, 'Mario', 'Bruno'),
    (14, 'Grazia', 'Bruno'),
    (15, 'Leonardo', 'Serra'),
    (16, 'Pasquale', 'Pellegrino'),
    (17, 'Teresa', 'Costa'),
    (18, 'Addolorata', 'Bruno'),
    (19, 'Leonardo', 'Greco'),
    (20, 'Isabella', 'Lombardi'),
    (21, 'Grazia', 'Lombardi'),
    (22, 'Anna', 'Bruno'),
    (23, 'Grazia', 'Martino'),
    (24, 'Anna', 'Conti'),
    (25, 'Leonardo', 'Russo'),
    (26, 'Isabella', 'Romano'),
    (27, 'Pasquale', 'Ricci'),
    (28, 'Carmela', 'Gallo'),
    (29, 'Teresa', 'Fontana'),
    (30, 'Rosa', 'Conti'),
    (31, 'Leonardo', 'Lombardi'),
    (32, 'Maddalena', 'Greco'),
    (33, 'Onofrio', 'Costa'),
    (34, 'Vincenzo', 'Longo'),
    (35, 'Giovanni', 'Fontana'),
    (36, 'Carmela', 'Martino'),
    (37, 'Giovanni', 'Esposito'),
    (38, 'Cosimo', 'Serra'),
    (39, 'Addolorata', 'Leone'),
    (40, 'Grazia', 'Romano')
  ) AS v(id,nome,cognome)
  WHERE c.id=v.id AND c.nome ~* '^cliente (demo|prova|test)';

  UPDATE customers
  SET nome='Andrea', cognome='Gatti', modello_veicolo='Ford Focus',
      citta_residenza='Molfetta',
      numero_polizza='PA-2026-7625294',
      targa='GH 831 RS', targa_norm='GH831RS'
  WHERE id=41 AND nome ~* 'demo|prova|test' AND targa_norm='QA7625294';

  UPDATE insurance_companies SET nome='Protezione Adriatica Assicurazioni',
    email_sinistri='assistenza@protezione-adriatica.example'
  WHERE id=1 AND nome='Assicurazione Demo S.p.A.';

  UPDATE vehicles SET base_indirizzo=replace(base_indirizzo, ' (indirizzo di esempio)', '')
  WHERE base_indirizzo LIKE '% (indirizzo di esempio)%';
  UPDATE vehicles SET targa='GJ 528 PM', base_indirizzo='Zona artigianale, Molfetta'
  WHERE id=9 AND targa='DEMO-AUT-09';

  UPDATE workshops SET indirizzo=replace(indirizzo, ' (indirizzo di esempio)', '')
  WHERE indirizzo LIKE '% (indirizzo di esempio)%';

  UPDATE missions SET codice='M-2026-00031',
    descrizione_problema='Guasto meccanico con perdita di potenza e necessità di traino'
  WHERE id=31 AND NOT is_fixture AND codice='DEMO-ATTIVA-001';
  UPDATE missions SET codice='M-2026-00032',
    descrizione_problema='Batteria scarica: richiesta assistenza per mancato avviamento'
  WHERE id=32 AND NOT is_fixture AND codice='DEMO-NUOVA-001';
  UPDATE missions SET descrizione_problema='Batteria completamente scarica dopo una lunga sosta'
  WHERE id=33 AND NOT is_fixture AND descrizione_problema ILIKE 'DEMO%';
  UPDATE missions SET descrizione_problema='Avviamento difficoltoso e verifica circuito di ricarica'
  WHERE id=34 AND NOT is_fixture AND descrizione_problema ILIKE 'DEMO%';
  UPDATE missions SET descrizione_problema='Mancato avviamento risolto prima dell’arrivo del soccorso',
    origine_indirizzo='Via Giovinazzo, Molfetta', targa_dichiarata='GH831RS'
  WHERE id=35 AND NOT is_fixture AND descrizione_problema ILIKE 'DEMO%';
  UPDATE missions SET origine_indirizzo='Via Terlizzi, Molfetta'
  WHERE id=37 AND NOT is_fixture AND origine_indirizzo='Via non lo so';

  UPDATE notifications_outbox
  SET payload=jsonb_set(payload::jsonb, '{codice}', to_jsonb('M-2026-00031'::text))
  WHERE mission_id=31 AND payload->>'codice'='DEMO-ATTIVA-001' AND stato='SKIPPED';
END
$realistic$;
COMMIT;
