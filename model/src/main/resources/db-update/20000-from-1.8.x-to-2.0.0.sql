--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

-- [NCL-5346] - Replace empty string with nulls
update buildconfiguration set description=null where description='';
update project set description=null where description='';
update project set projecturl=null where projecturl='';
update project set issuetrackerurl=null where issuetrackerurl='';
update product set description=null where description='';


-- [NCL-5638] - Remove Product.productCode and Product.pgmSystemName
ALTER TABLE product DROP COLUMN productcode;
ALTER TABLE product DROP COLUMN pgmsystemname;


-- [NCL-5799]: Remove fields from Product Milestone/Release
ALTER TABLE productrelease DROP COLUMN downloadUrl;
ALTER TABLE productrelease DROP COLUMN issueTrackerUrl;
ALTER TABLE productmilestone DROP COLUMN downloadUrl;
ALTER TABLE productmilestone DROP COLUMN issueTrackerUrl;


-- [NCL-5497] - Store User in BuildConfiguration and BuildConfigurationAudited
BEGIN transaction;

    ALTER TABLE buildconfiguration ADD column creationuser_id int4;
    ALTER TABLE buildconfiguration ADD column lastmodificationuser_id int4;
    ALTER TABLE buildconfiguration_aud ADD column creationuser_id int4;
    ALTER TABLE buildconfiguration_aud ADD column lastmodificationuser_id int4;

    CREATE INDEX idx_buildconfiguration_creation_user ON buildconfiguration (creationuser_id);
    CREATE INDEX idx_buildconfiguration_modification_user ON buildconfiguration (lastmodificationuser_id);
    CREATE INDEX idx_buildconfiguration_aud_creation_user ON buildconfiguration_aud (creationuser_id);
    CREATE INDEX idx_buildconfiguration_aud_modification_user ON buildconfiguration_aud (lastmodificationuser_id);

    ALTER TABLE buildconfiguration ADD CONSTRAINT fk_buildconfiguration_creation_user
    FOREIGN KEY (creationuser_id) REFERENCES usertable(id);

    ALTER TABLE buildconfiguration ADD CONSTRAINT fk_buildconfiguration_modification_user
    FOREIGN KEY (lastmodificationuser_id) REFERENCES usertable(id);

    ALTER TABLE buildconfiguration_aud ADD CONSTRAINT fk_buildconfiguration_aud_creation_user
    FOREIGN KEY (creationuser_id) REFERENCES usertable(id);

    ALTER TABLE buildconfiguration_aud ADD CONSTRAINT fk_buildconfiguration_aud_modification_user
    FOREIGN KEY (lastmodificationuser_id) REFERENCES usertable(id);

COMMIT;


-- [NCL-5661] - Backend support for MilestoneRelease logs
BEGIN transaction;

    DROP SEQUENCE public.build_record_push_result_id_seq;
    DROP SEQUENCE public.product_milestone_release_id_seq;

    ALTER TABLE public.buildrecordpushresult ALTER COLUMN id TYPE bigint;
    ALTER TABLE public.productmilestonerelease ALTER COLUMN id TYPE bigint;

    ALTER TABLE public.buildrecordpushresult ADD COLUMN productmilestonerelease_id bigint;

    ALTER TABLE ONLY public.buildrecordpushresult
    ADD CONSTRAINT fk_pushresult_milestonerelease FOREIGN KEY (productmilestonerelease_id) REFERENCES public.productmilestonerelease(id);

COMMIT;

-- [NCL-5677] - Update the PNC model related to Products and Projects to store the additional metadata required
BEGIN transaction;

    ALTER TABLE project ADD COLUMN engineeringTeam varchar(255);
    ALTER TABLE project ADD COLUMN technicalLeader varchar(255);

    ALTER TABLE product ADD COLUMN productManagers varchar(255);
    ALTER TABLE product ADD COLUMN productPagesCode varchar(50);

    ALTER TABLE productrelease ADD COLUMN commonPlatformEnumeration varchar(255);
    ALTER TABLE productrelease ADD COLUMN productPagesCode varchar(50);

COMMIT;


-- [NCL-5511] - Drop email not null constrain
BEGIN transaction;

ALTER TABLE usertable ALTER COLUMN email DROP NOT NULL;
COMMIT;


