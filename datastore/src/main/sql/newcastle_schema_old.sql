--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014-2022 Red Hat, Inc., and individual contributors
-- as indicated by the @author tags.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
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
-- Name: artifact; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE artifact (
    id integer NOT NULL,
    filename character varying(100) NOT NULL,
    build_result_id integer,
    checksum character varying(50)
);


ALTER TABLE public.artifact OWNER TO postgres;

--
-- Name: TABLE artifact; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE artifact IS 'Build Artifacts';


--
-- Name: artifact_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE artifact_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.artifact_id_seq OWNER TO postgres;

--
-- Name: build_collection_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE build_collection_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.build_collection_id_seq OWNER TO postgres;

--
-- Name: build_collection; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE build_collection (
    id integer DEFAULT nextval('build_collection_id_seq'::regclass) NOT NULL,
    name character varying(20) NOT NULL,
    description text
);


ALTER TABLE public.build_collection OWNER TO postgres;

--
-- Name: TABLE build_collection; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE build_collection IS 'Group of build results, such as all builds that are part of a single product release';


--
-- Name: build_collection_build_result; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE build_collection_build_result (
    build_collection_id integer NOT NULL,
    build_result_id integer NOT NULL
);


ALTER TABLE public.build_collection_build_result OWNER TO postgres;

--
-- Name: TABLE build_collection_build_result; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE build_collection_build_result IS 'Mapping between build results and build collections';


--
-- Name: build_configuration_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE build_configuration_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.build_configuration_id_seq OWNER TO postgres;

--
-- Name: build_configuration; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE build_configuration (
    id integer DEFAULT nextval('build_configuration_id_seq'::regclass) NOT NULL,
    project_id integer,
    build_script text,
    source_id character varying(30),
    system_image_id integer
);


ALTER TABLE public.build_configuration OWNER TO postgres;

--
-- Name: TABLE build_configuration; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE build_configuration IS 'Build environment configuration';


--
-- Name: COLUMN build_configuration.build_script; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN build_configuration.build_script IS 'Command line instructions to run the build';


--
-- Name: COLUMN build_configuration.source_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN build_configuration.source_id IS 'Unique ID of the source to build';


--
-- Name: COLUMN build_configuration.system_image_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN build_configuration.system_image_id IS 'ID of the system image to use to execute the build';


--
-- Name: build_result_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE build_result_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.build_result_id_seq OWNER TO postgres;

--
-- Name: build_result; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE build_result (
    id integer DEFAULT nextval('build_result_id_seq'::regclass) NOT NULL,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    source_url character varying(100),
    build_environment_description text,
    username character varying(20),
    build_log text,
    build_script text,
    project_id integer,
    project_name character varying(40),
    user_id integer
);


ALTER TABLE public.build_result OWNER TO postgres;

--
-- Name: TABLE build_result; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE build_result IS 'Result of a build such as the command used to execute the build, the log file, and whether the build was successful.';


--
-- Name: build_trigger_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE build_trigger_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.build_trigger_id_seq OWNER TO postgres;

--
-- Name: build_trigger; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE build_trigger (
    id integer DEFAULT nextval('build_trigger_id_seq'::regclass) NOT NULL,
    build_configuration_id integer,
    triggered_build_configuration_id integer
);


ALTER TABLE public.build_trigger OWNER TO postgres;

--
-- Name: TABLE build_trigger; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE build_trigger IS 'Relationship to allow one build to automatically trigger execution of another build';


--
-- Name: license_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE license_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.license_id_seq OWNER TO postgres;

--
-- Name: license; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE license (
    id integer DEFAULT nextval('license_id_seq'::regclass) NOT NULL,
    short_name character varying(20),
    full_name character varying(100),
    full_text text,
    ref_url character varying(100)
);


ALTER TABLE public.license OWNER TO postgres;

--
-- Name: TABLE license; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE license IS 'Software Licenses';


--
-- Name: project_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.project_id_seq OWNER TO postgres;

--
-- Name: project; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE project (
    name character varying(20) NOT NULL,
    id integer DEFAULT nextval('project_id_seq'::regclass) NOT NULL,
    current_license_id integer,
    description text,
    scm_url character varying(50),
    issue_tracker_url character varying(50),
    project_url character varying(50)
);


ALTER TABLE public.project OWNER TO postgres;

--
-- Name: TABLE project; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE project IS 'Project to be built';


--
-- Name: COLUMN project.scm_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN project.scm_url IS 'URL of the SCM repository';


--
-- Name: COLUMN project.issue_tracker_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN project.issue_tracker_url IS 'URL to the project issue tracking system';


