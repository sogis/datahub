-- Generated from ili2pg and JobRunr. See docs/develop-manual-de.md.
\set ON_ERROR_STOP on
BEGIN;
--
-- PostgreSQL database dump
--

-- Dumped from database version 16.4 (Debian 16.4-1.pgdg110+1)
-- Dumped by pg_dump version 16.4 (Debian 16.4-1.pgdg110+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: agi_datahub_config_v1; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA agi_datahub_config_v1;


--
-- Name: agi_datahub_jobrunr_v1; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA agi_datahub_jobrunr_v1;


--
-- Name: agi_datahub_log_v1; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA agi_datahub_log_v1;


--
-- Name: t_ili2db_seq; Type: SEQUENCE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE SEQUENCE agi_datahub_config_v1.t_ili2db_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: core_apikey; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.core_apikey (
    t_id bigint DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq'::regclass) NOT NULL,
    apikey character varying(256) NOT NULL,
    createdat timestamp without time zone NOT NULL,
    revokedat timestamp without time zone,
    dateofexpiry timestamp without time zone,
    organisation_r bigint NOT NULL
);


--
-- Name: COLUMN core_apikey.apikey; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.apikey IS 'API-Key';


--
-- Name: COLUMN core_apikey.createdat; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.createdat IS 'Zeitpunkt der Erstellung.';


--
-- Name: COLUMN core_apikey.revokedat; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.revokedat IS 'Zeitpunkt des Invalidierens.';


--
-- Name: COLUMN core_apikey.dateofexpiry; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_apikey.dateofexpiry IS 'Zeitpunkt, an dem Key invalidiert wird.';


--
-- Name: core_operat; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.core_operat (
    t_id bigint DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq'::regclass) NOT NULL,
    aname character varying(512) NOT NULL,
    adescription character varying(1024),
    organisation_r bigint NOT NULL,
    theme_r bigint NOT NULL
);


--
-- Name: COLUMN core_operat.aname; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_operat.aname IS 'Name resp. ID des Operates, z.B. DMAV-2601 für die Gemeinde Solothurn im DMAV.';


--
-- Name: core_organisation; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.core_organisation (
    t_id bigint DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq'::regclass) NOT NULL,
    aname character varying(512) NOT NULL,
    auid character varying(15),
    arole character varying(20) NOT NULL,
    email character varying(512) NOT NULL
);


--
-- Name: COLUMN core_organisation.aname; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_organisation.aname IS 'Name der Organisation / Firma.';


--
-- Name: COLUMN core_organisation.auid; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_organisation.auid IS 'Unternehmens-Identifikationsnummer. Schweizweit eindeutig.';


--
-- Name: COLUMN core_organisation.arole; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_organisation.arole IS 'Rolle der Organisation (Admin oder User).';


--
-- Name: core_theme; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.core_theme (
    t_id bigint DEFAULT nextval('agi_datahub_config_v1.t_ili2db_seq'::regclass) NOT NULL,
    aname character varying(512) NOT NULL,
    config character varying(512),
    metaconfig character varying(512),
    expected_models character varying(2048),
    adescription character varying(1024)
);


--
-- Name: TABLE core_theme; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON TABLE agi_datahub_config_v1.core_theme IS 'TODO';


--
-- Name: COLUMN core_theme.aname; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_theme.aname IS 'Name / ID des Themas, z.B. DMAV';


--
-- Name: COLUMN core_theme.config; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_theme.config IS 'Name / ID der Config-Datei, die für die Validierung verwendet wird.';


--
-- Name: COLUMN core_theme.metaconfig; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_theme.metaconfig IS 'Name / ID der Meta-Config-Datei, die für die Validierung verwendet wird.';


--
-- Name: COLUMN core_theme.expected_models; Type: COMMENT; Schema: agi_datahub_config_v1; Owner: -
--

COMMENT ON COLUMN agi_datahub_config_v1.core_theme.expected_models IS 'Semicolon-separated names of permitted transfer models.';


--
-- Name: t_ili2db_attrname; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_attrname (
    iliname character varying(1024) NOT NULL,
    sqlname character varying(1024) NOT NULL,
    colowner character varying(1024) NOT NULL,
    target character varying(1024)
);


--
-- Name: t_ili2db_basket; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_basket (
    t_id bigint NOT NULL,
    dataset bigint,
    topic character varying(200) NOT NULL,
    t_ili_tid character varying(200),
    attachmentkey character varying(200) NOT NULL,
    domains character varying(1024)
);


--
-- Name: t_ili2db_classname; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_classname (
    iliname character varying(1024) NOT NULL,
    sqlname character varying(1024) NOT NULL
);


--
-- Name: t_ili2db_column_prop; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_column_prop (
    tablename character varying(255) NOT NULL,
    subtype character varying(255),
    columnname character varying(255) NOT NULL,
    tag character varying(1024) NOT NULL,
    setting character varying(8000) NOT NULL
);


--
-- Name: t_ili2db_dataset; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_dataset (
    t_id bigint NOT NULL,
    datasetname character varying(200)
);


--
-- Name: t_ili2db_import; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_import (
    t_id bigint NOT NULL,
    dataset bigint NOT NULL,
    importdate timestamp without time zone NOT NULL,
    importuser character varying(40) NOT NULL,
    importfile character varying(200)
);


--
-- Name: t_ili2db_import_basket; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_import_basket (
    t_id bigint NOT NULL,
    importrun bigint NOT NULL,
    basket bigint NOT NULL,
    objectcount integer
);


--
-- Name: t_ili2db_inheritance; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_inheritance (
    thisclass character varying(1024) NOT NULL,
    baseclass character varying(1024)
);


--
-- Name: t_ili2db_meta_attrs; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_meta_attrs (
    ilielement character varying(255) NOT NULL,
    attr_name character varying(1024) NOT NULL,
    attr_value character varying(8000) NOT NULL
);


--
-- Name: t_ili2db_model; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_model (
    filename character varying(250) NOT NULL,
    iliversion character varying(3) NOT NULL,
    modelname text NOT NULL,
    content text NOT NULL,
    importdate timestamp without time zone NOT NULL
);


--
-- Name: t_ili2db_settings; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_settings (
    tag character varying(60) NOT NULL,
    setting character varying(8000)
);


--
-- Name: t_ili2db_table_prop; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_table_prop (
    tablename character varying(255) NOT NULL,
    tag character varying(1024) NOT NULL,
    setting character varying(8000) NOT NULL
);


--
-- Name: t_ili2db_trafo; Type: TABLE; Schema: agi_datahub_config_v1; Owner: -
--

CREATE TABLE agi_datahub_config_v1.t_ili2db_trafo (
    iliname character varying(1024) NOT NULL,
    tag character varying(1024) NOT NULL,
    setting character varying(1024) NOT NULL
);


--
-- Name: jobrunr_backgroundjobservers; Type: TABLE; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers (
    id character(36) NOT NULL,
    workerpoolsize integer NOT NULL,
    pollintervalinseconds integer NOT NULL,
    firstheartbeat timestamp(6) without time zone NOT NULL,
    lastheartbeat timestamp(6) without time zone NOT NULL,
    running integer NOT NULL,
    systemtotalmemory bigint NOT NULL,
    systemfreememory bigint NOT NULL,
    systemcpuload numeric(3,2) NOT NULL,
    processmaxmemory bigint NOT NULL,
    processfreememory bigint NOT NULL,
    processallocatedmemory bigint NOT NULL,
    processcpuload numeric(3,2) NOT NULL,
    deletesucceededjobsafter character varying(32),
    permanentlydeletejobsafter character varying(32),
    name character varying(128)
);


--
-- Name: jobrunr_jobs; Type: TABLE; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_jobs (
    id character(36) NOT NULL,
    version integer NOT NULL,
    jobasjson text NOT NULL,
    jobsignature character varying(512) NOT NULL,
    state character varying(36) NOT NULL,
    createdat timestamp without time zone NOT NULL,
    updatedat timestamp without time zone NOT NULL,
    scheduledat timestamp without time zone,
    recurringjobid character varying(128)
);


--
-- Name: jobrunr_metadata; Type: TABLE; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_metadata (
    id character varying(156) NOT NULL,
    name character varying(92) NOT NULL,
    owner character varying(64) NOT NULL,
    value text NOT NULL,
    createdat timestamp without time zone NOT NULL,
    updatedat timestamp without time zone NOT NULL
);


--
-- Name: jobrunr_recurring_jobs; Type: TABLE; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_recurring_jobs (
    id character(128) NOT NULL,
    version integer NOT NULL,
    jobasjson text NOT NULL,
    createdat bigint DEFAULT '0'::bigint NOT NULL
);


--
-- Name: jobrunr_jobs_stats; Type: VIEW; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE VIEW agi_datahub_jobrunr_v1.jobrunr_jobs_stats AS
 WITH job_stat_results AS (
         SELECT jobrunr_jobs.state,
            count(*) AS count
           FROM agi_datahub_jobrunr_v1.jobrunr_jobs
          GROUP BY jobrunr_jobs.state
        )
 SELECT COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results), (0)::numeric) AS total,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'AWAITING'::text)), (0)::numeric) AS awaiting,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'SCHEDULED'::text)), (0)::numeric) AS scheduled,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'ENQUEUED'::text)), (0)::numeric) AS enqueued,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'PROCESSING'::text)), (0)::numeric) AS processing,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'PROCESSED'::text)), (0)::numeric) AS processed,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'FAILED'::text)), (0)::numeric) AS failed,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'SUCCEEDED'::text)), (0)::numeric) AS succeeded,
    COALESCE(( SELECT ((jm.value)::character(10))::numeric(10,0) AS value
           FROM agi_datahub_jobrunr_v1.jobrunr_metadata jm
          WHERE ((jm.id)::text = 'succeeded-jobs-counter-cluster'::text)), (0)::numeric) AS alltimesucceeded,
    COALESCE(( SELECT sum(job_stat_results.count) AS sum
           FROM job_stat_results
          WHERE ((job_stat_results.state)::text = 'DELETED'::text)), (0)::numeric) AS deleted,
    ( SELECT count(*) AS count
           FROM agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers) AS nbrofbackgroundjobservers,
    ( SELECT count(*) AS count
           FROM agi_datahub_jobrunr_v1.jobrunr_recurring_jobs) AS nbrofrecurringjobs;


