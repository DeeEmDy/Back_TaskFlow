-- Agregar restricciones de claves foráneas.
ALTER TABLE public.tbnotification
    ADD CONSTRAINT fk_notification_user FOREIGN KEY (id_user) REFERENCES public.tbuser (id_user);

ALTER TABLE public.tbreport
    ADD CONSTRAINT fk_report_user FOREIGN KEY (id_user) REFERENCES public.tbuser (id_user);

ALTER TABLE public.tbtask
    ADD CONSTRAINT fk_task_user FOREIGN KEY (id_user) REFERENCES public.tbuser (id_user);

ALTER TABLE public.tbuser
    ADD CONSTRAINT fk_user_image FOREIGN KEY (id_image) REFERENCES public.tbimage (idimage);

ALTER TABLE public.tbuser
    ADD CONSTRAINT fk_user_rol FOREIGN KEY (id_rol) REFERENCES public.tbrol (idrol);

ALTER TABLE public.tbuser_activation
    ADD CONSTRAINT tbuser_activation_activation_token_id_fkey FOREIGN KEY (activation_token_id) REFERENCES public.tbactivationtoken (id) ON DELETE CASCADE;

ALTER TABLE public.tbuser_activation
    ADD CONSTRAINT tbuser_activation_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.tbuser (id_user) ON DELETE CASCADE;

