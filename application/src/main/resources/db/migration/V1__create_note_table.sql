CREATE TABLE IF NOT EXISTS public.note
(
    id    bigint NOT NULL,
    title character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT note_pkey PRIMARY KEY (id)
)