--
-- Name: jobrunr_migrations; Type: TABLE; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE TABLE agi_datahub_jobrunr_v1.jobrunr_migrations (
    id character(36) NOT NULL,
    script character varying(64) NOT NULL,
    installedon character varying(29) NOT NULL
);


--
-- Name: t_ili2db_seq; Type: SEQUENCE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE SEQUENCE agi_datahub_log_v1.t_ili2db_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: deliveries_delivery; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.deliveries_delivery (
    t_id bigint DEFAULT nextval('agi_datahub_log_v1.t_ili2db_seq'::regclass) NOT NULL,
    jobid character varying(36) NOT NULL,
    deliverydate timestamp without time zone NOT NULL,
    isvalid boolean,
    isdelivered boolean,
    organisation character varying(512) NOT NULL,
    theme character varying(512) NOT NULL,
    operat character varying(512) NOT NULL,
    email character varying(512),
    validator_config character varying(512),
    validator_meta_config character varying(512),
    expected_models character varying(2048),
    processing_status character varying(20),
    result_models text,
    transfer_version character varying(10),
    validator_version character varying(128),
    notification_status character varying(20),
    updated_at timestamp without time zone,
    finished_at timestamp without time zone,
    artifacts_expired boolean
);


--
-- Name: t_ili2db_attrname; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_attrname (
    iliname character varying(1024) NOT NULL,
    sqlname character varying(1024) NOT NULL,
    colowner character varying(1024) NOT NULL,
    target character varying(1024)
);


--
-- Name: t_ili2db_basket; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_basket (
    t_id bigint NOT NULL,
    dataset bigint,
    topic character varying(200) NOT NULL,
    t_ili_tid character varying(200),
    attachmentkey character varying(200) NOT NULL,
    domains character varying(1024)
);


--
-- Name: t_ili2db_classname; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_classname (
    iliname character varying(1024) NOT NULL,
    sqlname character varying(1024) NOT NULL
);


--
-- Name: t_ili2db_column_prop; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_column_prop (
    tablename character varying(255) NOT NULL,
    subtype character varying(255),
    columnname character varying(255) NOT NULL,
    tag character varying(1024) NOT NULL,
    setting character varying(8000) NOT NULL
);


--
-- Name: t_ili2db_dataset; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_dataset (
    t_id bigint NOT NULL,
    datasetname character varying(200)
);


--
-- Name: t_ili2db_import; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_import (
    t_id bigint NOT NULL,
    dataset bigint NOT NULL,
    importdate timestamp without time zone NOT NULL,
    importuser character varying(40) NOT NULL,
    importfile character varying(200)
);


--
-- Name: t_ili2db_import_basket; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_import_basket (
    t_id bigint NOT NULL,
    importrun bigint NOT NULL,
    basket bigint NOT NULL,
    objectcount integer
);


--
-- Name: t_ili2db_inheritance; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_inheritance (
    thisclass character varying(1024) NOT NULL,
    baseclass character varying(1024)
);


--
-- Name: t_ili2db_meta_attrs; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_meta_attrs (
    ilielement character varying(255) NOT NULL,
    attr_name character varying(1024) NOT NULL,
    attr_value character varying(8000) NOT NULL
);


--
-- Name: t_ili2db_model; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_model (
    filename character varying(250) NOT NULL,
    iliversion character varying(3) NOT NULL,
    modelname text NOT NULL,
    content text NOT NULL,
    importdate timestamp without time zone NOT NULL
);


--
-- Name: t_ili2db_settings; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_settings (
    tag character varying(60) NOT NULL,
    setting character varying(8000)
);


--
-- Name: t_ili2db_table_prop; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_table_prop (
    tablename character varying(255) NOT NULL,
    tag character varying(1024) NOT NULL,
    setting character varying(8000) NOT NULL
);


--
-- Name: t_ili2db_trafo; Type: TABLE; Schema: agi_datahub_log_v1; Owner: -
--

CREATE TABLE agi_datahub_log_v1.t_ili2db_trafo (
    iliname character varying(1024) NOT NULL,
    tag character varying(1024) NOT NULL,
    setting character varying(1024) NOT NULL
);


--
-- Data for Name: core_apikey; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.core_apikey (t_id, apikey, createdat, revokedat, dateofexpiry, organisation_r) FROM stdin;
\.


--
-- Data for Name: core_operat; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.core_operat (t_id, aname, adescription, organisation_r, theme_r) FROM stdin;
\.


--
-- Data for Name: core_organisation; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.core_organisation (t_id, aname, auid, arole, email) FROM stdin;
\.


--
-- Data for Name: core_theme; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.core_theme (t_id, aname, config, metaconfig, expected_models, adescription) FROM stdin;
\.


