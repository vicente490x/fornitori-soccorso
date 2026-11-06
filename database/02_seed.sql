-- FORNITORI / seed storico da Excel del docente, snapshot 28-10-2026
-- Password di demo in Excel sostituite con hash BCrypt generati individualmente.
-- I record storici sono is_fixture=TRUE: lo scheduler NON li modifica.
BEGIN;

INSERT INTO insurance_companies (id, nome, telefono_centrale, email_sinistri) VALUES
  (1, 'Assicurazione Demo S.p.A.', '800 100 200', 'centrale@assicurazionedemo.example'),
  (2, 'Polizza Sicura Assicurazioni', '800 200 300', 'sinistri@polizzasicura.example'),
  (3, 'Tutela Auto S.r.l.', '800 300 400', 'assistenza@tutelaauto.example'),
  (4, 'Strada Serena Mutua', '800 400 500', 'soccorso@stradaserena.example')
ON CONFLICT (id) DO NOTHING;


INSERT INTO app_users (id, email, password_hash, ruolo, nome) VALUES
  (1, 'responsabile@demo.it', '$2y$10$rgt2uDDJTkIg8R/uT.QQ1.Z2FgKAGd7XZTnBHO43RFPulzNfzDlTq', 'responsabile', 'Maria Rossi'),
  (2, 'centrale2@demo.it', '$2y$10$L3m4fNXupxhOvFNQ6xd5FuocTpiqX0EyprLxYRWVKOATymp4ts8wu', 'responsabile', 'Paolo Ferri'),
  (3, 'autista1@demo.it', '$2y$10$Sebr4wS0dJkzy8cZtsOv/u2i8qPFh2pC64XaWVbrMoaSSUfeobHMi', 'autista', 'Luca Bianchi'),
  (4, 'autista2@demo.it', '$2y$10$JgBpn1k5mPnzJS0C3qmON.etUESxjewI77hqrU34FO7yUP9DeFJPK', 'autista', 'Giuseppe De Palma'),
  (5, 'autista3@demo.it', '$2y$10$xunuIc4vj4FyvVB6knYBaO24Q5GLda4UUmtWPLf5T.mXb7/eHPR/6', 'autista', 'Antonio Lorusso'),
  (6, 'autista4@demo.it', '$2y$10$nBJdfLT.O/J9AlM7qbXWD.8mJYok7zKvUUgvQt/x9gNssqY7sQQYm', 'autista', 'Francesco Amoruso'),
  (7, 'autista5@demo.it', '$2y$10$KMqZmG9N.lkxLp8mZlxYa.KI8yMrXeC34gYWOsiKSBY1RMpsuprrS', 'autista', 'Vito Caputo'),
  (8, 'autista6@demo.it', '$2y$10$zA5ByciAHIFpDMZjXRKPzeLnbbjoqETW2VK3NqpJo/wL4mTZ9axW6', 'autista', 'Michele Spadavecchia'),
  (9, 'autista7@demo.it', '$2y$10$Rl0UIL1EnJ34/Y/PVPRtguhn51evPtEgdXJsDJxy26OTBATSJcD9q', 'autista', 'Nicola Minervini'),
  (10, 'autista8@demo.it', '$2y$10$odj.pEzuL/B80M0fEqrKaeeu9YXaJgsWn/zPrggYJTLU5CiCpfaSK', 'autista', 'Domenico Azzollini')
ON CONFLICT (id) DO NOTHING;


INSERT INTO vehicles (id, targa, tipologia, portata_t, base_indirizzo, base_citta, base_lat, base_lng) VALUES
  (1, 'FZ 412 KT', 'Carro attrezzi leggero (fino a 3,5 t)', 3.5, 'Via dei Portuali 12 (indirizzo di esempio)', 'Molfetta', 41.2003, 16.5978),
  (2, 'GA 777 LP', 'Carro attrezzi leggero (fino a 3,5 t)', 3.5, 'Via Terlizzi 88 (indirizzo di esempio)', 'Molfetta', 41.2003, 16.5978),
  (3, 'FX 920 MB', 'Carro attrezzi leggero (fino a 3,5 t)', 3.5, 'Via Bari 41 (indirizzo di esempio)', 'Giovinazzo', 41.1872, 16.6697),
  (4, 'GB 305 CD', 'Carro attrezzi leggero (fino a 3,5 t)', 3.5, 'Via Palo 15 (indirizzo di esempio)', 'Bitonto', 41.1083, 16.6914),
  (5, 'FW 118 RS', 'Carro attrezzi pesante (oltre 3,5 t)', 12, 'Via Andria 203 (indirizzo di esempio)', 'Trani', 41.2739, 16.4174),
  (6, 'GC 644 ZE', 'Carro attrezzi leggero (fino a 3,5 t)', 3.5, 'Via Corato 7 (indirizzo di esempio)', 'Ruvo di Puglia', 41.1168, 16.4858),
  (7, 'FY 251 HN', 'Furgone officina mobile', 3.5, 'Via Bovio 60 (indirizzo di esempio)', 'Bisceglie', 41.241, 16.5017),
  (8, 'GD 089 QA', 'Carro attrezzi leggero (fino a 3,5 t)', 3.5, 'Via Napoli 120 (indirizzo di esempio)', 'Bari', 41.1171, 16.8719)
