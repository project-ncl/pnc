
ALTER TABLE artifact
	DROP CONSTRAINT pk_artifact_id;

ALTER TABLE license
	DROP CONSTRAINT pk_license_id;

ALTER TABLE project
	DROP CONSTRAINT pk_project_id;

ALTER TABLE artifact
	DROP CONSTRAINT fk_artifact_build_result_id;

ALTER TABLE project
	DROP CONSTRAINT unique_project_name;

ALTER TABLE project
	DROP CONSTRAINT fk_project_license_id;

DROP TABLE build_collection;

DROP TABLE build_collection_build_result;

DROP TABLE build_configuration;

DROP TABLE build_result;

DROP TABLE build_trigger;

DROP TABLE system_image;

DROP TABLE "user";

DROP SEQUENCE build_collection_id_seq;

DROP SEQUENCE build_configuration_id_seq;

DROP SEQUENCE build_result_id_seq;

DROP SEQUENCE build_trigger_id_seq;

DROP SEQUENCE system_image_id_seq;

DROP SEQUENCE user_id_seq;

CREATE SEQUENCE buildcollection_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

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

CREATE SEQUENCE productversion_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE productversionproject_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE projectbuildconfiguration_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE projectbuildresult_id_seq
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE systemimage_id_seq
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

ALTER SEQUENCE license_id_seq
	OWNED BY license.id;

ALTER SEQUENCE project_id_seq
	OWNED BY project.id;

CREATE TABLE buildcollection (
	id integer DEFAULT nextval('buildcollection_id_seq'::regclass) NOT NULL,
	productbuildbumber integer NOT NULL,
	productversion_id integer
);

CREATE TABLE buildcollection_projectbuildresult (
	buildcollections_id integer NOT NULL,
	projectbuildresult_id integer NOT NULL
);

CREATE TABLE environment (
	id integer DEFAULT nextval('environment_id_seq'::regclass) NOT NULL,
	buildtype character varying(50) NOT NULL,
	operationalsystem character varying(50) NOT NULL
);

CREATE TABLE product (
	id integer DEFAULT nextval('product_id_seq'::regclass) NOT NULL,
	description character varying(255),
	milestone character varying(20) NOT NULL,
	name character varying(100) NOT NULL
);

CREATE TABLE productversion (
	id integer DEFAULT nextval('productversion_id_seq'::regclass) NOT NULL,
	version character varying(50) NOT NULL,
	product_id integer
);

CREATE TABLE productversionproject (
	id integer DEFAULT nextval('productversionproject_id_seq'::regclass) NOT NULL,
	productversion_id integer,
	project_id integer
);