--
-- Data for Name: t_ili2db_attrname; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_attrname (iliname, sqlname, colowner, target) FROM stdin;
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	organisation_r	core_operat	core_organisation
SO_AGI_Datahub_Config_20240403.Core.Theme.description	adescription	core_theme	\N
SO_AGI_Datahub_Config_20240403.Core.Organisation.email	email	core_organisation	\N
SO_AGI_Datahub_Config_20240403.Core.Organisation.uid	auid	core_organisation	\N
SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt	revokedat	core_apikey	\N
SO_AGI_Datahub_Config_20240403.Core.Operat.name	aname	core_operat	\N
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	theme_r	core_operat	core_theme
SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry	dateofexpiry	core_apikey	\N
SO_AGI_Datahub_Config_20240403.Core.Theme.expected_models	expected_models	core_theme	\N
SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt	createdat	core_apikey	\N
SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig	metaconfig	core_theme	\N
SO_AGI_Datahub_Config_20240403.Core.Operat.description	adescription	core_operat	\N
SO_AGI_Datahub_Config_20240403.Core.Theme.config	config	core_theme	\N
SO_AGI_Datahub_Config_20240403.Core.Theme.name	aname	core_theme	\N
SO_AGI_Datahub_Config_20240403.Core.Organisation.name	aname	core_organisation	\N
SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey	apikey	core_apikey	\N
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	organisation_r	core_apikey	core_organisation
SO_AGI_Datahub_Config_20240403.Core.Organisation.role	arole	core_organisation	\N
\.


--
-- Data for Name: t_ili2db_basket; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_basket (t_id, dataset, topic, t_ili_tid, attachmentkey, domains) FROM stdin;
\.


--
-- Data for Name: t_ili2db_classname; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_classname (iliname, sqlname) FROM stdin;
SO_AGI_Datahub_Config_20240403.Core.Theme	core_theme
SO_AGI_Datahub_Config_20240403.Core.ApiKey	core_apikey
SO_AGI_Datahub_Config_20240403.Core.Operat	core_operat
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey	core_organisation_apikey
SO_AGI_Datahub_Config_20240403.Core.Organisation	core_organisation
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation	core_operat_organisation
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat	core_theme_operat
\.


--
-- Data for Name: t_ili2db_column_prop; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_column_prop (tablename, subtype, columnname, tag, setting) FROM stdin;
core_operat	\N	adescription	ch.ehi.ili2db.typeKind	TEXT
core_operat	\N	organisation_r	ch.ehi.ili2db.foreignKey	core_organisation
core_theme	\N	config	ch.ehi.ili2db.typeKind	TEXT
core_apikey	\N	createdat	ch.ehi.ili2db.typeKind	DATETIME
core_organisation	\N	email	ch.ehi.ili2db.typeKind	TEXT
core_operat	\N	aname	ch.ehi.ili2db.typeKind	TEXT
core_operat	\N	theme_r	ch.ehi.ili2db.foreignKey	core_theme
core_organisation	\N	auid	ch.ehi.ili2db.typeKind	TEXT
core_theme	\N	aname	ch.ehi.ili2db.typeKind	TEXT
core_theme	\N	expected_models	ch.ehi.ili2db.typeKind	TEXT
core_theme	\N	adescription	ch.ehi.ili2db.typeKind	TEXT
core_organisation	\N	aname	ch.ehi.ili2db.typeKind	TEXT
core_theme	\N	metaconfig	ch.ehi.ili2db.typeKind	TEXT
core_apikey	\N	revokedat	ch.ehi.ili2db.typeKind	DATETIME
core_apikey	\N	organisation_r	ch.ehi.ili2db.foreignKey	core_organisation
core_apikey	\N	dateofexpiry	ch.ehi.ili2db.typeKind	DATETIME
core_organisation	\N	arole	ch.ehi.ili2db.typeKind	TEXT
core_apikey	\N	apikey	ch.ehi.ili2db.typeKind	TEXT
\.


--
-- Data for Name: t_ili2db_dataset; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_dataset (t_id, datasetname) FROM stdin;
\.


--
-- Data for Name: t_ili2db_import; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_import (t_id, dataset, importdate, importuser, importfile) FROM stdin;
\.


--
-- Data for Name: t_ili2db_import_basket; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_import_basket (t_id, importrun, basket, objectcount) FROM stdin;
\.


--
-- Data for Name: t_ili2db_inheritance; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_inheritance (thisclass, baseclass) FROM stdin;
SO_AGI_Datahub_Config_20240403.Core.ApiKey	\N
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat	\N
SO_AGI_Datahub_Config_20240403.Core.Operat	\N
SO_AGI_Datahub_Config_20240403.Core.Theme	\N
SO_AGI_Datahub_Config_20240403.Core.Organisation	\N
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation	\N
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey	\N
\.


--
-- Data for Name: t_ili2db_meta_attrs; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_meta_attrs (ilielement, attr_name, attr_value) FROM stdin;
SO_AGI_Datahub_Config_20240403.Core.Theme.expected_models	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.expected_models	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.processing_status	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.processing_status	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme.config	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.config	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Operat.description	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Operat.description	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifacts_expired	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifacts_expired	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme.description	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.description	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	ili2db.ili.assocCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	ili2db.ili.assocCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notification_status	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notification_status	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R	ili2db.ili.assocCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R	ili2db.ili.assocCardinalityMax	*
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expected_models	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expected_models	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	ili2db.ili.assocCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	ili2db.ili.assocCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Config_20240403.Core.Theme.name	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.name	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.result_models	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.result_models	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core	ili2db.ili.topicClasses	core_apikey core_operat core_organisation core_theme
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_version	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_version	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_config	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_config	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	ili2db.ili.assocCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	ili2db.ili.assocCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403	furtherInformation	http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
SO_AGI_Datahub_Log_20240403	ili2db.ili.lang	de
SO_AGI_Datahub_Log_20240403	technicalContact	mailto:agi@bd.so.ch
SO_AGI_Datahub_Log_20240403.Deliveries	ili2db.ili.topicClasses	deliveries_delivery
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_meta_config	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_meta_config	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R	ili2db.ili.assocCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R	ili2db.ili.assocCardinalityMax	*
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Operat.name	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Operat.name	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updated_at	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updated_at	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R	ili2db.ili.assocCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R	ili2db.ili.assocCardinalityMax	*
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finished_at	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finished_at	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation.uid	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.uid	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation.name	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.name	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.email	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.email	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403	furtherInformation	http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
SO_AGI_Datahub_Config_20240403	ili2db.ili.lang	de
SO_AGI_Datahub_Config_20240403	technicalContact	mailto:agi@bd.so.ch
SO_AGI_Datahub_Config_20240403.Core.Organisation.role	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.role	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.transfer_version	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.transfer_version	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat	ili2db.ili.attrCardinalityMin	1
\.


