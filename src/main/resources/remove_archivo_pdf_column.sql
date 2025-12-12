-- Migration script to remove the archivo_pdf column from boletin table
-- This should be run manually on the database after deploying the code changes

-- Drop the archivo_pdf column
ALTER TABLE boletin DROP COLUMN IF EXISTS archivo_pdf;

-- Verification query (optional)
-- SELECT id_boletin, codigo_estudiante, id_periodo, fecha_generacion 
-- FROM boletin 
-- ORDER BY fecha_generacion DESC 
-- LIMIT 5;