ON CONFLICT (id) DO NOTHING;


INSERT INTO drivers (id, user_id, nome, telefono, vehicle_id, stato_seed, zona) VALUES
  (1, 3, 'Luca Bianchi', '+39 331 010 1111', 1, 'disponibile', 'Molfetta'),
  (2, 4, 'Giuseppe De Palma', '+39 332 020 1222', 2, 'disponibile', 'Molfetta'),
  (3, 5, 'Antonio Lorusso', '+39 333 030 1333', 3, 'disponibile', 'Giovinazzo'),
  (4, 6, 'Francesco Amoruso', '+39 334 040 1444', 4, 'disponibile', 'Bitonto'),
  (5, 7, 'Vito Caputo', '+39 335 050 1555', 5, 'disponibile', 'Trani'),
  (6, 8, 'Michele Spadavecchia', '+39 336 060 1666', 6, 'disponibile', 'Ruvo di Puglia'),
  (7, 9, 'Nicola Minervini', '+39 337 070 1777', 7, 'disponibile', 'Bisceglie'),
  (8, 10, 'Domenico Azzollini', '+39 338 080 1888', 8, 'disponibile', 'Bari')
ON CONFLICT (id) DO NOTHING;


INSERT INTO customers (id, nome, telefono, targa, targa_norm, modello_veicolo, company_id, numero_polizza, scadenza_polizza, citta_residenza) VALUES
  (1, 'Lucia Gallo', '+39 384 624 2681', 'PW 660 SW', 'PW660SW', 'Audi A3', 2, 'PS-2026-730226', DATE '2027-10-18', 'Corato'),
  (2, 'Mario Martino', '+39 330 213 5706', 'ZS 795 LG', 'ZS795LG', 'Citroen C3', 1, 'AD-2026-571497', DATE '2027-01-27', 'Bisceglie'),
  (3, 'Vincenzo Fontana', '+39 368 863 9414', 'VV 399 KR', 'VV399KR', 'Toyota Yaris', 1, 'AD-2026-858237', DATE '2027-06-03', 'Andria'),
  (4, 'Mario Fontana', '+39 366 572 7917', 'SD 535 TX', 'SD535TX', 'Fiat Ducato (furgone)', 1, 'AD-2026-518144', DATE '2027-10-18', 'Modugno'),
  (5, 'Maddalena Lombardi', '+39 373 677 8851', 'EB 821 TZ', 'EB821TZ', 'Volkswagen Golf', 1, 'AD-2026-703472', DATE '2027-04-25', 'Castellana Grotte'),
  (6, 'Cosimo Romano', '+39 381 334 2999', 'VJ 673 NZ', 'VJ673NZ', 'Opel Corsa', 4, 'SS-2026-807058', DATE '2027-08-23', 'Gravina in Puglia'),
  (7, 'Maddalena Mancini', '+39 371 543 4318', 'BE 771 XR', 'BE771XR', 'Iveco Daily (furgone)', 3, 'TA-2026-972249', DATE '2027-05-01', 'Monopoli'),
  (8, 'Vincenzo Marino', '+39 353 809 8955', 'PV 779 BK', 'PV779BK', 'Jeep Renegade', 4, 'SS-2026-636102', DATE '2027-08-26', 'Conversano'),
  (9, 'Lucia Vitale', '+39 385 365 8420', 'CY 372 SM', 'CY372SM', 'Volkswagen Golf', 2, 'PS-2026-546459', DATE '2027-12-16', 'Trani'),
  (10, 'Nunzio Romano', '+39 377 114 2510', 'CX 912 GH', 'CX912GH', 'Volkswagen Golf', 1, 'AD-2026-573149', DATE '2027-10-18', 'Bari'),
  (11, 'Leonardo Lombardi', '+39 365 258 1084', 'SE 658 HS', 'SE658HS', 'Toyota Yaris', 3, 'TA-2026-796864', DATE '2027-10-19', 'Altamura'),
  (12, 'Onofrio Esposito', '+39 373 784 3623', 'TP 499 ET', 'TP499ET', 'Peugeot 208', 2, 'PS-2026-921494', DATE '2027-10-10', 'Modugno'),
  (13, 'Mario Bruno', '+39 369 175 1841', 'AA 622 SL', 'AA622SL', 'Opel Corsa', 3, 'TA-2026-777665', DATE '2027-06-25', 'Conversano'),
  (14, 'Grazia Bruno', '+39 383 577 3797', 'PX 444 RX', 'PX444RX', 'Dacia Sandero', 2, 'PS-2026-719982', DATE '2027-09-07', 'Bitonto'),
  (15, 'Leonardo Serra', '+39 324 300 3840', 'BR 988 YG', 'BR988YG', 'Fiat Ducato (furgone)', 1, 'AD-2026-779719', DATE '2027-09-27', 'Molfetta'),
  (16, 'Pasquale Pellegrino', '+39 347 904 9468', 'FN 379 PV', 'FN379PV', 'Nissan Qashqai', 2, 'PS-2026-268181', DATE '2027-11-28', 'Bitonto'),
  (17, 'Teresa Costa', '+39 343 452 3862', 'PK 578 FL', 'PK578FL', 'Iveco Daily (furgone)', 3, 'TA-2026-586450', DATE '2027-08-25', 'Monopoli'),
  (18, 'Addolorata Bruno', '+39 387 915 9596', 'GH 105 CR', 'GH105CR', 'Jeep Renegade', 3, 'TA-2026-955797', DATE '2027-02-28', 'Castellana Grotte'),
  (19, 'Leonardo Greco', '+39 323 460 3568', 'VG 212 XJ', 'VG212XJ', 'Peugeot 208', 3, 'TA-2026-689302', DATE '2027-11-19', 'Andria'),
  (20, 'Isabella Lombardi', '+39 391 235 2662', 'DZ 161 MF', 'DZ161MF', 'Volkswagen Golf', 3, 'TA-2026-201114', DATE '2027-07-06', 'Altamura'),
  (21, 'Grazia Lombardi', '+39 395 957 2047', 'JC 114 PJ', 'JC114PJ', 'Nissan Qashqai', 3, 'TA-2026-429883', DATE '2027-03-09', 'Molfetta'),
  (22, 'Anna Bruno', '+39 350 123 6077', 'WM 422 RX', 'WM422RX', 'Fiat 500', 1, 'AD-2026-198429', DATE '2027-11-18', 'Modugno'),
  (23, 'Grazia Martino', '+39 347 708 8580', 'MT 797 JN', 'MT797JN', 'Opel Corsa', 4, 'SS-2026-808752', DATE '2027-02-22', 'Palo del Colle'),
  (24, 'Anna Conti', '+39 323 388 6415', 'PL 699 AV', 'PL699AV', 'Jeep Renegade', 3, 'TA-2026-862447', DATE '2027-02-10', 'Trani'),
  (25, 'Leonardo Russo', '+39 334 266 3010', 'MX 763 YK', 'MX763YK', 'Fiat Panda', 1, 'AD-2026-139131', DATE '2027-03-17', 'Castellana Grotte'),
  (26, 'Isabella Romano', '+39 365 525 7293', 'HE 287 VA', 'HE287VA', 'Citroen C3', 4, 'SS-2026-350930', DATE '2027-07-22', 'Bisceglie'),
  (27, 'Pasquale Ricci', '+39 322 116 5535', 'EY 707 CF', 'EY707CF', 'Fiat 500', 4, 'SS-2026-538178', DATE '2027-05-11', 'Altamura'),
  (28, 'Carmela Gallo', '+39 386 872 6201', 'HS 159 HZ', 'HS159HZ', 'Iveco Daily (furgone)', 1, 'AD-2026-789824', DATE '2027-01-21', 'Terlizzi'),
  (29, 'Teresa Fontana', '+39 349 675 5518', 'RY 568 VS', 'RY568VS', 'Opel Corsa', 3, 'TA-2026-310742', DATE '2027-06-12', 'Trani'),
  (30, 'Rosa Conti', '+39 339 596 1087', 'PB 754 FC', 'PB754FC', 'Opel Corsa', 3, 'TA-2026-953282', DATE '2027-09-23', 'Palo del Colle'),
  (31, 'Leonardo Lombardi', '+39 337 831 2650', 'WK 686 BN', 'WK686BN', 'Citroen C3', 1, 'AD-2026-659326', DATE '2027-04-28', 'Gravina in Puglia'),
  (32, 'Maddalena Greco', '+39 385 207 2401', 'VJ 417 BW', 'VJ417BW', 'Peugeot 208', 2, 'PS-2026-868161', DATE '2027-07-11', 'Ruvo di Puglia'),
  (33, 'Onofrio Costa', '+39 389 392 6852', 'YA 967 XY', 'YA967XY', 'Fiat Ducato (furgone)', 2, 'PS-2026-507489', DATE '2027-04-24', 'Castellana Grotte'),
  (34, 'Vincenzo Longo', '+39 341 839 9457', 'EF 129 DT', 'EF129DT', 'Fiat Panda', 1, 'AD-2026-288348', DATE '2027-06-18', 'Palo del Colle'),
  (35, 'Giovanni Fontana', '+39 358 321 9718', 'ED 434 SD', 'ED434SD', 'Lancia Ypsilon', 2, 'PS-2026-728617', DATE '2027-10-08', 'Modugno'),
  (36, 'Carmela Martino', '+39 358 763 2925', 'VT 352 MZ', 'VT352MZ', 'Audi A3', 3, 'TA-2026-597424', DATE '2027-08-04', 'Palo del Colle'),
  (37, 'Giovanni Esposito', '+39 331 927 9107', 'FL 542 TV', 'FL542TV', 'Jeep Renegade', 3, 'TA-2026-378191', DATE '2027-11-08', 'Barletta'),
  (38, 'Cosimo Serra', '+39 360 280 4219', 'MP 951 WN', 'MP951WN', 'Peugeot 208', 4, 'SS-2026-330865', DATE '2027-12-12', 'Terlizzi'),
  (39, 'Addolorata Leone', '+39 336 541 5384', 'JR 369 EV', 'JR369EV', 'Ford Fiesta', 3, 'TA-2026-976238', DATE '2027-02-11', 'Corato'),
  (40, 'Grazia Romano', '+39 391 890 3019', 'YN 509 AV', 'YN509AV', 'Jeep Renegade', 3, 'TA-2026-407724', DATE '2027-10-15', 'Castellana Grotte')