--
-- Data for Name: t_ili2db_model; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_model (filename, iliversion, modelname, content, importdate) FROM stdin;
SO_AGI_Datahub_20240403.ili	2.3	SO_AGI_Datahub_Config_20240403 SO_AGI_Datahub_Log_20240403	INTERLIS 2.3;\n\n/** !!------------------------------------------------------------------------------\n *  !! Version    | wer | Änderung\n *  !!------------------------------------------------------------------------------\n *  !! 2024-02-12 | sz  | Ersterfassung\n *  !! 2024-03-01 | sz  | API-Key-Variante\n *  !! 2024-04-03 | sz  | Trennung Konfig und Log\n *  !!==============================================================================\n */\n!!@ technicalContact=mailto:agi@bd.so.ch\n!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml\nMODEL SO_AGI_Datahub_Config_20240403 (de)\nAT "https://agi.so.ch"\nVERSION "2026-11-19"  =\n\n  TOPIC Core =\n\n    CLASS ApiKey =\n      /** API-Key\n       */\n      apiKey : MANDATORY TEXT*256;\n      /** Zeitpunkt der Erstellung.\n       */\n      createdAt : MANDATORY INTERLIS.XMLDateTime;\n      /** Zeitpunkt des Invalidierens.\n       */\n      revokedAt : INTERLIS.XMLDateTime;\n      /** Zeitpunkt, an dem Key invalidiert wird.\n       */\n      dateOfExpiry : INTERLIS.XMLDateTime;\n    END ApiKey;\n\n    CLASS Operat =\n      /** Name resp. ID des Operates, z.B. DMAV-2601 für die Gemeinde Solothurn im DMAV.\n       */\n      name : MANDATORY TEXT*512;\n      description : TEXT*1024;\n    END Operat;\n\n    CLASS Organisation =\n      /** Name der Organisation / Firma.\n       */\n      name : MANDATORY TEXT*512;\n      /** Unternehmens-Identifikationsnummer. Schweizweit eindeutig.\n       */\n      uid : TEXT*15;\n      /** Rolle der Organisation (Admin oder User).\n       */\n      role : MANDATORY TEXT*20;\n      email : MANDATORY TEXT*512;\n      /** Name der Organisation ist eindeutig.\n       */\n      UNIQUE name;\n    END Organisation;\n\n    /** TODO\n     */\n    CLASS Theme =\n      /** Name / ID des Themas, z.B. DMAV\n       */\n      name : MANDATORY TEXT*512;\n      /** Name / ID der Config-Datei, die für die Validierung verwendet wird.\n       */\n      config : TEXT*512;\n      /** Name / ID der Meta-Config-Datei, die für die Validierung verwendet wird.\n       */\n      metaConfig : TEXT*512;\n      /** Semicolon-separated names of permitted transfer models. */\n      expected_models : TEXT*2048;\n      description : TEXT*1024;\n      /** ID des Themas ist eindeutig.\n       */\n      UNIQUE name;\n    END Theme;\n\n    ASSOCIATION Operat_Organisation =\n      Organisation_R -- {1} Organisation;\n      Operat_R -- {0..*} Operat;\n    END Operat_Organisation;\n\n    ASSOCIATION Organisation_ApiKey =\n      ApiKey_R -- {0..*} ApiKey;\n      Organisation_R -- {1} Organisation;\n    END Organisation_ApiKey;\n\n    ASSOCIATION Theme_Operat =\n      Operat_R -- {0..*} Operat;\n      Theme_R -- {1} Theme;\n    END Theme_Operat;\n\n    CONSTRAINTS OF Operat =\n      UNIQUE name, Theme_R;\n    END;\n\n  END Core;\n\nEND SO_AGI_Datahub_Config_20240403.\n\n/** !!------------------------------------------------------------------------------\n *  !! Version    | wer | Änderung\n *  !!------------------------------------------------------------------------------\n *  !! 2024-04-03 | sz  | Ersterfassung\n *  !!==============================================================================\n */\n!!@ technicalContact=mailto:agi@bd.so.ch\n!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml\nMODEL SO_AGI_Datahub_Log_20240403 (de)\nAT "https://agi.so.ch"\nVERSION "2026-11-19"  =\n\n  TOPIC Deliveries =\n\n    CLASS Delivery =\n      jobId : MANDATORY TEXT*36;\n      deliveryDate : MANDATORY INTERLIS.XMLDateTime;\n      isValid : BOOLEAN;\n      isDelivered : BOOLEAN;\n      organisation : MANDATORY TEXT*512;\n      theme : MANDATORY TEXT*512;\n      operat : MANDATORY TEXT*512;\n      email : TEXT*512;\n      validator_config : TEXT*512;\n      validator_meta_config : TEXT*512;\n      expected_models : TEXT*2048;\n      processing_status : TEXT*20;\n      result_models : MTEXT;\n      transfer_version : TEXT*10;\n      validator_version : TEXT*128;\n      notification_status : TEXT*20;\n      updated_at : INTERLIS.XMLDateTime;\n      finished_at : INTERLIS.XMLDateTime;\n      artifacts_expired : BOOLEAN;\n      UNIQUE jobId;\n    END Delivery;\n  END Deliveries;\n\nEND SO_AGI_Datahub_Log_20240403.\n	2026-11-19 17:19:03.597
\.


--
-- Data for Name: t_ili2db_settings; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_settings (tag, setting) FROM stdin;
ch.ehi.ili2db.createMetaInfo	True
ch.interlis.ili2c.ilidirs	dev
ch.ehi.ili2db.arrayTrafo	coalesce
ch.ehi.ili2db.createForeignKeyIndex	yes
ch.ehi.ili2db.importTabs	simple
ch.ehi.ili2db.nameOptimization	topic
ch.ehi.ili2db.localisedTrafo	expand
ch.ehi.ili2db.jsonTrafo	coalesce
ch.ehi.ili2db.sender	ili2pg-5.5.2-4607cc3a1171ed8a885ed83076c197a12559b41f
ch.ehi.ili2db.createForeignKey	yes
ch.ehi.ili2db.defaultSrsAuthority	EPSG
ch.ehi.ili2db.uniqueConstraints	create
ch.ehi.ili2db.maxSqlNameLength	60
ch.ehi.ili2db.uuidDefaultValue	uuid_generate_v4()
ch.ehi.ili2db.inheritanceTrafo	smart1
ch.ehi.ili2db.catalogueRefTrafo	coalesce
ch.ehi.ili2db.multiPointTrafo	coalesce
ch.ehi.ili2db.multiLineTrafo	coalesce
ch.ehi.ili2db.structTrafo	expand
ch.ehi.ili2db.multiSurfaceTrafo	coalesce
ch.ehi.ili2db.multilingualTrafo	expand
\.


--
-- Data for Name: t_ili2db_table_prop; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_table_prop (tablename, tag, setting) FROM stdin;
core_organisation	ch.ehi.ili2db.tableKind	CLASS
core_theme	ch.ehi.ili2db.tableKind	CLASS
core_operat	ch.ehi.ili2db.tableKind	CLASS
core_apikey	ch.ehi.ili2db.tableKind	CLASS
\.


--
-- Data for Name: t_ili2db_trafo; Type: TABLE DATA; Schema: agi_datahub_config_v1; Owner: -
--

COPY agi_datahub_config_v1.t_ili2db_trafo (iliname, tag, setting) FROM stdin;
SO_AGI_Datahub_Config_20240403.Core.Theme	ch.ehi.ili2db.inheritance	newClass
SO_AGI_Datahub_Config_20240403.Core.ApiKey	ch.ehi.ili2db.inheritance	newClass
SO_AGI_Datahub_Config_20240403.Core.Operat	ch.ehi.ili2db.inheritance	newClass
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey	ch.ehi.ili2db.inheritance	embedded
SO_AGI_Datahub_Config_20240403.Core.Organisation	ch.ehi.ili2db.inheritance	newClass
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation	ch.ehi.ili2db.inheritance	embedded
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat	ch.ehi.ili2db.inheritance	embedded
\.


--
-- Data for Name: jobrunr_backgroundjobservers; Type: TABLE DATA; Schema: agi_datahub_jobrunr_v1; Owner: -
--

COPY agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers (id, workerpoolsize, pollintervalinseconds, firstheartbeat, lastheartbeat, running, systemtotalmemory, systemfreememory, systemcpuload, processmaxmemory, processfreememory, processallocatedmemory, processcpuload, deletesucceededjobsafter, permanentlydeletejobsafter, name) FROM stdin;
\.


--
-- Data for Name: jobrunr_jobs; Type: TABLE DATA; Schema: agi_datahub_jobrunr_v1; Owner: -
--

COPY agi_datahub_jobrunr_v1.jobrunr_jobs (id, version, jobasjson, jobsignature, state, createdat, updatedat, scheduledat, recurringjobid) FROM stdin;
\.


--
-- Data for Name: jobrunr_metadata; Type: TABLE DATA; Schema: agi_datahub_jobrunr_v1; Owner: -
--

COPY agi_datahub_jobrunr_v1.jobrunr_metadata (id, name, owner, value, createdat, updatedat) FROM stdin;
succeeded-jobs-counter-cluster	succeeded-jobs-counter	cluster	0	2026-11-19 17:19:09.854961	2026-11-19 17:19:09.854961
\.


