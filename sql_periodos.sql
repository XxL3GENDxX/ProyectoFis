-- Script para insertar períodos ejemplo
-- Los períodos son semestrales

INSERT INTO periodo (nombrePeriodo, fechaInicio, fechaFin) 
VALUES 
    ('2023-1', '2023-01-15', '2023-06-30'),
    ('2023-2', '2023-08-01', '2023-12-31'),
    ('2024-1', '2024-01-15', '2024-06-30'),
    ('2024-2', '2024-08-01', '2024-12-31'),
    ('2025-1', '2025-01-15', '2025-06-30');

-- Verificar que se insertaron correctamente
SELECT * FROM periodo ORDER BY nombrePeriodo DESC;
