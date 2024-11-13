-- Eliminar restricciones de claves foráneas si existen.
ALTER TABLE IF EXISTS public.tbnotification DROP CONSTRAINT IF EXISTS fk_notification_user;
ALTER TABLE IF EXISTS public.tbreport DROP CONSTRAINT IF EXISTS fk_report_user;
ALTER TABLE IF EXISTS public.tbtask DROP CONSTRAINT IF EXISTS fk_task_user;
ALTER TABLE IF EXISTS public.tbuser DROP CONSTRAINT IF EXISTS fk_user_image;
ALTER TABLE IF EXISTS public.tbuser DROP CONSTRAINT IF EXISTS fk_user_rol;
ALTER TABLE IF EXISTS public.tbuser_activation DROP CONSTRAINT IF EXISTS tbuser_activation_activation_token_id_fkey;
ALTER TABLE IF EXISTS public.tbuser_activation DROP CONSTRAINT IF EXISTS tbuser_activation_user_id_fkey;

-- Eliminar tablas existentes en el orden correcto.
DROP TABLE IF EXISTS public.tbuser_activation CASCADE;
DROP TABLE IF EXISTS public.tbnotification CASCADE;
DROP TABLE IF EXISTS public.tbreport CASCADE;
DROP TABLE IF EXISTS public.tbtask CASCADE;
DROP TABLE IF EXISTS public.tbuser CASCADE;
DROP TABLE IF EXISTS public.tbimage CASCADE;
DROP TABLE IF EXISTS public.tbrol CASCADE;
DROP TABLE IF EXISTS public.tbactivationtoken CASCADE;
DROP TABLE IF EXISTS public.spring_session CASCADE;
DROP TABLE IF EXISTS public.spring_session_attributes CASCADE;