--
-- Data for Name: jobrunr_migrations; Type: TABLE DATA; Schema: agi_datahub_jobrunr_v1; Owner: -
--

COPY agi_datahub_jobrunr_v1.jobrunr_migrations (id, script, installedon) FROM stdin;
8a2033f3-ae1d-4f15-b878-e54f761c4bde	v000__create_migrations_table.sql	2026-11-19T16:19:09.678049Z
ac70f4a7-94a1-47b1-ae15-f8c100bddae1	v001__create_job_table.sql	2026-11-19T16:19:09.740456Z
bb64efb6-a7d3-48cf-b273-58d9e0e8ff20	v002__create_recurring_job_table.sql	2026-11-19T16:19:09.755292Z
e04ed942-8a35-4c22-84ad-ba2a1d89a5d1	v003__create_background_job_server_table.sql	2026-11-19T16:19:09.769500Z
f4b10d5e-38ff-421a-918e-d29c0572eb78	v004__create_job_stats_view.sql	2026-11-19T16:19:09.788138Z
2bb9b26f-ecfb-4201-bb5b-11b196f529e8	v005__update_job_stats_view.sql	2026-11-19T16:19:09.807295Z
a0067f47-e980-4acd-bd52-d7ef1c070076	v006__alter_table_jobs_add_recurringjob.sql	2026-11-19T16:19:09.823199Z
ae0655a0-5362-4d7d-8ebb-c8fb95a6de93	v007__alter_table_backgroundjobserver_add_delete_config.sql	2026-11-19T16:19:09.836735Z
b0d48fb9-51f0-42de-94eb-157ac1ba95d3	v008__alter_table_jobs_increase_jobAsJson_size.sql	2026-11-19T16:19:09.848031Z
6280adc5-46fb-4a48-bfaf-42a93c714534	v009__change_jobrunr_job_counters_to_jobrunr_metadata.sql	2026-11-19T16:19:09.865002Z
841fe68b-c2d5-437c-a38d-e00ff2469b1e	v010__change_job_stats.sql	2026-11-19T16:19:09.877892Z
76fe5b6c-4a81-456d-a04e-9d9e1299476b	v011__change_sqlserver_text_to_varchar.sql	2026-11-19T16:19:09.886212Z
5625cf49-b7c9-4f89-919e-e25fc5cd57c6	v012__change_oracle_alter_jobrunr_metadata_column_size.sql	2026-11-19T16:19:09.894846Z
a6d194de-cc37-4d1b-9097-b850d3d3a0cc	v013__alter_table_recurring_job_add_createdAt.sql	2026-11-19T16:19:09.905384Z
2bad054f-1c9d-45f0-9f48-492dad45bcd7	v014__improve_job_stats.sql	2026-11-19T16:19:09.916880Z
f060e5e8-0c7b-441f-9e25-031402775f69	v015__alter_table_backgroundjobserver_add_name.sql	2026-11-19T16:19:09.925247Z
6bafe1b5-1f71-4712-ac1f-b176b58c950a	v016__alter_jobs_stats_add_awaiting_jobs.sql	2026-11-19T16:19:09.935596Z
\.


--
-- Data for Name: jobrunr_recurring_jobs; Type: TABLE DATA; Schema: agi_datahub_jobrunr_v1; Owner: -
--

COPY agi_datahub_jobrunr_v1.jobrunr_recurring_jobs (id, version, jobasjson, createdat) FROM stdin;
\.


--
-- Data for Name: deliveries_delivery; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.deliveries_delivery (t_id, jobid, deliverydate, isvalid, isdelivered, organisation, theme, operat, email, validator_config, validator_meta_config, expected_models, processing_status, result_models, transfer_version, validator_version, notification_status, updated_at, finished_at, artifacts_expired) FROM stdin;
\.


--
-- Data for Name: t_ili2db_attrname; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_attrname (iliname, sqlname, colowner, target) FROM stdin;
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.transfer_version	transfer_version	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation	organisation	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate	deliverydate	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_config	validator_config	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifacts_expired	artifacts_expired	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expected_models	expected_models	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.result_models	result_models	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_meta_config	validator_meta_config	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notification_status	notification_status	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_version	validator_version	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finished_at	finished_at	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered	isdelivered	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updated_at	updated_at	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat	operat	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email	email	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid	isvalid	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme	theme	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.processing_status	processing_status	deliveries_delivery	\N
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId	jobid	deliveries_delivery	\N
\.


--
-- Data for Name: t_ili2db_basket; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_basket (t_id, dataset, topic, t_ili_tid, attachmentkey, domains) FROM stdin;
\.


--
-- Data for Name: t_ili2db_classname; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_classname (iliname, sqlname) FROM stdin;
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery	deliveries_delivery
\.


--
-- Data for Name: t_ili2db_column_prop; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_column_prop (tablename, subtype, columnname, tag, setting) FROM stdin;
deliveries_delivery	\N	jobid	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	validator_meta_config	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	organisation	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	result_models	ch.ehi.ili2db.textKind	MTEXT
deliveries_delivery	\N	result_models	ch.ehi.ili2db.typeKind	MTEXT
deliveries_delivery	\N	notification_status	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	processing_status	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	deliveries_delivery	isvalid	ch.ehi.ili2db.enumDomain	INTERLIS.BOOLEAN
deliveries_delivery	\N	expected_models	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	validator_config	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	operat	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	deliveries_delivery	artifacts_expired	ch.ehi.ili2db.enumDomain	INTERLIS.BOOLEAN
deliveries_delivery	\N	isdelivered	ch.ehi.ili2db.typeKind	BOOLEAN
deliveries_delivery	\N	isvalid	ch.ehi.ili2db.typeKind	BOOLEAN
deliveries_delivery	\N	finished_at	ch.ehi.ili2db.typeKind	DATETIME
deliveries_delivery	deliveries_delivery	isdelivered	ch.ehi.ili2db.enumDomain	INTERLIS.BOOLEAN
deliveries_delivery	\N	email	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	artifacts_expired	ch.ehi.ili2db.typeKind	BOOLEAN
deliveries_delivery	\N	validator_version	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	updated_at	ch.ehi.ili2db.typeKind	DATETIME
deliveries_delivery	\N	theme	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	transfer_version	ch.ehi.ili2db.typeKind	TEXT
deliveries_delivery	\N	deliverydate	ch.ehi.ili2db.typeKind	DATETIME
\.


--
-- Data for Name: t_ili2db_dataset; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_dataset (t_id, datasetname) FROM stdin;
\.


--
-- Data for Name: t_ili2db_import; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_import (t_id, dataset, importdate, importuser, importfile) FROM stdin;
\.


--
-- Data for Name: t_ili2db_import_basket; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_import_basket (t_id, importrun, basket, objectcount) FROM stdin;
\.


--
-- Data for Name: t_ili2db_inheritance; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_inheritance (thisclass, baseclass) FROM stdin;
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery	\N
\.


