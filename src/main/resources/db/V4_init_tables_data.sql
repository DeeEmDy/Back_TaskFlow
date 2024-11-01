BEGIN;

INSERT INTO public.tbrol (rol_name, status, created_at) VALUES 
('ROLE_ADMIN', true, now()),
('ROLE_NORMUSER', true, now());

INSERT INTO public.tbimage (image_content, status, created_at, updated_at) VALUES 
('data:image/jpeg;base64,...', true, now(), now());

INSERT INTO public.tbuser (name, first_surname, second_surname, id_card, phone_number, id_image, id_rol, email, password, user_verified, status, created_at, updated_at, activation_token, activation_token_expiration) VALUES 
('Dylan', 'Arias', 'Durán', '208190890', '60129860', 1, 1, 'dy-lanarias@hotmail.com', '123DylanAD456.', true, true, now(), now(), '94400530-9cd8-490d-8e74-7222c4752b58', now()),
('Dixon', 'Góngora', 'Muñoz', '402520449', '66406988', 1, 1, 'dixongongora13@gmail.com', '12345678', true, true, now(), now(), '94400530-9cd8-490d-8e74-7222c4752b58', now()),
('TestAdmin', 'FSAdmin', 'SSAdmin', '102180325', '85123315', 1, 1, 'testadmin@gmail.com', '12345678', true, true, now(), now(), '94400530-9cd8-490d-8e74-7222c4752b33', now()),
('TestNormUser', 'FSNormUser', 'SSNormUser', '201210255', '60153312', 1, 1, 'testnormuser@gmail.com', '12345678', true, true, now(), now(), '94400530-9cd8-490d-8e74-7222c475c124', now());

INSERT INTO public.tbtask (id_user, title, description_task, created_task_date, expiration_task_date, progress_task, finalization_task_date, score, status, created_at, updated_at) VALUES
(1, 'Tarea 1', 'Descripcion de la tarea 1', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(1, 'Tarea 2', 'Descripcion de la tarea 2', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(2, 'Tarea 3', 'Descripcion de la tarea 3', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(2, 'Tarea 4', 'Descripcion de la tarea 4', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(1, 'Tarea 5', 'Descripcion de la tarea 5', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(2, 'Tarea 6', 'Descripcion de la tarea 6', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(2, 'Tarea 7', 'Descripcion de la tarea 7', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(1, 'Tarea 8', 'Descripcion de la tarea 8', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(1, 'Tarea 9', 'Descripcion de la tarea 9', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now()),
(2, 'Tarea 10', 'Descripcion de la tarea 10', '2021-08-01', '2021-08-10', 'Haciendo', null, 0, true, now(), now());

INSERT INTO public.tbreport (id_user, cut_off_task_date, amount_task_assigned, total_task_achieved, total_task_failed, average_total_task_ownled, total_points_owned, status, created_at, updated_at) VALUES
(1, '2021-08-01', 5, 3, 2, 60.5, 30, true, now(), now()),
(2, '2021-08-01', 4, 2, 2, 50.0, 20, true, now(), now()),
(1, '2021-08-01', 6, 4, 2, 70.0, 35, true, now(), now()),
(2, '2021-08-01', 3, 2, 1, 33.5, 15, true, now(), now()),
(2, '2021-08-01', 5, 3, 2, 40.0, 25, true, now(), now()),
(1, '2021-08-01', 7, 5, 2, 75.0, 40, true, now(), now()),
(1, '2021-08-01', 2, 1, 1, 20.0, 10, true, now(), now()),
(2, '2021-08-01', 4, 3, 1, 30.0, 20, true, now(), now()),
(1, '2021-08-01', 6, 4, 2, 60.0, 30, true, now(), now()),
(2, '2021-08-01', 5, 4, 1, 55.0, 25, true, now(), now());


END;