ON CONFLICT (id) DO NOTHING;


INSERT INTO workshops (id, nome, indirizzo, citta, lat, lng) VALUES
  (1, 'Officina Fratelli Gadaleta', 'Via Fornari 25 (indirizzo di esempio)', 'Molfetta', 41.2003, 16.5978),
  (2, 'Autocarrozzeria Adriatica', 'Via Giovinazzo 9 (indirizzo di esempio)', 'Molfetta', 41.2003, 16.5978),
  (3, 'Centro Revisioni Bitonto', 'Via Traetta 70 (indirizzo di esempio)', 'Bitonto', 41.1083, 16.6914),
  (4, 'Officina Meccanica Trani Nord', 'Via Malcangi 112 (indirizzo di esempio)', 'Trani', 41.2739, 16.4174),
  (5, 'Autofficina Murgia Service', 'Via Lama 3 (indirizzo di esempio)', 'Ruvo di Puglia', 41.1168, 16.4858),
  (6, 'Officina Levante', 'Via Napoli 300 (indirizzo di esempio)', 'Bari', 41.1171, 16.8719)
ON CONFLICT (id) DO NOTHING;


INSERT INTO pricing_config(id,uscita_eur,tariffa_km_eur,maggiorazione_notturna,night_start,night_end,timeout_proposta_min)
VALUES (1,50.00,1.20,0.25,'20:00','08:00',10)
ON CONFLICT (id) DO NOTHING;


