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

    ALTER TABLE artifact ADD COLUMN reason varchar(200);
    ALTER TABLE artifact ADD COLUMN creationuser_id integer;
    ALTER TABLE artifact ADD COLUMN modificationuser_id integer;
    ALTER TABLE artifact ADD COLUMN creationtime DATA_TYPE timestamp with time zone;
    ALTER TABLE artifact ADD COLUMN modificationtime DATA_TYPE timestamp with time zone;

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
       creationuser_id integer,
       modificationuser_id integer,
       creationtime DATA_TYPE timestamp with time zone.
       modificationtime DATA_TYPE timestamp with time zone,
       reason varchar(200),
       artifactquality varchar(255) not null,
       primary key (id, rev)
    );

    CREATE INDEX idx_artifact_aud_creation_user ON artifact_aud (creationuser_id);
    CREATE INDEX idx_artifact_aud_modification_user ON artifact_aud (modificationuser_id);

    ALTER TABLE artifact_aud ADD CONSTRAINT fk_artifact_aud_creation_user
    FOREIGN KEY (creationuser_id) REFERENCES usertable(id);
    ALTER TABLE artifact_aud ADD CONSTRAINT fk_artifact_aud_modification_user
    FOREIGN KEY (modificationuser_id) REFERENCES usertable(id);
    ALTER TABLE artifact_aud ADD CONSTRAINT fk_artifact_aud_revinfo
    FOREIGN KEY (rev) REFERENCES revinfo(rev);

COMMIT;
