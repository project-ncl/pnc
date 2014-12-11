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
    checksum character varying(255),
    deployurl character varying(255),
    filename character varying(100) NOT NULL,
    identifier character varying(255) NOT NULL,
    repotype character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    projectbuildresult_id integer
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
-- Name: buildcollection; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE buildcollection (
    id integer NOT NULL,
    productbuildbumber integer NOT NULL,
    productversion_id integer
);


ALTER TABLE public.buildcollection OWNER TO newcastle;

--
-- Name: buildcollection_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE buildcollection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.buildcollection_id_seq OWNER TO newcastle;

--
-- Name: buildcollection_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE buildcollection_id_seq OWNED BY buildcollection.id;


--
-- Name: buildcollection_projectbuildresult; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE buildcollection_projectbuildresult (
    buildcollections_id integer NOT NULL,
    projectbuildresult_id integer NOT NULL
);


ALTER TABLE public.buildcollection_projectbuildresult OWNER TO newcastle;

--
-- Name: environment; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE environment (
    id integer NOT NULL,
    buildtype character varying(50) NOT NULL,
    operationalsystem character varying(50) NOT NULL
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
    fullcontent text NOT NULL,
    fullname character varying(255) NOT NULL,
    refurl character varying(255),
    shortname character varying(255)
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
    milestone character varying(20) NOT NULL,
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
-- Name: productversion; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE productversion (
    id integer NOT NULL,
    version character varying(50) NOT NULL,
    product_id integer
);


ALTER TABLE public.productversion OWNER TO newcastle;

--
-- Name: productversion_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE productversion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.productversion_id_seq OWNER TO newcastle;

--
-- Name: productversion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE productversion_id_seq OWNED BY productversion.id;


--
-- Name: productversionproject; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE productversionproject (
    id integer NOT NULL,
    productversion_id integer,
    project_id integer
);


ALTER TABLE public.productversionproject OWNER TO newcastle;

--
-- Name: productversionproject_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE productversionproject_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.productversionproject_id_seq OWNER TO newcastle;

--
-- Name: productversionproject_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE productversionproject_id_seq OWNED BY productversionproject.id;


--
-- Name: project; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE project (
    id integer NOT NULL,
    description character varying(255),
    issuetrackerurl character varying(255),
    name character varying(100) NOT NULL,
    projecturl character varying(255),
    license_id integer
);


ALTER TABLE public.project OWNER TO newcastle;

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
-- Name: projectbuildconfiguration; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE projectbuildconfiguration (
    id integer NOT NULL,
    buildscript character varying(255) NOT NULL,
    creationtime timestamp without time zone,
    identifier character varying(255) NOT NULL,
    lastmodificationtime timestamp without time zone,
    patchesurl character varying(255),
    repositories character varying(255),
    scmurl character varying(255) NOT NULL,
    environment_id integer,
    parent_id integer,
    productversion_id integer,
    project_id integer
);


ALTER TABLE public.projectbuildconfiguration OWNER TO newcastle;

--
-- Name: projectbuildconfiguration_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE projectbuildconfiguration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.projectbuildconfiguration_id_seq OWNER TO newcastle;

--
-- Name: projectbuildconfiguration_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE projectbuildconfiguration_id_seq OWNED BY projectbuildconfiguration.id;


--
-- Name: projectbuildresult; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE projectbuildresult (
    id integer NOT NULL,
    builddriverid character varying(255),
    buildlog text,
    buildscript character varying(255),
    endtime timestamp without time zone,
    patchesurl character varying(255),
    sourceurl character varying(255),
    starttime timestamp without time zone,
    status character varying(255),
    projectbuildconfiguration_id integer,
    systemimage_id integer,
    user_id integer
);


ALTER TABLE public.projectbuildresult OWNER TO newcastle;

--
-- Name: projectbuildresult_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE projectbuildresult_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.projectbuildresult_id_seq OWNER TO newcastle;

--
-- Name: projectbuildresult_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE projectbuildresult_id_seq OWNED BY projectbuildresult.id;


--
-- Name: systemimage; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE systemimage (
    id integer NOT NULL,
    description character varying(255),
    imageurl character varying(255),
    name character varying(255) NOT NULL,
    environment_id integer
);


ALTER TABLE public.systemimage OWNER TO newcastle;

--
-- Name: systemimage_id_seq; Type: SEQUENCE; Schema: public; Owner: newcastle
--

CREATE SEQUENCE systemimage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.systemimage_id_seq OWNER TO newcastle;

--
-- Name: systemimage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: newcastle
--

ALTER SEQUENCE systemimage_id_seq OWNED BY systemimage.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: newcastle; Tablespace: 
--

CREATE TABLE users (
    id integer NOT NULL,
    email character varying(100) NOT NULL,
    firstname character varying(255),
    lastname character varying(255),
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

ALTER TABLE ONLY buildcollection ALTER COLUMN id SET DEFAULT nextval('buildcollection_id_seq'::regclass);


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

ALTER TABLE ONLY productversion ALTER COLUMN id SET DEFAULT nextval('productversion_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY productversionproject ALTER COLUMN id SET DEFAULT nextval('productversionproject_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project ALTER COLUMN id SET DEFAULT nextval('project_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildconfiguration ALTER COLUMN id SET DEFAULT nextval('projectbuildconfiguration_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildresult ALTER COLUMN id SET DEFAULT nextval('projectbuildresult_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY systemimage ALTER COLUMN id SET DEFAULT nextval('systemimage_id_seq'::regclass);


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
-- Name: buildcollection_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY buildcollection
    ADD CONSTRAINT buildcollection_pkey PRIMARY KEY (id);


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
-- Name: productversion_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY productversion
    ADD CONSTRAINT productversion_pkey PRIMARY KEY (id);


--
-- Name: productversionproject_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY productversionproject
    ADD CONSTRAINT productversionproject_pkey PRIMARY KEY (id);


--
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- Name: projectbuildconfiguration_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY projectbuildconfiguration
    ADD CONSTRAINT projectbuildconfiguration_pkey PRIMARY KEY (id);


--
-- Name: projectbuildresult_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY projectbuildresult
    ADD CONSTRAINT projectbuildresult_pkey PRIMARY KEY (id);


--
-- Name: systemimage_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY systemimage
    ADD CONSTRAINT systemimage_pkey PRIMARY KEY (id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: newcastle; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: fk_artifact_projectbuildresult; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY artifact
    ADD CONSTRAINT fk_artifact_projectbuildresult FOREIGN KEY (projectbuildresult_id) REFERENCES projectbuildresult(id);


--
-- Name: fk_buildcollection_productversion; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY buildcollection
    ADD CONSTRAINT fk_buildcollection_productversion FOREIGN KEY (productversion_id) REFERENCES productversion(id);


--
-- Name: fk_buildcollection_projectbuildresult_buildcollection; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY buildcollection_projectbuildresult
    ADD CONSTRAINT fk_buildcollection_projectbuildresult_buildcollection FOREIGN KEY (buildcollections_id) REFERENCES buildcollection(id);


--
-- Name: fk_buildcollection_projectbuildresult_projectbuildresult; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY buildcollection_projectbuildresult
    ADD CONSTRAINT fk_buildcollection_projectbuildresult_projectbuildresult FOREIGN KEY (projectbuildresult_id) REFERENCES projectbuildresult(id);


--
-- Name: fk_productversion_product; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY productversion
    ADD CONSTRAINT fk_productversion_product FOREIGN KEY (product_id) REFERENCES product(id);


--
-- Name: fk_productversionproject_productversion; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY productversionproject
    ADD CONSTRAINT fk_productversionproject_productversion FOREIGN KEY (productversion_id) REFERENCES productversion(id);


--
-- Name: fk_productversionproject_project; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY productversionproject
    ADD CONSTRAINT fk_productversionproject_project FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: fk_project_license; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fk_project_license FOREIGN KEY (license_id) REFERENCES license(id);


--
-- Name: fk_projectbuildconfiguration_environment; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildconfiguration
    ADD CONSTRAINT fk_projectbuildconfiguration_environment FOREIGN KEY (environment_id) REFERENCES environment(id);


--
-- Name: fk_projectbuildconfiguration_parent; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildconfiguration
    ADD CONSTRAINT fk_projectbuildconfiguration_parent FOREIGN KEY (parent_id) REFERENCES projectbuildconfiguration(id);


--
-- Name: fk_projectbuildconfiguration_productversion; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildconfiguration
    ADD CONSTRAINT fk_projectbuildconfiguration_productversion FOREIGN KEY (productversion_id) REFERENCES productversion(id);


--
-- Name: fk_projectbuildconfiguration_project; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildconfiguration
    ADD CONSTRAINT fk_projectbuildconfiguration_project FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: fk_projectbuildresult_projectbuildconfiguration; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildresult
    ADD CONSTRAINT fk_projectbuildresult_projectbuildconfiguration FOREIGN KEY (projectbuildconfiguration_id) REFERENCES projectbuildconfiguration(id);


--
-- Name: fk_projectbuildresult_systemimage; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildresult
    ADD CONSTRAINT fk_projectbuildresult_systemimage FOREIGN KEY (systemimage_id) REFERENCES systemimage(id);


--
-- Name: fk_projectbuildresult_user; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY projectbuildresult
    ADD CONSTRAINT fk_projectbuildresult_user FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: fk_systemimage_environment; Type: FK CONSTRAINT; Schema: public; Owner: newcastle
--

ALTER TABLE ONLY systemimage
    ADD CONSTRAINT fk_systemimage_environment FOREIGN KEY (environment_id) REFERENCES environment(id);


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