INSERT INTO missions (id, codice, customer_id, driver_id, stato, tipo_intervento, canale_origine, descrizione_problema, origine_indirizzo, origine_lat, origine_lng, workshop_id, km_stimati, km_effettivi, creata_il, proposta_il, scadenza_il, accettata_il, chiusa_il, notturno, costo_calcolato, costo_finale, is_fixture) VALUES
  (1, 'M-2026-00001', 12, 8, 'eseguita', 'traino', 'whatsapp', 'Rottura cinghia servizi', 'Via Dante Alighieri 17, Modugno', 41.06987, 16.79852, 2, 15.5, 14.6, (TIMESTAMP '2026-10-02 10:43:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 10:53:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 11:03:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 10:54:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 12:17:00' AT TIME ZONE 'Europe/Rome'), FALSE, 67.52, 67.52, TRUE),
  (2, 'M-2026-00002', 24, 6, 'eseguita', 'traino', 'whatsapp', 'Foratura, ruota di scorta assente', 'Via Giuseppe Mazzini 20, Trani', 41.28942, 16.40108, 2, 36.6, 38.4, (TIMESTAMP '2026-10-02 20:00:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 20:11:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 20:21:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 20:16:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 21:43:00' AT TIME ZONE 'Europe/Rome'), TRUE, 120.1, 120.1, TRUE),
  (3, 'M-2026-00003', 26, 8, 'scaduta', 'traino', 'whatsapp', 'Carburante esaurito', 'Corso Umberto I 108, Bisceglie', 41.22138, 16.50254, 4, 47.4, NULL, (TIMESTAMP '2026-10-04 10:35:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-04 10:43:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-04 10:53:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (4, 'M-2026-00004', 20, 8, 'eseguita', 'riparazione_sul_posto', 'centrale', 'Auto non parte, batteria scarica', 'Via Nazionale 57, Altamura', 40.81301, 16.57091, NULL, 19.8, 18.9, (TIMESTAMP '2026-10-05 19:40:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-05 19:51:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-05 20:01:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-05 19:53:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-05 20:50:00' AT TIME ZONE 'Europe/Rome'), FALSE, 72.68, 72.68, TRUE),
  (5, 'M-2026-00005', 26, 1, 'eseguita', 'riparazione_sul_posto', 'whatsapp', 'Freni bloccati', 'Via Garibaldi 20, Bisceglie', 41.24231, 16.51075, NULL, 30.1, 32, (TIMESTAMP '2026-10-06 07:09:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-06 07:21:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-06 07:31:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-06 07:22:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-06 08:42:00' AT TIME ZONE 'Europe/Rome'), TRUE, 110.5, 110.5, TRUE),
  (6, 'M-2026-00006', 22, 6, 'non_accettata', 'traino', 'whatsapp', 'Freni bloccati', 'Via Vittorio Veneto 85, Modugno', 41.06477, 16.79029, 3, 8.7, NULL, (TIMESTAMP '2026-10-07 20:32:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-07 20:35:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-07 20:45:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (7, 'M-2026-00007', 8, 2, 'scaduta', 'traino', 'whatsapp', 'Carburante esaurito', 'Via Giuseppe Mazzini 52, Conversano', 40.97847, 17.12661, 2, 15.9, NULL, (TIMESTAMP '2026-10-09 10:33:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-09 10:39:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-09 10:49:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (8, 'M-2026-00008', 11, 4, 'scaduta', 'traino', 'centrale', 'Auto non parte, batteria scarica', 'Via Alcide De Gasperi 15, Altamura', 40.84457, 16.53475, 5, 34.7, NULL, (TIMESTAMP '2026-10-13 07:29:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-13 07:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-13 07:51:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (9, 'M-2026-00009', 8, 3, 'eseguita', 'traino', 'centrale', 'Surriscaldamento motore', 'Viale della Repubblica 68, Conversano', 40.96993, 17.11802, 4, 45.8, 45.4, (TIMESTAMP '2026-10-14 07:02:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 07:08:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 07:18:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 07:12:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 09:03:00' AT TIME ZONE 'Europe/Rome'), TRUE, 130.6, 130.6, TRUE),
  (10, 'M-2026-00010', 40, 5, 'non_accettata', 'traino', 'whatsapp', 'Frizione bloccata', 'Via Garibaldi 83, Castellana Grotte', 40.89112, 17.15151, 2, 44, NULL, (TIMESTAMP '2026-10-14 08:35:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 08:42:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 08:52:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (11, 'M-2026-00011', 13, 5, 'eseguita', 'traino', 'centrale', 'Chiavi chiuse in auto', 'Via Giuseppe Mazzini 39, Conversano', 40.9611, 17.11489, 5, 10.2, 13.1, (TIMESTAMP '2026-10-14 09:46:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 09:55:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 10:05:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 09:57:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 11:47:00' AT TIME ZONE 'Europe/Rome'), FALSE, 65.72, 65.72, TRUE),
  (12, 'M-2026-00012', 38, 7, 'non_accettata', 'traino', 'whatsapp', 'Auto non parte, batteria scarica', 'Via Nazionale 70, Terlizzi', 41.13131, 16.54541, 2, 29.5, NULL, (TIMESTAMP '2026-10-15 09:15:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 09:23:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 09:33:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (13, 'M-2026-00013', 36, 5, 'eseguita', 'traino', 'centrale', 'Spia motore e perdita di potenza', 'Via Nazionale 13, Palo del Colle', 41.0641, 16.70606, 3, 46.9, 47.5, (TIMESTAMP '2026-10-15 12:38:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 12:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 12:51:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 12:48:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 15:04:00' AT TIME ZONE 'Europe/Rome'), FALSE, 107, 107, TRUE),
  (14, 'M-2026-00014', 39, 2, 'eseguita', 'traino', 'centrale', 'Chiavi chiuse in auto', 'Via Alcide De Gasperi 42, Corato', 41.15702, 16.39253, 6, 11.2, 9.6, (TIMESTAMP '2026-10-15 17:34:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 17:40:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 17:50:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 17:45:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 18:34:00' AT TIME ZONE 'Europe/Rome'), FALSE, 61.52, 61.52, TRUE),
  (15, 'M-2026-00015', 28, 4, 'eseguita', 'traino', 'whatsapp', 'Foratura, ruota di scorta assente', 'Via Dante Alighieri 91, Terlizzi', 41.13854, 16.54398, 3, 33.2, 31.6, (TIMESTAMP '2026-10-15 21:38:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 21:43:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 21:53:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 21:45:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-16 00:06:00' AT TIME ZONE 'Europe/Rome'), TRUE, 109.9, 109.9, TRUE),
  (16, 'M-2026-00016', 3, 3, 'eseguita', 'traino', 'centrale', 'Auto non parte, batteria scarica', 'Zona industriale, Via dell''Artigianato 14, Andria', 41.20991, 16.3123, 6, 40, 38.5, (TIMESTAMP '2026-10-15 22:30:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 22:37:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 22:47:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 22:40:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 23:28:00' AT TIME ZONE 'Europe/Rome'), TRUE, 120.25, 120.25, TRUE),
  (17, 'M-2026-00017', 28, 2, 'eseguita', 'riparazione_sul_posto', 'whatsapp', 'Incidente senza feriti, vettura non marciante', 'Via Dante Alighieri 78, Terlizzi', 41.14425, 16.54881, NULL, 14.7, 14.7, (TIMESTAMP '2026-10-17 09:36:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 09:46:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 09:56:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 09:49:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 11:32:00' AT TIME ZONE 'Europe/Rome'), FALSE, 67.64, 67.64, TRUE),
  (18, 'M-2026-00018', 38, 6, 'scaduta', 'traino', 'whatsapp', 'Carburante esaurito', 'Corso Umberto I 54, Terlizzi', 41.13207, 16.52992, 2, 30, NULL, (TIMESTAMP '2026-10-17 12:58:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 13:05:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 13:15:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (19, 'M-2026-00019', 25, 1, 'non_accettata', 'riparazione_sul_posto', 'centrale', 'Chiavi chiuse in auto', 'Zona industriale, Via dell''Artigianato 87, Castellana Grotte', 40.89871, 17.18464, NULL, 35.2, NULL, (TIMESTAMP '2026-10-19 10:19:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-19 10:21:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-19 10:31:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (20, 'M-2026-00020', 22, 2, 'eseguita', 'traino', 'centrale', 'Surriscaldamento motore', 'Viale della Repubblica 76, Modugno', 41.10111, 16.76702, 2, 34.6, 35.7, (TIMESTAMP '2026-10-25 10:38:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-25 10:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-25 10:51:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-25 10:44:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-25 11:29:00' AT TIME ZONE 'Europe/Rome'), FALSE, 92.84, 92.84, TRUE),
  (21, 'M-2026-00021', 11, 4, 'eseguita', 'riparazione_sul_posto', 'whatsapp', 'Surriscaldamento motore', 'Via Vittorio Veneto 55, Altamura', 40.82723, 16.56806, NULL, 25.3, 28.3, (TIMESTAMP '2026-10-27 10:15:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 10:22:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 10:32:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 10:25:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 12:18:00' AT TIME ZONE 'Europe/Rome'), FALSE, 83.96, 83.96, TRUE),
  (22, 'M-2026-00022', 5, 6, 'eseguita', 'riparazione_sul_posto', 'centrale', 'Spia motore e perdita di potenza', 'Zona industriale, Via dell''Artigianato 111, Castellana Grotte', 40.89351, 17.15211, NULL, 39.2, 37.8, (TIMESTAMP '2026-10-27 20:55:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 20:59:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 21:09:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 21:07:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 23:02:00' AT TIME ZONE 'Europe/Rome'), TRUE, 119.2, 119.2, TRUE),
  (23, 'M-2026-00023', 36, 8, 'proposta', 'traino', 'whatsapp', 'Spia motore e perdita di potenza', 'Via Roma 22, Palo del Colle', 41.03997, 16.71149, 2, 30.4, NULL, (TIMESTAMP '2026-10-28 07:22:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 07:27:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 07:37:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (24, 'M-2026-00024', 1, 1, 'accettata', 'traino', 'centrale', 'Freni bloccati', 'Viale della Repubblica 81, Corato', 41.16428, 16.39418, 6, 30.7, NULL, (TIMESTAMP '2026-10-28 10:07:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 10:12:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 10:22:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 10:15:00' AT TIME ZONE 'Europe/Rome'), NULL, FALSE, NULL, NULL, TRUE),
  (25, 'M-2026-00025', 39, 7, 'eseguita', 'traino', 'whatsapp', 'Chiavi chiuse in auto', 'Via Dante Alighieri 47, Corato', 41.1447, 16.40332, 1, 47.6, 50.5, (TIMESTAMP '2026-10-28 10:48:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 11:00:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 11:10:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 11:02:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 13:04:00' AT TIME ZONE 'Europe/Rome'), FALSE, 110.6, 110.6, TRUE),
  (26, 'M-2026-00026', 2, 3, 'accettata', 'riparazione_sul_posto', 'centrale', 'Surriscaldamento motore', 'Via Alcide De Gasperi 110, Bisceglie', 41.23955, 16.49009, NULL, 13.1, NULL, (TIMESTAMP '2026-10-28 12:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 12:46:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 12:56:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 12:49:00' AT TIME ZONE 'Europe/Rome'), NULL, FALSE, NULL, NULL, TRUE),
  (27, 'M-2026-00027', 20, 4, 'eseguita', 'traino', 'whatsapp', 'Surriscaldamento motore', 'Viale della Repubblica 89, Altamura', 40.84543, 16.54298, 4, 17.5, 16.4, (TIMESTAMP '2026-10-28 17:26:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:37:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:47:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:40:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 19:21:00' AT TIME ZONE 'Europe/Rome'), FALSE, 69.68, 69.68, TRUE),
  (28, 'M-2026-00028', 31, 3, 'proposta', 'traino', 'whatsapp', 'Foratura, ruota di scorta assente', 'Via Vittorio Veneto 36, Gravina in Puglia', 40.80248, 16.40054, 1, 19.5, NULL, (TIMESTAMP '2026-10-28 17:33:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:38:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:48:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, TRUE),
  (29, 'M-2026-00029', 6, NULL, 'nuova', 'riparazione_sul_posto', 'whatsapp', 'Carburante esaurito', 'Via Nazionale 6, Gravina in Puglia', 40.80733, 16.43132, NULL, 46.8, NULL, (TIMESTAMP '2026-10-28 18:09:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE),
  (30, 'M-2026-00030', 21, NULL, 'nuova', 'traino', 'whatsapp', 'Frizione bloccata', 'Via Dante Alighieri 108, Molfetta', 41.20456, 16.58801, 1, 33.7, NULL, (TIMESTAMP '2026-10-28 19:29:00' AT TIME ZONE 'Europe/Rome'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, TRUE)
ON CONFLICT (id) DO NOTHING;


INSERT INTO mission_proposals (mission_id, driver_id, stato, proposta_il, scadenza_il, risposta_il, is_fixture) VALUES
  (1, 8, 'chiusa', (TIMESTAMP '2026-10-02 10:53:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 11:03:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 10:54:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (2, 6, 'chiusa', (TIMESTAMP '2026-10-02 20:11:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 20:21:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-02 20:16:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (3, 8, 'scaduta', (TIMESTAMP '2026-10-04 10:43:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-04 10:53:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (4, 8, 'chiusa', (TIMESTAMP '2026-10-05 19:51:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-05 20:01:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-05 19:53:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (5, 1, 'chiusa', (TIMESTAMP '2026-10-06 07:21:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-06 07:31:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-06 07:22:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (6, 6, 'non_accettata', (TIMESTAMP '2026-10-07 20:35:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-07 20:45:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (7, 2, 'scaduta', (TIMESTAMP '2026-10-09 10:39:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-09 10:49:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (8, 4, 'scaduta', (TIMESTAMP '2026-10-13 07:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-13 07:51:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (9, 3, 'chiusa', (TIMESTAMP '2026-10-14 07:08:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 07:18:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 07:12:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (10, 5, 'non_accettata', (TIMESTAMP '2026-10-14 08:42:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 08:52:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (11, 5, 'chiusa', (TIMESTAMP '2026-10-14 09:55:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 10:05:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-14 09:57:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (12, 7, 'non_accettata', (TIMESTAMP '2026-10-15 09:23:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 09:33:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (13, 5, 'chiusa', (TIMESTAMP '2026-10-15 12:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 12:51:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 12:48:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (14, 2, 'chiusa', (TIMESTAMP '2026-10-15 17:40:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 17:50:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 17:45:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (15, 4, 'chiusa', (TIMESTAMP '2026-10-15 21:43:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 21:53:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 21:45:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (16, 3, 'chiusa', (TIMESTAMP '2026-10-15 22:37:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 22:47:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-15 22:40:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (17, 2, 'chiusa', (TIMESTAMP '2026-10-17 09:46:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 09:56:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 09:49:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (18, 6, 'scaduta', (TIMESTAMP '2026-10-17 13:05:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-17 13:15:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (19, 1, 'non_accettata', (TIMESTAMP '2026-10-19 10:21:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-19 10:31:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (20, 2, 'chiusa', (TIMESTAMP '2026-10-25 10:41:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-25 10:51:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-25 10:44:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (21, 4, 'chiusa', (TIMESTAMP '2026-10-27 10:22:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 10:32:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 10:25:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (22, 6, 'chiusa', (TIMESTAMP '2026-10-27 20:59:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 21:09:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-27 21:07:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (23, 8, 'proposta', (TIMESTAMP '2026-10-28 07:27:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 07:37:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE),
  (24, 1, 'accettata', (TIMESTAMP '2026-10-28 10:12:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 10:22:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 10:15:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (25, 7, 'chiusa', (TIMESTAMP '2026-10-28 11:00:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 11:10:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 11:02:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (26, 3, 'accettata', (TIMESTAMP '2026-10-28 12:46:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 12:56:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 12:49:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (27, 4, 'chiusa', (TIMESTAMP '2026-10-28 17:37:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:47:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:40:00' AT TIME ZONE 'Europe/Rome'), TRUE),
  (28, 3, 'proposta', (TIMESTAMP '2026-10-28 17:38:00' AT TIME ZONE 'Europe/Rome'), (TIMESTAMP '2026-10-28 17:48:00' AT TIME ZONE 'Europe/Rome'), NULL, TRUE)
ON CONFLICT DO NOTHING;


SELECT setval(pg_get_serial_sequence('app_users','id'), GREATEST((SELECT MAX(id) FROM app_users),1), true);
SELECT setval(pg_get_serial_sequence('vehicles','id'), GREATEST((SELECT MAX(id) FROM vehicles),1), true);
SELECT setval(pg_get_serial_sequence('drivers','id'), GREATEST((SELECT MAX(id) FROM drivers),1), true);
SELECT setval(pg_get_serial_sequence('insurance_companies','id'), GREATEST((SELECT MAX(id) FROM insurance_companies),1), true);
SELECT setval(pg_get_serial_sequence('customers','id'), GREATEST((SELECT MAX(id) FROM customers),1), true);
SELECT setval(pg_get_serial_sequence('workshops','id'), GREATEST((SELECT MAX(id) FROM workshops),1), true);
SELECT setval(pg_get_serial_sequence('missions','id'), GREATEST((SELECT MAX(id) FROM missions),1), true);
COMMIT;