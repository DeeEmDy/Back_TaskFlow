BEGIN;

--Crear tablas.
CREATE TABLE IF NOT EXISTS public.databasechangelog (
    id VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    dateexecuted TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    orderexecuted INTEGER NOT NULL,
    exectype VARCHAR(10) NOT NULL,
    md5sum VARCHAR(35),
    description VARCHAR(255),
    comments VARCHAR(255),
    tag VARCHAR(255),
    liquibase VARCHAR(20),
    contexts VARCHAR(255),
    labels VARCHAR(255),
    deployment_id VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS public.databasechangeloglock (
    id INTEGER NOT NULL PRIMARY KEY,
    locked BOOLEAN NOT NULL,
    lockgranted TIMESTAMP WITHOUT TIME ZONE,
    lockedby VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS public.spring_session (
    primary_id VARCHAR(255) NOT NULL PRIMARY KEY,
    session_id VARCHAR(255),
    creation_time BIGINT,
    last_access_time BIGINT,
    max_inactive_interval INTEGER,
    expiry_time BIGINT,
    principal_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS public.spring_session_attributes (
    session_primary_id VARCHAR(255) NOT NULL,
    attribute_name VARCHAR(255) NOT NULL,
    attribute_bytes BIGINT,
    PRIMARY KEY (session_primary_id, attribute_name)
);

CREATE TABLE IF NOT EXISTS public.tbactivationtoken (
    id UUID NOT NULL PRIMARY KEY,
    token_value TEXT NOT NULL UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbimage (
    idimage SERIAL NOT NULL PRIMARY KEY,
    image_content TEXT,
    status BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbnotification (
    idnotification SERIAL NOT NULL PRIMARY KEY,
    notification_message VARCHAR(255),
    read BOOLEAN,
    dispatch_day TIMESTAMP WITHOUT TIME ZONE,
    id_user INTEGER,
    status BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbreport (
    idreport SERIAL NOT NULL PRIMARY KEY,
    id_user INTEGER,
    cut_off_task_date DATE,
    amount_task_assigned INTEGER,
    total_task_achieved INTEGER,
    total_task_failed INTEGER,
    average_total_task_ownled DOUBLE PRECISION,
    total_points_owned INTEGER,
    status BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbrol (
    idrol SERIAL NOT NULL PRIMARY KEY,
    rol_name VARCHAR(255),
    status BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbtask (
    idtask SERIAL NOT NULL PRIMARY KEY,
    id_user INTEGER,
    title VARCHAR(255),
    description_task VARCHAR(255),
    created_task_date DATE,
    expiration_task_date DATE,
    progress_task VARCHAR(20),
    finalization_task_date DATE,
    score INTEGER,
    status BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbuser (
    id_user SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR(255),
    first_surname VARCHAR(255),
    second_surname VARCHAR(255),
    id_card VARCHAR(255) UNIQUE,
    phone_number VARCHAR(255),
    id_image INTEGER,
    id_rol INTEGER,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    user_verified BOOLEAN,
    status BOOLEAN,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    activation_token VARCHAR(255),
    activation_token_expiration TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS public.tbuser_activation (
    user_id INTEGER NOT NULL,
    activation_token_id UUID NOT NULL,
    PRIMARY KEY (user_id, activation_token_id)
);

END;