-- [NCL-5680] - Update the model related to Artifacts and revision of quality labels change
BEGIN transaction;

    ALTER TABLE artifact ADD COLUMN qualitylevelreason varchar(200);
    ALTER TABLE artifact ADD COLUMN creationuser_id integer;
    ALTER TABLE artifact ADD COLUMN modificationuser_id integer;
    ALTER TABLE artifact ADD COLUMN creationtime timestamptz;
    ALTER TABLE artifact ADD COLUMN modificationtime timestamptz;

    CREATE INDEX idx_artifact_creation_user ON artifact (creationuser_id);
    CREATE INDEX idx_artifact_modification_user ON artifact (modificationuser_id);

    ALTER TABLE artifact ADD CONSTRAINT fk_artifact_creation_user
    FOREIGN KEY (creationuser_id) REFERENCES usertable(id);
    ALTER TABLE artifact ADD CONSTRAINT fk_artifact_modification_user
    FOREIGN KEY (modificationuser_id) REFERENCES usertable(id);

    CREATE TABLE artifact_aud (
       id integer not null,
       rev integer not null,
       revtype SMALLINT,
       modificationuser_id integer,
       modificationtime timestamptz,
       qualityLevelReason varchar(200),
       artifactquality varchar(255),
       primary key (id, rev)
    );

    CREATE INDEX idx_artifact_aud_modification_user ON artifact_aud (modificationuser_id);

    ALTER TABLE artifact_aud ADD CONSTRAINT fk_artifact_aud_modification_user
    FOREIGN KEY (modificationuser_id) REFERENCES usertable(id);
    ALTER TABLE artifact_aud ADD CONSTRAINT fk_artifact_aud_revinfo
    FOREIGN KEY (rev) REFERENCES revinfo(rev);

COMMIT;

-- [NCL-5884] - Add a defaultAlignmentParams field in BuildConfiguration and BuildConfigurationAudited
BEGIN transaction;

    ALTER TABLE buildconfiguration ADD COLUMN defaultAlignmentParams text;
    ALTER TABLE buildconfiguration_aud ADD COLUMN defaultAlignmentParams text;

COMMIT;

-- [NCL-5923][NCL-3702] Add SQL migration for group config's new 'active' column, and populate them as true for existing group configs
BEGIN transaction;
    -- add column active
    ALTER TABLE buildconfigurationset ADD COLUMN active boolean;
    -- set all existing build configuration sets to be active
    UPDATE buildconfigurationset set active = true;
COMMIT;

-- Optimize query in BuilcConfig page (problems with loading http://orch.psi.redhat.com/pnc-web/#/projects/632/build-configs/2888 posted by Honza on PNC Users)
-- Commented as already executed by avibelli on Aug 5th, 2020
--
-- CREATE INDEX idx_build_configuration_parameters_aud_revinfo ON public.build_configuration_parameters_AUD USING btree (rev)
-- CREATE INDEX idx_build_configuration_parameters_aud_rev ON public.build_configuration_parameters_AUD USING btree (buildconfiguration_id, rev)

-- Insert NPM repositories
BEGIN transaction;
    insert into TargetRepository (temporaryRepo, identifier, repositoryPath, repositoryType) values (false, 'indy-npm', '/api/content/npm/group/builds-untested', 'NPM');
    insert into TargetRepository (temporaryRepo, identifier, repositoryPath, repositoryType) values (true, 'indy-npm', '/api/content/npm/group/temporary-builds', 'NPM');
    insert into TargetRepository (temporaryRepo, identifier, repositoryPath, repositoryType) values (false, 'indy-npm', '/api/content/npm/hosted/shared-imports', 'NPM');
COMMIT;

-- NCL-4888 - Change EXECUTION_ROOT_NAME parameter name
BEGIN transaction;
    update build_configuration_parameters set KEY='BREW_BUILD_NAME' where KEY='EXECUTION_ROOT_NAME';
    update build_configuration_parameters_aud set KEY='BREW_BUILD_NAME' where KEY='EXECUTION_ROOT_NAME';
COMMIT;

-- NCL-4679
BEGIN transaction;
    alter table buildrecord add column buildoutputchecksum varchar(255);
COMMIT;

-- NCL-5933 -- add a BR <-> BR OneToMany constraint to be able to specify no-rebuild cause
BEGIN transaction;
    ALTER TABLE buildrecord ADD COLUMN norebuildcause_id INTEGER;

    CREATE INDEX idx_buildrecord_norebuildcause ON buildrecord(norebuildcause_id);

    ALTER TABLE buildrecord ADD CONSTRAINT fk_buildrecord_norebuildcause
    FOREIGN KEY (norebuildcause_id) REFERENCES buildrecord(id);
COMMIT;


BEGIN transaction;

    -- Verify that there are no duplicate data
    do $$
    declare
        total_dupes integer;
    begin
        SELECT count(*)
        INTO total_dupes
        FROM (SELECT built_artifact_id FROM build_record_built_artifact_map GROUP BY built_artifact_id HAVING count(*) > 1) AS agg;

        assert total_dupes = 0;
    end$$;

    -- Add new column to artifact table
    ALTER TABLE artifact ADD column buildrecord_id int4;
    CREATE INDEX idx_artifact_buildrecord ON artifact (buildrecord_id);
    ALTER TABLE artifact ADD CONSTRAINT fk_artifact_buildrecord FOREIGN KEY (buildrecord_id) REFERENCES buildrecord(id);

    -- Copy data from old table
    UPDATE artifact
        SET buildrecord_id = build_record_built_artifact_map.build_record_id
        FROM build_record_built_artifact_map
        WHERE artifact.id = build_record_built_artifact_map.built_artifact_id;

    -- Drop the old table
    DROP TABLE build_record_built_artifact_map;

COMMIT;
