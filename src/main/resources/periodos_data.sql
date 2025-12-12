INSERT INTO periodo (id_periodo, nombre_periodo, fecha_inicio, fecha_fin, activo) VALUES 
(1, '2025-I', '2025-01-15 00:00:00', '2025-06-15 23:59:59', true),
(2, '2025-II', '2025-07-15 00:00:00', '2025-11-30 23:59:59', false)
ON CONFLICT (id_periodo) DO NOTHING;
