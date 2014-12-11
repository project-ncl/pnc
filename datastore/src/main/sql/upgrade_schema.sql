
ALTER TABLE artifact
	DROP CONSTRAINT pk_artifact_id;

ALTER TABLE build_collection
	DROP CONSTRAINT pk_build_collection_id;

ALTER TABLE license
	DROP CONSTRAINT pk_license_id;

ALTER TABLE project
	DROP CONSTRAINT pk_project_id;

ALTER TABLE system_image
	DROP CONSTRAINT pk_system_image_id;

ALTER TABLE artifact
	DROP CONSTRAINT fk_artifact_build_result_id;

ALTER TABLE project
	DROP CONSTRAINT unique_project_name;

ALTER TABLE project
	DROP CONSTRAINT fk_project_license_id;

DROP TABLE build_collection_build_result;

DROP TABLE build_configuration;

DROP TABLE build_result;

DROP TABLE build_trigger;

DROP TABLE "user";

DROP SEQUENCE build_configuration_id_seq;

DROP SEQUENCE build_result_id_seq;

DROP SEQUENCE build_trigger_id_seq;

DROP SEQUENCE user_id_seq;

CREATE SEQUENCE environment_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE product_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE product_version_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE product_version_project_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE project_build_configuration_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE project_build_result_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE users_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

ALTER SEQUENCE artifact_id_seq
	OWNED BY artifact.id;

ALTER SEQUENCE build_collection_id_seq
	OWNED BY build_collection.id;

ALTER SEQUENCE license_id_seq
	OWNED BY license.id;

ALTER SEQUENCE project_id_seq
	OWNED BY project.id;

ALTER SEQUENCE system_image_id_seq
	OWNED BY system_image.id;

CREATE TABLE build_collection_project_build_result (
	buildcollection_id integer NOT NULL,
	projectbuildresult_id integer NOT NULL
);

CREATE TABLE environment (
	id integer DEFAULT nextval('environment_id_seq'::regclass) NOT NULL,
	build_type character varying(50) NOT NULL,
	operational_system character varying(50) NOT NULL
);

CREATE TABLE product (
	id integer DEFAULT nextval('product_id_seq'::regclass) NOT NULL,
	description character varying(255),
	name character varying(100) NOT NULL
);

CREATE TABLE product_version (
	id integer DEFAULT nextval('product_version_id_seq'::regclass) NOT NULL,
	version character varying(50) NOT NULL,
	product_id integer
);

CREATE TABLE product_version_project (
	id integer DEFAULT nextval('product_version_project_id_seq'::regclass) NOT NULL,
	product_version_id integer,
	project_id integer
);