--
-- Data for Name: t_ili2db_meta_attrs; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_meta_attrs (ilielement, attr_name, attr_value) FROM stdin;
SO_AGI_Datahub_Config_20240403.Core.Theme.expected_models	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.expected_models	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.processing_status	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.processing_status	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme.config	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.config	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.theme	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Operat.description	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Operat.description	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifacts_expired	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.artifacts_expired	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme.description	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.description	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	ili2db.ili.assocCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	ili2db.ili.assocCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.Organisation_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notification_status	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.notification_status	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R	ili2db.ili.assocCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R	ili2db.ili.assocCardinalityMax	*
SO_AGI_Datahub_Config_20240403.Core.Organisation_ApiKey.ApiKey_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.jobId	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.deliveryDate	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expected_models	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.expected_models	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	ili2db.ili.assocCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	ili2db.ili.assocCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Theme_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Config_20240403.Core.Theme.name	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.name	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.result_models	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.result_models	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.revokedAt	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core	ili2db.ili.topicClasses	core_apikey core_operat core_organisation core_theme
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_version	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_version	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_config	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_config	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	ili2db.ili.assocCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	ili2db.ili.assocCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Organisation_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403	furtherInformation	http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
SO_AGI_Datahub_Log_20240403	ili2db.ili.lang	de
SO_AGI_Datahub_Log_20240403	technicalContact	mailto:agi@bd.so.ch
SO_AGI_Datahub_Log_20240403.Deliveries	ili2db.ili.topicClasses	deliveries_delivery
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_meta_config	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.validator_meta_config	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.email	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R	ili2db.ili.assocCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R	ili2db.ili.assocCardinalityMax	*
SO_AGI_Datahub_Config_20240403.Core.Theme_Operat.Operat_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isValid	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.createdAt	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Operat.name	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Operat.name	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updated_at	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.updated_at	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Theme.metaConfig	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R	ili2db.ili.assocCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R	ili2db.ili.assocCardinalityMax	*
SO_AGI_Datahub_Config_20240403.Core.Operat_Organisation.Operat_R	ili2db.ili.assocKind	ASSOCIATE
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.isDelivered	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finished_at	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.finished_at	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.dateOfExpiry	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation.uid	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.uid	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Config_20240403.Core.Organisation.name	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.name	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.email	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.email	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.ApiKey.apiKey	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Config_20240403	furtherInformation	http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml
SO_AGI_Datahub_Config_20240403	ili2db.ili.lang	de
SO_AGI_Datahub_Config_20240403	technicalContact	mailto:agi@bd.so.ch
SO_AGI_Datahub_Config_20240403.Core.Organisation.role	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Config_20240403.Core.Organisation.role	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.transfer_version	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.transfer_version	ili2db.ili.attrCardinalityMin	0
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.organisation	ili2db.ili.attrCardinalityMin	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat	ili2db.ili.attrCardinalityMax	1
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery.operat	ili2db.ili.attrCardinalityMin	1
\.


--
-- Data for Name: t_ili2db_model; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_model (filename, iliversion, modelname, content, importdate) FROM stdin;
SO_AGI_Datahub_20240403.ili	2.3	SO_AGI_Datahub_Config_20240403 SO_AGI_Datahub_Log_20240403	INTERLIS 2.3;\n\n/** !!------------------------------------------------------------------------------\n *  !! Version    | wer | Änderung\n *  !!------------------------------------------------------------------------------\n *  !! 2024-02-12 | sz  | Ersterfassung\n *  !! 2024-03-01 | sz  | API-Key-Variante\n *  !! 2024-04-03 | sz  | Trennung Konfig und Log\n *  !!==============================================================================\n */\n!!@ technicalContact=mailto:agi@bd.so.ch\n!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml\nMODEL SO_AGI_Datahub_Config_20240403 (de)\nAT "https://agi.so.ch"\nVERSION "2026-11-19"  =\n\n  TOPIC Core =\n\n    CLASS ApiKey =\n      /** API-Key\n       */\n      apiKey : MANDATORY TEXT*256;\n      /** Zeitpunkt der Erstellung.\n       */\n      createdAt : MANDATORY INTERLIS.XMLDateTime;\n      /** Zeitpunkt des Invalidierens.\n       */\n      revokedAt : INTERLIS.XMLDateTime;\n      /** Zeitpunkt, an dem Key invalidiert wird.\n       */\n      dateOfExpiry : INTERLIS.XMLDateTime;\n    END ApiKey;\n\n    CLASS Operat =\n      /** Name resp. ID des Operates, z.B. DMAV-2601 für die Gemeinde Solothurn im DMAV.\n       */\n      name : MANDATORY TEXT*512;\n      description : TEXT*1024;\n    END Operat;\n\n    CLASS Organisation =\n      /** Name der Organisation / Firma.\n       */\n      name : MANDATORY TEXT*512;\n      /** Unternehmens-Identifikationsnummer. Schweizweit eindeutig.\n       */\n      uid : TEXT*15;\n      /** Rolle der Organisation (Admin oder User).\n       */\n      role : MANDATORY TEXT*20;\n      email : MANDATORY TEXT*512;\n      /** Name der Organisation ist eindeutig.\n       */\n      UNIQUE name;\n    END Organisation;\n\n    /** TODO\n     */\n    CLASS Theme =\n      /** Name / ID des Themas, z.B. DMAV\n       */\n      name : MANDATORY TEXT*512;\n      /** Name / ID der Config-Datei, die für die Validierung verwendet wird.\n       */\n      config : TEXT*512;\n      /** Name / ID der Meta-Config-Datei, die für die Validierung verwendet wird.\n       */\n      metaConfig : TEXT*512;\n      /** Semicolon-separated names of permitted transfer models. */\n      expected_models : TEXT*2048;\n      description : TEXT*1024;\n      /** ID des Themas ist eindeutig.\n       */\n      UNIQUE name;\n    END Theme;\n\n    ASSOCIATION Operat_Organisation =\n      Organisation_R -- {1} Organisation;\n      Operat_R -- {0..*} Operat;\n    END Operat_Organisation;\n\n    ASSOCIATION Organisation_ApiKey =\n      ApiKey_R -- {0..*} ApiKey;\n      Organisation_R -- {1} Organisation;\n    END Organisation_ApiKey;\n\n    ASSOCIATION Theme_Operat =\n      Operat_R -- {0..*} Operat;\n      Theme_R -- {1} Theme;\n    END Theme_Operat;\n\n    CONSTRAINTS OF Operat =\n      UNIQUE name, Theme_R;\n    END;\n\n  END Core;\n\nEND SO_AGI_Datahub_Config_20240403.\n\n/** !!------------------------------------------------------------------------------\n *  !! Version    | wer | Änderung\n *  !!------------------------------------------------------------------------------\n *  !! 2024-04-03 | sz  | Ersterfassung\n *  !!==============================================================================\n */\n!!@ technicalContact=mailto:agi@bd.so.ch\n!!@ furtherInformation=http://geo.so.ch/models/AGI/SO_AGI_Datahub_20240301.uml\nMODEL SO_AGI_Datahub_Log_20240403 (de)\nAT "https://agi.so.ch"\nVERSION "2026-11-19"  =\n\n  TOPIC Deliveries =\n\n    CLASS Delivery =\n      jobId : MANDATORY TEXT*36;\n      deliveryDate : MANDATORY INTERLIS.XMLDateTime;\n      isValid : BOOLEAN;\n      isDelivered : BOOLEAN;\n      organisation : MANDATORY TEXT*512;\n      theme : MANDATORY TEXT*512;\n      operat : MANDATORY TEXT*512;\n      email : TEXT*512;\n      validator_config : TEXT*512;\n      validator_meta_config : TEXT*512;\n      expected_models : TEXT*2048;\n      processing_status : TEXT*20;\n      result_models : MTEXT;\n      transfer_version : TEXT*10;\n      validator_version : TEXT*128;\n      notification_status : TEXT*20;\n      updated_at : INTERLIS.XMLDateTime;\n      finished_at : INTERLIS.XMLDateTime;\n      artifacts_expired : BOOLEAN;\n      UNIQUE jobId;\n    END Delivery;\n  END Deliveries;\n\nEND SO_AGI_Datahub_Log_20240403.\n	2026-11-19 17:19:03.806
\.