CREATE TABLE projectbuildconfiguration (
	id integer DEFAULT nextval('projectbuildconfiguration_id_seq'::regclass) NOT NULL,
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

CREATE TABLE projectbuildresult (
	id integer DEFAULT nextval('projectbuildresult_id_seq'::regclass) NOT NULL,
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

CREATE TABLE systemimage (
	id integer DEFAULT nextval('systemimage_id_seq'::regclass) NOT NULL,
	description character varying(255),
	imageurl character varying(255),
	name character varying(255) NOT NULL,
	environment_id integer
);

CREATE TABLE users (
	id integer DEFAULT nextval('users_id_seq'::regclass) NOT NULL,
	email character varying(100) NOT NULL,
	firstname character varying(255),
	lastname character varying(255),
	username character varying(100) NOT NULL
);

ALTER TABLE artifact
	DROP COLUMN build_result_id,
	ADD COLUMN deployurl character varying(255),
	ADD COLUMN identifier character varying(255) NOT NULL,
	ADD COLUMN repotype character varying(255) NOT NULL,
	ADD COLUMN status character varying(255) NOT NULL,
	ADD COLUMN projectbuildresult_id integer,
	ALTER COLUMN id SET DEFAULT nextval('artifact_id_seq'::regclass),
	ALTER COLUMN checksum TYPE character varying(255) /* TYPE change - table: artifact original: character varying(50) new: character varying(255) */;

COMMENT ON TABLE artifact IS NULL;

ALTER TABLE license
	DROP COLUMN short_name,
	DROP COLUMN full_name,
	DROP COLUMN full_text,
	DROP COLUMN ref_url,
	ADD COLUMN fullcontent text NOT NULL,
	ADD COLUMN fullname character varying(255) NOT NULL,
	ADD COLUMN refurl character varying(255),
	ADD COLUMN shortname character varying(255);

COMMENT ON TABLE license IS NULL;

ALTER TABLE project
	DROP COLUMN current_license_id,
	DROP COLUMN scm_url,
	DROP COLUMN issue_tracker_url,
	DROP COLUMN project_url,
	ADD COLUMN issuetrackerurl character varying(255),
	ADD COLUMN projecturl character varying(255),
	ADD COLUMN license_id integer,
	ALTER COLUMN description TYPE character varying(255) /* TYPE change - table: project original: text new: character varying(255) */,
	ALTER COLUMN name TYPE character varying(100) /* TYPE change - table: project original: character varying(20) new: character varying(100) */;

COMMENT ON TABLE project IS NULL;

ALTER SEQUENCE buildcollection_id_seq
	OWNED BY buildcollection.id;

ALTER SEQUENCE environment_id_seq
	OWNED BY environment.id;

ALTER SEQUENCE product_id_seq
	OWNED BY product.id;

ALTER SEQUENCE productversion_id_seq
	OWNED BY productversion.id;

ALTER SEQUENCE productversionproject_id_seq
	OWNED BY productversionproject.id;

ALTER SEQUENCE projectbuildconfiguration_id_seq
	OWNED BY projectbuildconfiguration.id;

ALTER SEQUENCE projectbuildresult_id_seq
	OWNED BY projectbuildresult.id;

ALTER SEQUENCE systemimage_id_seq
	OWNED BY systemimage.id;

ALTER SEQUENCE users_id_seq
	OWNED BY users.id;

ALTER TABLE artifact
	ADD CONSTRAINT artifact_pkey PRIMARY KEY (id);

ALTER TABLE buildcollection
	ADD CONSTRAINT buildcollection_pkey PRIMARY KEY (id);

ALTER TABLE environment
	ADD CONSTRAINT environment_pkey PRIMARY KEY (id);

ALTER TABLE license
	ADD CONSTRAINT license_pkey PRIMARY KEY (id);

ALTER TABLE product
	ADD CONSTRAINT product_pkey PRIMARY KEY (id);

ALTER TABLE productversion
	ADD CONSTRAINT productversion_pkey PRIMARY KEY (id);

ALTER TABLE productversionproject
	ADD CONSTRAINT productversionproject_pkey PRIMARY KEY (id);

ALTER TABLE project
	ADD CONSTRAINT project_pkey PRIMARY KEY (id);

ALTER TABLE projectbuildconfiguration
	ADD CONSTRAINT projectbuildconfiguration_pkey PRIMARY KEY (id);

ALTER TABLE projectbuildresult
	ADD CONSTRAINT projectbuildresult_pkey PRIMARY KEY (id);

ALTER TABLE systemimage
	ADD CONSTRAINT systemimage_pkey PRIMARY KEY (id);

ALTER TABLE users
	ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE artifact
	ADD CONSTRAINT fk_artifact_projectbuildresult FOREIGN KEY (projectbuildresult_id) REFERENCES projectbuildresult(id);

ALTER TABLE buildcollection
	ADD CONSTRAINT fk_buildcollection_productversion FOREIGN KEY (productversion_id) REFERENCES productversion(id);

ALTER TABLE buildcollection_projectbuildresult
	ADD CONSTRAINT fk_buildcollection_projectbuildresult_buildcollection FOREIGN KEY (buildcollections_id) REFERENCES buildcollection(id);

ALTER TABLE buildcollection_projectbuildresult
	ADD CONSTRAINT fk_buildcollection_projectbuildresult_projectbuildresult FOREIGN KEY (projectbuildresult_id) REFERENCES projectbuildresult(id);

ALTER TABLE productversion
	ADD CONSTRAINT fk_productversion_product FOREIGN KEY (product_id) REFERENCES product(id);

ALTER TABLE productversionproject
	ADD CONSTRAINT fk_productversionproject_productversion FOREIGN KEY (productversion_id) REFERENCES productversion(id);

ALTER TABLE productversionproject
	ADD CONSTRAINT fk_productversionproject_project FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE project
	ADD CONSTRAINT fk_project_license FOREIGN KEY (license_id) REFERENCES license(id);

ALTER TABLE projectbuildconfiguration
	ADD CONSTRAINT fk_projectbuildconfiguration_environment FOREIGN KEY (environment_id) REFERENCES environment(id);

ALTER TABLE projectbuildconfiguration
	ADD CONSTRAINT fk_projectbuildconfiguration_parent FOREIGN KEY (parent_id) REFERENCES projectbuildconfiguration(id);

ALTER TABLE projectbuildconfiguration
	ADD CONSTRAINT fk_projectbuildconfiguration_productversion FOREIGN KEY (productversion_id) REFERENCES productversion(id);

ALTER TABLE projectbuildconfiguration
	ADD CONSTRAINT fk_projectbuildconfiguration_project FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE projectbuildresult
	ADD CONSTRAINT fk_projectbuildresult_projectbuildconfiguration FOREIGN KEY (projectbuildconfiguration_id) REFERENCES projectbuildconfiguration(id);

ALTER TABLE projectbuildresult
	ADD CONSTRAINT fk_projectbuildresult_systemimage FOREIGN KEY (systemimage_id) REFERENCES systemimage(id);

ALTER TABLE projectbuildresult
	ADD CONSTRAINT fk_projectbuildresult_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE systemimage
	ADD CONSTRAINT fk_systemimage_environment FOREIGN KEY (environment_id) REFERENCES environment(id);