CREATE TABLE project_build_configuration (
	id integer DEFAULT nextval('project_build_configuration_id_seq'::regclass) NOT NULL,
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

CREATE TABLE project_build_result (
	id integer DEFAULT nextval('project_build_result_id_seq'::regclass) NOT NULL,
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

CREATE TABLE users (
	id integer DEFAULT nextval('users_id_seq'::regclass) NOT NULL,
	email character varying(100) NOT NULL,
	first_name character varying(255),
	last_name character varying(255),
	username character varying(100) NOT NULL
);

ALTER TABLE artifact
	DROP COLUMN build_result_id,
	ADD COLUMN artifact_status character varying(255) NOT NULL,
	ADD COLUMN deploy_url character varying(255),
	ADD COLUMN identifier character varying(255) NOT NULL,
	ADD COLUMN repository_type character varying(255) NOT NULL,
	ADD COLUMN project_build_result_id integer,
	ALTER COLUMN id SET DEFAULT nextval('artifact_id_seq'::regclass),
	ALTER COLUMN checksum TYPE character varying(255) /* TYPE change - table: artifact original: character varying(50) new: character varying(255) */;

COMMENT ON TABLE artifact IS NULL;

ALTER TABLE build_collection
	DROP COLUMN name,
	DROP COLUMN description,
	ADD COLUMN product_build_number integer NOT NULL,
	ADD COLUMN product_milestone character varying(20) NOT NULL,
	ADD COLUMN product_version_id integer;

COMMENT ON TABLE build_collection IS NULL;

ALTER TABLE license
	DROP COLUMN full_text,
	ADD COLUMN full_content text NOT NULL,
	ALTER COLUMN full_name TYPE character varying(255) /* TYPE change - table: license original: character varying(100) new: character varying(255) */,
	ALTER COLUMN full_name SET NOT NULL,
	ALTER COLUMN ref_url TYPE character varying(255) /* TYPE change - table: license original: character varying(100) new: character varying(255) */,
	ALTER COLUMN short_name TYPE character varying(255) /* TYPE change - table: license original: character varying(20) new: character varying(255) */;

COMMENT ON TABLE license IS NULL;

ALTER TABLE project
	DROP COLUMN current_license_id,
	DROP COLUMN scm_url,
	ADD COLUMN license_id integer,
	ALTER COLUMN description TYPE character varying(255) /* TYPE change - table: project original: text new: character varying(255) */,
	ALTER COLUMN issue_tracker_url TYPE character varying(255) /* TYPE change - table: project original: character varying(50) new: character varying(255) */,
	ALTER COLUMN name TYPE character varying(100) /* TYPE change - table: project original: character varying(20) new: character varying(100) */,
	ALTER COLUMN project_url TYPE character varying(255) /* TYPE change - table: project original: character varying(50) new: character varying(255) */;

COMMENT ON TABLE project IS NULL;

COMMENT ON COLUMN project.issue_tracker_url IS NULL;

COMMENT ON COLUMN project.project_url IS NULL;

ALTER TABLE system_image
	DROP COLUMN image_blob,
	ADD COLUMN environment_id integer,
	ALTER COLUMN description TYPE character varying(255) /* TYPE change - table: system_image original: text new: character varying(255) */,
	ALTER COLUMN image_url TYPE character varying(255) /* TYPE change - table: system_image original: character varying(100) new: character varying(255) */,
	ALTER COLUMN name TYPE character varying(255) /* TYPE change - table: system_image original: character varying(20) new: character varying(255) */,
	ALTER COLUMN name SET NOT NULL;

COMMENT ON TABLE system_image IS NULL;

COMMENT ON COLUMN system_image.image_url IS NULL;

COMMENT ON COLUMN system_image.name IS NULL;

ALTER SEQUENCE environment_id_seq
	OWNED BY environment.id;

ALTER SEQUENCE product_id_seq
	OWNED BY product.id;

ALTER SEQUENCE product_version_id_seq
	OWNED BY product_version.id;

ALTER SEQUENCE product_version_project_id_seq
	OWNED BY product_version_project.id;

ALTER SEQUENCE project_build_configuration_id_seq
	OWNED BY project_build_configuration.id;

ALTER SEQUENCE project_build_result_id_seq
	OWNED BY project_build_result.id;

ALTER SEQUENCE users_id_seq
	OWNED BY users.id;

ALTER TABLE artifact
	ADD CONSTRAINT artifact_pkey PRIMARY KEY (id);

ALTER TABLE build_collection
	ADD CONSTRAINT build_collection_pkey PRIMARY KEY (id);

ALTER TABLE environment
	ADD CONSTRAINT environment_pkey PRIMARY KEY (id);

ALTER TABLE license
	ADD CONSTRAINT license_pkey PRIMARY KEY (id);

ALTER TABLE product
	ADD CONSTRAINT product_pkey PRIMARY KEY (id);

ALTER TABLE product_version
	ADD CONSTRAINT product_version_pkey PRIMARY KEY (id);

ALTER TABLE product_version_project
	ADD CONSTRAINT product_version_project_pkey PRIMARY KEY (id);

ALTER TABLE project
	ADD CONSTRAINT project_pkey PRIMARY KEY (id);

ALTER TABLE project_build_configuration
	ADD CONSTRAINT project_build_configuration_pkey PRIMARY KEY (id);

ALTER TABLE project_build_result
	ADD CONSTRAINT project_build_result_pkey PRIMARY KEY (id);

ALTER TABLE system_image
	ADD CONSTRAINT system_image_pkey PRIMARY KEY (id);

ALTER TABLE users
	ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE artifact
	ADD CONSTRAINT fk_artifact_project_build_result FOREIGN KEY (project_build_result_id) REFERENCES project_build_result(id);

ALTER TABLE build_collection
	ADD CONSTRAINT fk_build_collection_product_version FOREIGN KEY (product_version_id) REFERENCES product_version(id);

ALTER TABLE build_collection_project_build_result
	ADD CONSTRAINT fk_build_collection_project_build_result_build_collection FOREIGN KEY (buildcollection_id) REFERENCES build_collection(id);

ALTER TABLE build_collection_project_build_result
	ADD CONSTRAINT fk_build_collection_project_build_result_project_build_result FOREIGN KEY (projectbuildresult_id) REFERENCES project_build_result(id);

ALTER TABLE product_version
	ADD CONSTRAINT fk_product_version_product FOREIGN KEY (product_id) REFERENCES product(id);

ALTER TABLE product_version_project
	ADD CONSTRAINT fk_product_version_project_product_version FOREIGN KEY (product_version_id) REFERENCES product_version(id);

ALTER TABLE product_version_project
	ADD CONSTRAINT fk_product_version_project_project FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE project
	ADD CONSTRAINT fk_project_license FOREIGN KEY (license_id) REFERENCES license(id);

ALTER TABLE project_build_configuration
	ADD CONSTRAINT fk_project_build_configuration_environment FOREIGN KEY (environment_id) REFERENCES environment(id);

ALTER TABLE project_build_configuration
	ADD CONSTRAINT fk_project_build_configuration_parent FOREIGN KEY (parent_id) REFERENCES project_build_configuration(id);

ALTER TABLE project_build_configuration
	ADD CONSTRAINT fk_project_build_configuration_product_version FOREIGN KEY (product_version_id) REFERENCES product_version(id);

ALTER TABLE project_build_configuration
	ADD CONSTRAINT fk_project_build_configuration_project FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE project_build_result
	ADD CONSTRAINT fk_project_build_result_project_build_configuration FOREIGN KEY (project_build_configuration_id) REFERENCES project_build_configuration(id);

ALTER TABLE project_build_result
	ADD CONSTRAINT fk_project_build_result_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE system_image
	ADD CONSTRAINT fk_system_image_environment FOREIGN KEY (environment_id) REFERENCES environment(id);
