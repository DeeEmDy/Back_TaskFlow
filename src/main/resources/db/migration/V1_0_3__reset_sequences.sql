-- V1.0.3_reset_sequences.sql
-- Reiniciar las secuencias de las tablas para que comiencen desde 1.

ALTER SEQUENCE public.tbimage_idimage_seq RESTART WITH 1;
ALTER SEQUENCE public.tbnotification_idnotification_seq RESTART WITH 1;
ALTER SEQUENCE public.tbreport_idreport_seq RESTART WITH 1;
ALTER SEQUENCE public.tbrol_idrol_seq RESTART WITH 1;
ALTER SEQUENCE public.tbtask_idtask_seq RESTART WITH 1;
ALTER SEQUENCE public.tbuser_id_user_seq RESTART WITH 1;
