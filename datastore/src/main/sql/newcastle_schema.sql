--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: artifact; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE artifact (
    id integer NOT NULL,
    artifact_status character varying(255) NOT NULL,
    checksum character varying(255),
    deploy_url character varying(255),
    filename character varying(100) NOT NULL,
    identifier character varying(255) NOT NULL,
    repository_type character varying(255) NOT NULL,
    project_build_result_id integer
);


ALTER TABLE public.artifact OWNER TO newcastle;

--
-- Name: artifact_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE artifact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.artifact_id_seq OWNER TO newcastle;

--
-- Name: artifact_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE artifact_id_seq OWNED BY artifact.id;


--
-- Name: build_collection; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE build_collection (
    id integer NOT NULL,
    product_build_number integer NOT NULL,
    product_milestone character varying(20) NOT NULL,
    product_version_id integer
);


ALTER TABLE public.build_collection OWNER TO newcastle;

--
-- Name: build_collection_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE build_collection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.build_collection_id_seq OWNER TO newcastle;

--
-- Name: build_collection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE build_collection_id_seq OWNED BY build_collection.id;


--
-- Name: build_collection_project_build_result; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE build_collection_project_build_result (
    buildcollection_id integer NOT NULL,
    projectbuildresult_id integer NOT NULL
);


ALTER TABLE public.build_collection_project_build_result OWNER TO newcastle;

--
-- Name: environment; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE environment (
    id integer NOT NULL,
    build_type character varying(50) NOT NULL,
    operational_system character varying(50) NOT NULL
);


ALTER TABLE public.environment OWNER TO newcastle;

--
-- Name: environment_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE environment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.environment_id_seq OWNER TO newcastle;

--
-- Name: environment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE environment_id_seq OWNED BY environment.id;


--
-- Name: license; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE license (
    id integer NOT NULL,
    full_content text NOT NULL,
    full_name character varying(255) NOT NULL,
    ref_url character varying(255),
    short_name character varying(255)
);


ALTER TABLE public.license OWNER TO newcastle;

--
-- Name: license_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE license_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.license_id_seq OWNER TO newcastle;

--
-- Name: license_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE license_id_seq OWNED BY license.id;


--
-- Name: product; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE product (
    id integer NOT NULL,
    description character varying(255),
    name character varying(100) NOT NULL
);


ALTER TABLE public.product OWNER TO newcastle;

--
-- Name: product_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_id_seq OWNER TO newcastle;

--
-- Name: product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE product_id_seq OWNED BY product.id;


--
-- Name: product_version; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE product_version (
    id integer NOT NULL,
    version character varying(50) NOT NULL,
    product_id integer
);


ALTER TABLE public.product_version OWNER TO newcastle;

--
-- Name: product_version_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE product_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_version_id_seq OWNER TO newcastle;

--
-- Name: product_version_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE product_version_id_seq OWNED BY product_version.id;


--
-- Name: product_version_project; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE product_version_project (
    id integer NOT NULL,
    product_version_id integer,
    project_id integer
);


ALTER TABLE public.product_version_project OWNER TO newcastle;

--
-- Name: product_version_project_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE product_version_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.product_version_project_id_seq OWNER TO newcastle;

--
-- Name: product_version_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE product_version_project_id_seq OWNED BY product_version_project.id;


--
-- Name: project; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE project (
    id integer NOT NULL,
    description character varying(255),
    issue_tracker_url character varying(255),
    name character varying(100) NOT NULL,
    project_url character varying(255),
    license_id integer
);


ALTER TABLE public.project OWNER TO newcastle;

--
-- Name: project_build_configuration; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE project_build_configuration (
    id integer NOT NULL,
    build_script character varying(255) NOT NULL,
    creation_time timestamp without time zone,
    identifier character varying(255) NOT NULL,
    last_modification_time timestamp without time zone,
    patches_url character varying(255),
    repository character varying(255),
    scm_url character varying(255) NOT NULL,
    environment_id integer,
    parent_id integer,
    product_version_id integer,
    project_id integer
);


ALTER TABLE public.project_build_configuration OWNER TO newcastle;

--
-- Name: project_build_configuration_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE project_build_configuration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_build_configuration_id_seq OWNER TO newcastle;

--
-- Name: project_build_configuration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE project_build_configuration_id_seq OWNED BY project_build_configuration.id;


--
-- Name: project_build_result; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE project_build_result (
    id integer NOT NULL,
    build_driver_id character varying(255),
    build_log text,
    build_script character varying(255),
    build_status character varying(255),
    end_time timestamp without time zone,
    patches_url character varying(255),
    source_url character varying(255),
    start_time timestamp without time zone,
    systemimage bytea,
    project_build_configuration_id integer,
    user_id integer
);


ALTER TABLE public.project_build_result OWNER TO newcastle;

--
-- Name: project_build_result_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE project_build_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_build_result_id_seq OWNER TO newcastle;

--
-- Name: project_build_result_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE project_build_result_id_seq OWNED BY project_build_result.id;


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_id_seq OWNER TO newcastle;

--
-- Name: project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE project_id_seq OWNED BY project.id;


--
-- Name: system_image; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE system_image (
    id integer NOT NULL,
    description character varying(255),
    image_url character varying(255),
    name character varying(255) NOT NULL,
    environment_id integer
);