--
-- Name: COLUMN project.project_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN project.project_url IS 'URL to homepage of the project';


--
-- Name: system_image_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE system_image_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.system_image_id_seq OWNER TO postgres;

--
-- Name: system_image; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE system_image (
    id integer DEFAULT nextval('system_image_id_seq'::regclass) NOT NULL,
    description text,
    image_blob bytea,
    image_url character varying(100),
    name character varying(20)
);


ALTER TABLE public.system_image OWNER TO postgres;

--
-- Name: TABLE system_image; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE system_image IS 'Build system image';


--
-- Name: COLUMN system_image.image_blob; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN system_image.image_blob IS 'Build system image file, this could be something like a VM or Docker image.';


--
-- Name: COLUMN system_image.image_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN system_image.image_url IS 'URL location of the system image to use for the build';


--
-- Name: COLUMN system_image.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN system_image.name IS 'Short name of the image for easy reference';


--
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_id_seq OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE "user" (
    id integer DEFAULT nextval('user_id_seq'::regclass) NOT NULL,
    username character varying(20) NOT NULL,
    first_name character varying(20),
    last_name character varying(20),
    email character varying(30)
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- Name: TABLE "user"; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE "user" IS 'System Users';


--
-- Name: pk_artifact_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY artifact
    ADD CONSTRAINT pk_artifact_id PRIMARY KEY (id);


--
-- Name: pk_build_collection_build_result; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY build_collection_build_result
    ADD CONSTRAINT pk_build_collection_build_result PRIMARY KEY (build_collection_id, build_result_id);


--
-- Name: pk_build_collection_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY build_collection
    ADD CONSTRAINT pk_build_collection_id PRIMARY KEY (id);


--
-- Name: pk_build_configuration_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY build_configuration
    ADD CONSTRAINT pk_build_configuration_id PRIMARY KEY (id);


--
-- Name: pk_build_result; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY build_result
    ADD CONSTRAINT pk_build_result PRIMARY KEY (id);


--
-- Name: pk_build_trigger_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY build_trigger
    ADD CONSTRAINT pk_build_trigger_id PRIMARY KEY (id);


--
-- Name: pk_license_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY license
    ADD CONSTRAINT pk_license_id PRIMARY KEY (id);


--
-- Name: pk_project_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT pk_project_id PRIMARY KEY (id);


--
-- Name: pk_system_image_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY system_image
    ADD CONSTRAINT pk_system_image_id PRIMARY KEY (id);


--
-- Name: pk_user_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT pk_user_id PRIMARY KEY (id);


--
-- Name: unique_project_name; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT unique_project_name UNIQUE (name);


--
-- Name: fk_artifact_build_result_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY artifact
    ADD CONSTRAINT fk_artifact_build_result_id FOREIGN KEY (build_result_id) REFERENCES build_result(id);


--
-- Name: fk_build_collection_build_result_build_collection_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_collection_build_result
    ADD CONSTRAINT fk_build_collection_build_result_build_collection_id FOREIGN KEY (build_collection_id) REFERENCES build_collection(id);


--
-- Name: fk_build_collection_build_result_build_result_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_collection_build_result
    ADD CONSTRAINT fk_build_collection_build_result_build_result_id FOREIGN KEY (build_result_id) REFERENCES build_result(id);


--
-- Name: fk_build_configuration_project_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_configuration
    ADD CONSTRAINT fk_build_configuration_project_id FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: fk_build_configuration_system_image_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_configuration
    ADD CONSTRAINT fk_build_configuration_system_image_id FOREIGN KEY (system_image_id) REFERENCES system_image(id);


--
-- Name: fk_build_result_project_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_result
    ADD CONSTRAINT fk_build_result_project_id FOREIGN KEY (project_id) REFERENCES project(id);


--
-- Name: fk_build_result_user_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_result
    ADD CONSTRAINT fk_build_result_user_id FOREIGN KEY (user_id) REFERENCES "user"(id);


--
-- Name: fk_build_trigger_build_configuration_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_trigger
    ADD CONSTRAINT fk_build_trigger_build_configuration_id FOREIGN KEY (build_configuration_id) REFERENCES build_configuration(id);


--
-- Name: fk_build_trigger_triggered_build_configuration_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY build_trigger
    ADD CONSTRAINT fk_build_trigger_triggered_build_configuration_id FOREIGN KEY (triggered_build_configuration_id) REFERENCES build_configuration(id);


--
-- Name: fk_project_license_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fk_project_license_id FOREIGN KEY (current_license_id) REFERENCES license(id);


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