--
-- Data for Name: t_ili2db_settings; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_settings (tag, setting) FROM stdin;
ch.ehi.ili2db.createMetaInfo	True
ch.interlis.ili2c.ilidirs	dev
ch.ehi.ili2db.arrayTrafo	coalesce
ch.ehi.ili2db.createForeignKeyIndex	yes
ch.ehi.ili2db.importTabs	simple
ch.ehi.ili2db.nameOptimization	topic
ch.ehi.ili2db.localisedTrafo	expand
ch.ehi.ili2db.jsonTrafo	coalesce
ch.ehi.ili2db.sender	ili2pg-5.5.2-4607cc3a1171ed8a885ed83076c197a12559b41f
ch.ehi.ili2db.createForeignKey	yes
ch.ehi.ili2db.defaultSrsAuthority	EPSG
ch.ehi.ili2db.uniqueConstraints	create
ch.ehi.ili2db.maxSqlNameLength	60
ch.ehi.ili2db.uuidDefaultValue	uuid_generate_v4()
ch.ehi.ili2db.inheritanceTrafo	smart1
ch.ehi.ili2db.catalogueRefTrafo	coalesce
ch.ehi.ili2db.multiPointTrafo	coalesce
ch.ehi.ili2db.multiLineTrafo	coalesce
ch.ehi.ili2db.structTrafo	expand
ch.ehi.ili2db.multiSurfaceTrafo	coalesce
ch.ehi.ili2db.multilingualTrafo	expand
\.


--
-- Data for Name: t_ili2db_table_prop; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_table_prop (tablename, tag, setting) FROM stdin;
deliveries_delivery	ch.ehi.ili2db.tableKind	CLASS
\.


--
-- Data for Name: t_ili2db_trafo; Type: TABLE DATA; Schema: agi_datahub_log_v1; Owner: -
--

COPY agi_datahub_log_v1.t_ili2db_trafo (iliname, tag, setting) FROM stdin;
SO_AGI_Datahub_Log_20240403.Deliveries.Delivery	ch.ehi.ili2db.inheritance	newClass
\.


--
-- Name: t_ili2db_seq; Type: SEQUENCE SET; Schema: agi_datahub_config_v1; Owner: -
--

SELECT pg_catalog.setval('agi_datahub_config_v1.t_ili2db_seq', 1, false);


--
-- Name: t_ili2db_seq; Type: SEQUENCE SET; Schema: agi_datahub_log_v1; Owner: -
--

SELECT pg_catalog.setval('agi_datahub_log_v1.t_ili2db_seq', 1, false);


--
-- Name: core_apikey core_apikey_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_apikey
    ADD CONSTRAINT core_apikey_pkey PRIMARY KEY (t_id);


--
-- Name: core_operat core_operat_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_operat
    ADD CONSTRAINT core_operat_pkey PRIMARY KEY (t_id);


--
-- Name: core_organisation core_organisation_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_organisation
    ADD CONSTRAINT core_organisation_pkey PRIMARY KEY (t_id);


--
-- Name: core_theme core_theme_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_theme
    ADD CONSTRAINT core_theme_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_attrname t_ili2db_attrname_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_attrname
    ADD CONSTRAINT t_ili2db_attrname_pkey PRIMARY KEY (sqlname, colowner);


--
-- Name: t_ili2db_basket t_ili2db_basket_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_basket
    ADD CONSTRAINT t_ili2db_basket_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_classname t_ili2db_classname_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_classname
    ADD CONSTRAINT t_ili2db_classname_pkey PRIMARY KEY (iliname);


--
-- Name: t_ili2db_dataset t_ili2db_dataset_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_dataset
    ADD CONSTRAINT t_ili2db_dataset_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_import_basket t_ili2db_import_basket_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_import_basket
    ADD CONSTRAINT t_ili2db_import_basket_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_import t_ili2db_import_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_import
    ADD CONSTRAINT t_ili2db_import_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_inheritance t_ili2db_inheritance_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_inheritance
    ADD CONSTRAINT t_ili2db_inheritance_pkey PRIMARY KEY (thisclass);


--
-- Name: t_ili2db_model t_ili2db_model_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_model
    ADD CONSTRAINT t_ili2db_model_pkey PRIMARY KEY (iliversion, modelname);


--
-- Name: t_ili2db_settings t_ili2db_settings_pkey; Type: CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_settings
    ADD CONSTRAINT t_ili2db_settings_pkey PRIMARY KEY (tag);


--
-- Name: jobrunr_backgroundjobservers jobrunr_backgroundjobservers_pkey; Type: CONSTRAINT; Schema: agi_datahub_jobrunr_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers
    ADD CONSTRAINT jobrunr_backgroundjobservers_pkey PRIMARY KEY (id);


--
-- Name: jobrunr_jobs jobrunr_jobs_pkey; Type: CONSTRAINT; Schema: agi_datahub_jobrunr_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_jobrunr_v1.jobrunr_jobs
    ADD CONSTRAINT jobrunr_jobs_pkey PRIMARY KEY (id);


--
-- Name: jobrunr_metadata jobrunr_metadata_pkey; Type: CONSTRAINT; Schema: agi_datahub_jobrunr_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_jobrunr_v1.jobrunr_metadata
    ADD CONSTRAINT jobrunr_metadata_pkey PRIMARY KEY (id);


--
-- Name: jobrunr_migrations jobrunr_migrations_pkey; Type: CONSTRAINT; Schema: agi_datahub_jobrunr_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_jobrunr_v1.jobrunr_migrations
    ADD CONSTRAINT jobrunr_migrations_pkey PRIMARY KEY (id);


--
-- Name: jobrunr_recurring_jobs jobrunr_recurring_jobs_pkey; Type: CONSTRAINT; Schema: agi_datahub_jobrunr_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_jobrunr_v1.jobrunr_recurring_jobs
    ADD CONSTRAINT jobrunr_recurring_jobs_pkey PRIMARY KEY (id);


--
-- Name: deliveries_delivery deliveries_delivery_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.deliveries_delivery
    ADD CONSTRAINT deliveries_delivery_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_attrname t_ili2db_attrname_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_attrname
    ADD CONSTRAINT t_ili2db_attrname_pkey PRIMARY KEY (sqlname, colowner);


--
-- Name: t_ili2db_basket t_ili2db_basket_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_basket
    ADD CONSTRAINT t_ili2db_basket_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_classname t_ili2db_classname_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_classname
    ADD CONSTRAINT t_ili2db_classname_pkey PRIMARY KEY (iliname);


--
-- Name: t_ili2db_dataset t_ili2db_dataset_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_dataset
    ADD CONSTRAINT t_ili2db_dataset_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_import_basket t_ili2db_import_basket_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_import_basket
    ADD CONSTRAINT t_ili2db_import_basket_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_import t_ili2db_import_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_import
    ADD CONSTRAINT t_ili2db_import_pkey PRIMARY KEY (t_id);


--
-- Name: t_ili2db_inheritance t_ili2db_inheritance_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_inheritance
    ADD CONSTRAINT t_ili2db_inheritance_pkey PRIMARY KEY (thisclass);


--
-- Name: t_ili2db_model t_ili2db_model_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_model
    ADD CONSTRAINT t_ili2db_model_pkey PRIMARY KEY (modelname, iliversion);


--
-- Name: t_ili2db_settings t_ili2db_settings_pkey; Type: CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_settings
    ADD CONSTRAINT t_ili2db_settings_pkey PRIMARY KEY (tag);


--
-- Name: core_apikey_organisation_r_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX core_apikey_organisation_r_idx ON agi_datahub_config_v1.core_apikey USING btree (organisation_r);


--
-- Name: core_operat_organisation_r_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX core_operat_organisation_r_idx ON agi_datahub_config_v1.core_operat USING btree (organisation_r);


--
-- Name: core_operat_theme_name_key; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE UNIQUE INDEX core_operat_theme_name_key ON agi_datahub_config_v1.core_operat USING btree (theme_r, aname);


--
-- Name: core_operat_theme_r_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX core_operat_theme_r_idx ON agi_datahub_config_v1.core_operat USING btree (theme_r);


--
-- Name: core_organisation_aname_key; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE UNIQUE INDEX core_organisation_aname_key ON agi_datahub_config_v1.core_organisation USING btree (aname);