ALTER TABLE public.system_image OWNER TO newcastle;

--
-- Name: system_image_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE system_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.system_image_id_seq OWNER TO newcastle;

--
-- Name: system_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE system_image_id_seq OWNED BY system_image.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE users (
    id integer NOT NULL,
    email character varying(100) NOT NULL,
    first_name character varying(255),
    last_name character varying(255),
    username character varying(100) NOT NULL
);


ALTER TABLE public.users OWNER TO newcastle;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO newcastle;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY artifact ALTER COLUMN id SET DEFAULT nextval('artifact_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY build_collection ALTER COLUMN id SET DEFAULT nextval('build_collection_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY environment ALTER COLUMN id SET DEFAULT nextval('environment_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY license ALTER COLUMN id SET DEFAULT nextval('license_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY product ALTER COLUMN id SET DEFAULT nextval('product_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY product_version ALTER COLUMN id SET DEFAULT nextval('product_version_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY product_version_project ALTER COLUMN id SET DEFAULT nextval('product_version_project_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project ALTER COLUMN id SET DEFAULT nextval('project_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_configuration ALTER COLUMN id SET DEFAULT nextval('project_build_configuration_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_result ALTER COLUMN id SET DEFAULT nextval('project_build_result_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY system_image ALTER COLUMN id SET DEFAULT nextval('system_image_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- Name: artifact_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY artifact
    ADD CONSTRAINT artifact_pkey PRIMARY KEY (id);


--
-- Name: build_collection_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY build_collection
    ADD CONSTRAINT build_collection_pkey PRIMARY KEY (id);


--
-- Name: environment_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY environment
    ADD CONSTRAINT environment_pkey PRIMARY KEY (id);


--
-- Name: license_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY license
    ADD CONSTRAINT license_pkey PRIMARY KEY (id);


--
-- Name: product_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: product_version_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY product_version
    ADD CONSTRAINT product_version_pkey PRIMARY KEY (id);


--
-- Name: product_version_project_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY product_version_project
    ADD CONSTRAINT product_version_project_pkey PRIMARY KEY (id);


--
-- Name: project_build_configuration_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY project_build_configuration
    ADD CONSTRAINT project_build_configuration_pkey PRIMARY KEY (id);


--
-- Name: project_build_result_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY project_build_result
    ADD CONSTRAINT project_build_result_pkey PRIMARY KEY (id);


--
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- Name: system_image_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY system_image
    ADD CONSTRAINT system_image_pkey PRIMARY KEY (id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: fk_artifact_project_build_result; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY artifact
    ADD CONSTRAINT fk_artifact_project_build_result FOREIGN KEY (project_build_result_id) REFERENCES project_build_result(id);


--
-- Name: fk_build_collection_product_version; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY build_collection
    ADD CONSTRAINT fk_build_collection_product_version FOREIGN KEY (product_version_id) REFERENCES product_version(id);


--
-- Name: fk_build_collection_project_build_result_build_collection; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY build_collection_project_build_result
    ADD CONSTRAINT fk_build_collection_project_build_result_build_collection FOREIGN KEY (buildcollection_id) REFERENCES build_collection(id);


--
-- Name: fk_build_collection_project_build_result_project_build_result; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY build_collection_project_build_result
    ADD CONSTRAINT fk_build_collection_project_build_result_project_build_result FOREIGN KEY (projectbuildresult_id) REFERENCES project_build_result(id);


--
-- Name: fk_product_version_product; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY product_version
    ADD CONSTRAINT fk_product_version_product FOREIGN KEY (product_id) REFERENCES product(id);


--
-- Name: fk_product_version_project_product_version; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY product_version_project
    ADD CONSTRAINT fk_product_version_project_product_version FOREIGN KEY (product_version_id) REFERENCES product_version(id);


--
-- Name: fk_product_version_project_project; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY product_version_project
    ADD CONSTRAINT fk_product_version_project_project FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: fk_project_build_configuration_environment; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_configuration
    ADD CONSTRAINT fk_project_build_configuration_environment FOREIGN KEY (environment_id) REFERENCES environment(id);


--
-- Name: fk_project_build_configuration_parent; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_configuration
    ADD CONSTRAINT fk_project_build_configuration_parent FOREIGN KEY (parent_id) REFERENCES project_build_configuration(id);


--
-- Name: fk_project_build_configuration_product_version; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_configuration
    ADD CONSTRAINT fk_project_build_configuration_product_version FOREIGN KEY (product_version_id) REFERENCES product_version(id);


--
-- Name: fk_project_build_configuration_project; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_configuration
    ADD CONSTRAINT fk_project_build_configuration_project FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: fk_project_build_result_project_build_configuration; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_result
    ADD CONSTRAINT fk_project_build_result_project_build_configuration FOREIGN KEY (project_build_configuration_id) REFERENCES project_build_configuration(id);


--
-- Name: fk_project_build_result_user; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project_build_result
    ADD CONSTRAINT fk_project_build_result_user FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: fk_project_license; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fk_project_license FOREIGN KEY (license_id) REFERENCES license(id);


--
-- Name: fk_system_image_environment; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY system_image
    ADD CONSTRAINT fk_system_image_environment FOREIGN KEY (environment_id) REFERENCES environment(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