--
-- Name: core_theme_aname_key; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE UNIQUE INDEX core_theme_aname_key ON agi_datahub_config_v1.core_theme USING btree (aname);


--
-- Name: t_ili2db_attrname_sqlname_colowner_key; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE UNIQUE INDEX t_ili2db_attrname_sqlname_colowner_key ON agi_datahub_config_v1.t_ili2db_attrname USING btree (sqlname, colowner);


--
-- Name: t_ili2db_basket_dataset_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX t_ili2db_basket_dataset_idx ON agi_datahub_config_v1.t_ili2db_basket USING btree (dataset);


--
-- Name: t_ili2db_dataset_datasetname_key; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE UNIQUE INDEX t_ili2db_dataset_datasetname_key ON agi_datahub_config_v1.t_ili2db_dataset USING btree (datasetname);


--
-- Name: t_ili2db_import_basket_basket_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX t_ili2db_import_basket_basket_idx ON agi_datahub_config_v1.t_ili2db_import_basket USING btree (basket);


--
-- Name: t_ili2db_import_basket_importrun_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX t_ili2db_import_basket_importrun_idx ON agi_datahub_config_v1.t_ili2db_import_basket USING btree (importrun);


--
-- Name: t_ili2db_import_dataset_idx; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE INDEX t_ili2db_import_dataset_idx ON agi_datahub_config_v1.t_ili2db_import USING btree (dataset);


--
-- Name: t_ili2db_model_iliversion_modelname_key; Type: INDEX; Schema: agi_datahub_config_v1; Owner: -
--

CREATE UNIQUE INDEX t_ili2db_model_iliversion_modelname_key ON agi_datahub_config_v1.t_ili2db_model USING btree (iliversion, modelname);


--
-- Name: jobrunr_bgjobsrvrs_fsthb_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_bgjobsrvrs_fsthb_idx ON agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers USING btree (firstheartbeat);


--
-- Name: jobrunr_bgjobsrvrs_lsthb_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_bgjobsrvrs_lsthb_idx ON agi_datahub_jobrunr_v1.jobrunr_backgroundjobservers USING btree (lastheartbeat);


--
-- Name: jobrunr_job_created_at_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_job_created_at_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs USING btree (createdat);


--
-- Name: jobrunr_job_rci_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_job_rci_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs USING btree (recurringjobid);


--
-- Name: jobrunr_job_scheduled_at_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_job_scheduled_at_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs USING btree (scheduledat);


--
-- Name: jobrunr_job_signature_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_job_signature_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs USING btree (jobsignature);


--
-- Name: jobrunr_jobs_state_updated_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_jobs_state_updated_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs USING btree (state, updatedat);


--
-- Name: jobrunr_recurring_job_created_at_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_recurring_job_created_at_idx ON agi_datahub_jobrunr_v1.jobrunr_recurring_jobs USING btree (createdat);


--
-- Name: jobrunr_state_idx; Type: INDEX; Schema: agi_datahub_jobrunr_v1; Owner: -
--

CREATE INDEX jobrunr_state_idx ON agi_datahub_jobrunr_v1.jobrunr_jobs USING btree (state);


--
-- Name: deliveries_delivery_jobid_key; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE UNIQUE INDEX deliveries_delivery_jobid_key ON agi_datahub_log_v1.deliveries_delivery USING btree (jobid);


--
-- Name: t_ili2db_attrname_sqlname_colowner_key; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE UNIQUE INDEX t_ili2db_attrname_sqlname_colowner_key ON agi_datahub_log_v1.t_ili2db_attrname USING btree (sqlname, colowner);


--
-- Name: t_ili2db_basket_dataset_idx; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE INDEX t_ili2db_basket_dataset_idx ON agi_datahub_log_v1.t_ili2db_basket USING btree (dataset);


--
-- Name: t_ili2db_dataset_datasetname_key; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE UNIQUE INDEX t_ili2db_dataset_datasetname_key ON agi_datahub_log_v1.t_ili2db_dataset USING btree (datasetname);


--
-- Name: t_ili2db_import_basket_basket_idx; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE INDEX t_ili2db_import_basket_basket_idx ON agi_datahub_log_v1.t_ili2db_import_basket USING btree (basket);


--
-- Name: t_ili2db_import_basket_importrun_idx; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE INDEX t_ili2db_import_basket_importrun_idx ON agi_datahub_log_v1.t_ili2db_import_basket USING btree (importrun);


--
-- Name: t_ili2db_import_dataset_idx; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE INDEX t_ili2db_import_dataset_idx ON agi_datahub_log_v1.t_ili2db_import USING btree (dataset);


--
-- Name: t_ili2db_model_modelname_iliversion_key; Type: INDEX; Schema: agi_datahub_log_v1; Owner: -
--

CREATE UNIQUE INDEX t_ili2db_model_modelname_iliversion_key ON agi_datahub_log_v1.t_ili2db_model USING btree (modelname, iliversion);


--
-- Name: core_apikey core_apikey_organisation_r_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_apikey
    ADD CONSTRAINT core_apikey_organisation_r_fkey FOREIGN KEY (organisation_r) REFERENCES agi_datahub_config_v1.core_organisation(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: core_operat core_operat_organisation_r_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_operat
    ADD CONSTRAINT core_operat_organisation_r_fkey FOREIGN KEY (organisation_r) REFERENCES agi_datahub_config_v1.core_organisation(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: core_operat core_operat_theme_r_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.core_operat
    ADD CONSTRAINT core_operat_theme_r_fkey FOREIGN KEY (theme_r) REFERENCES agi_datahub_config_v1.core_theme(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: t_ili2db_basket t_ili2db_basket_dataset_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_basket
    ADD CONSTRAINT t_ili2db_basket_dataset_fkey FOREIGN KEY (dataset) REFERENCES agi_datahub_config_v1.t_ili2db_dataset(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: t_ili2db_import_basket t_ili2db_import_basket_importrun_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_config_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_config_v1.t_ili2db_import_basket
    ADD CONSTRAINT t_ili2db_import_basket_importrun_fkey FOREIGN KEY (importrun) REFERENCES agi_datahub_config_v1.t_ili2db_import(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: t_ili2db_basket t_ili2db_basket_dataset_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_basket
    ADD CONSTRAINT t_ili2db_basket_dataset_fkey FOREIGN KEY (dataset) REFERENCES agi_datahub_log_v1.t_ili2db_dataset(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: t_ili2db_import_basket t_ili2db_import_basket_importrun_fkey; Type: FK CONSTRAINT; Schema: agi_datahub_log_v1; Owner: -
--

ALTER TABLE ONLY agi_datahub_log_v1.t_ili2db_import_basket
    ADD CONSTRAINT t_ili2db_import_basket_importrun_fkey FOREIGN KEY (importrun) REFERENCES agi_datahub_log_v1.t_ili2db_import(t_id) DEFERRABLE INITIALLY DEFERRED;


--
-- PostgreSQL database dump complete
--

-- Append after the schema dump when generating setup_gdi.sql.
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'datahub_read') THEN
    CREATE ROLE datahub_read NOLOGIN;
  END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'datahub_write') THEN
    CREATE ROLE datahub_write NOLOGIN;
  END IF;
END $$;

GRANT USAGE ON SCHEMA agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub;

GRANT USAGE ON SCHEMA agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub_read;
GRANT SELECT ON ALL TABLES IN SCHEMA
    agi_datahub_config_v1, agi_datahub_log_v1, agi_datahub_jobrunr_v1 TO datahub_read;

GRANT USAGE ON SCHEMA agi_datahub_config_v1 TO datahub_write;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA agi_datahub_config_v1 TO datahub_write;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA agi_datahub_config_v1 TO datahub_write;

COMMIT;
