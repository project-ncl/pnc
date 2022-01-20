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


--------------------------------------------------------------------------------
-- Start transaction
--------------------------------------------------------------------------------
BEGIN transaction;

--------------------------------------------------------------------------------
-- BuildEnvironment
--------------------------------------------------------------------------------
alter table buildenvironment add column deprecated boolean;
-- by default all existing buildenvironment are not deprecated
update buildenvironment set deprecated = false;
alter table buildenvironment alter column deprecated set not null;

--------------------------------------------------------------------------------
-- TargetRepository
--------------------------------------------------------------------------------
create sequence target_repository_repo_id_seq;

-- Updates required by new artifact repository relations
create table TargetRepository (
    id integer default nextval('target_repository_repo_id_seq') not null,
    temporaryRepo boolean not null,
    identifier varchar(255) not null,
    repositoryPath varchar(255) not null,
    repositoryType varchar(255) not null,
    primary key (id),
    unique (identifier, repositoryPath)
);
-- insert default repositories
insert into TargetRepository (id, temporaryRepo, identifier, repositoryPath, repositoryType) values (1, false, 'indy-maven', '/api/content/maven/group/builds-untested', 'MAVEN');
insert into TargetRepository (id, temporaryRepo, identifier, repositoryPath, repositoryType) values (2, true, 'indy-maven', '/api/content/maven/group/temporary-builds', 'MAVEN');
insert into TargetRepository (id, temporaryRepo, identifier, repositoryPath, repositoryType) values (3, false, 'indy-maven', '/api/content/maven/hosted/shared-imports', 'MAVEN');
insert into TargetRepository (id, temporaryRepo, identifier, repositoryPath, repositoryType) values (4, false, 'indy-http', '', 'GENERIC_PROXY');

-- Start the sequence id for target repository from last value = 5
-- We need to set last_value to 5 and not to 4 because the sequence has column
-- 'is_called' set to false
-- https://www.postgresql.org/docs/8.1/static/functions-sequence.html
alter sequence target_repository_repo_id_seq restart with 5;

--------------------------------------------------------------------------------
-- Artifact
--------------------------------------------------------------------------------
alter table Artifact add targetRepository_id integer;

alter table Artifact
    add constraint fk_artifact_targetRepository
    foreign key (targetRepository_id)
    references TargetRepository;

-- migrate data
-- old repotype 0 -> maven (1)
-- old repotype 3 -> generic proxy (4)
update Artifact set targetRepository_id = 1 where repotype = 0;
update Artifact set targetRepository_id = 4 where repotype = 3;

alter table Artifact alter column targetRepository_id set not null;
alter table Artifact drop column repotype;

-- Old uniqueness constraint (identifier, sha256)
alter table artifact
    drop CONSTRAINT uk_fh8aer1o1771nyq8atrp57iql;

-- New uniqueness constraint (identifier, sha256, targetrepository_id)
alter table Artifact
    add constraint uk_qlrbh99iffsgof0v4mxlh95vl unique (identifier, sha256, targetRepository_id);

--------------------------------------------------------------------------------
-- buildconfigsetrecord
--------------------------------------------------------------------------------
alter table buildconfigsetrecord add temporarybuild boolean;
update buildconfigsetrecord set temporarybuild = false;
alter table buildconfigsetrecord alter column temporarybuild set not null;

--------------------------------------------------------------------------------
-- BuildRecord
--
-- NCL-3531
-- TODO: do we need to compute the values?
--------------------------------------------------------------------------------
alter table buildrecord add temporarybuild boolean;
update buildrecord set temporarybuild = false;
alter table buildrecord alter column temporarybuild set not null;

alter table buildrecord
    add column buildlogmd5 varchar(255),
    add column buildlogsha256 varchar(255),
    add column buildlogsize int4,
    add column repourlogmd5 varchar(255),
    add column repourlogsha256 varchar(255),
    add column repourlogsize int4;

--------------------------------------------------------------------------------
-- Populate the new fields added to buildrecord with data
--------------------------------------------------------------------------------

-- Enable pgrcrypto extension for sha256 calculation
create extension pgcrypto;

-- buildlogmd5, buildlogsha256, buildlogsize
update
    buildrecord
set
    buildlogmd5 = md5(buildlog),
    buildlogsha256 = encode(digest(buildlog, 'sha256'), 'hex'),
    buildlogsize = length(buildlog)
where
    buildlog is not null;

-- repourlogmd5, repourlogsha256, repourlogsize
update
    buildrecord
set
    repourlogmd5 = md5(repourlog),
    repourlogsha256 = encode(digest(repourlog, 'sha256'), 'hex'),
    repourlogsize = length(repourlog)
where
    repourlog is not null;

-- Stop using pgrcypto extenstion
drop extension pgcrypto;

--------------------------------------------------------------------------------
-- BuildRecordPushResult
--------------------------------------------------------------------------------
create sequence build_record_push_result_id_seq;

create table BuildRecordPushResult (
    id int4 not null,
    brewBuildId int4,
    brewBuildUrl varchar(255),
    tagPrefix varchar(255),
    log text,
    status varchar(255),
    buildRecord_id int4,
    primary key (id)
);

alter table BuildRecordPushResult
    add constraint fk_buildrecordpushresult_buildrecord
    foreign key (buildRecord_id)
    references BuildRecord;

-- Copy data from build_record_attributes table to empty buildrecordpushresult table
INSERT INTO
    buildrecordpushresult (id, buildrecord_id, brewbuildid, brewbuildurl, status)
SELECT
    nextval('build_record_push_result_id_seq'), brID.build_record_id, cast(brID.value as int), brURL.value, 'SUCCESS'
FROM
    build_record_attributes brID
left join
    build_record_attributes brURL on brID.build_record_id = brURL.build_record_id AND brURL.key = 'brewLink'
WHERE
    brID.key = 'brewId';

--------------------------------------------------------------------------------
-- build_config_set_record_attributes collection table
--------------------------------------------------------------------------------
create table build_config_set_record_attributes (
    build_config_set_record_id int4 not null,
    value varchar(255),
    key varchar(255) not null,
    primary key (build_config_set_record_id, key)
);

alter table build_config_set_record_attributes
    add constraint fk_build_config_set_record_attributes_build_config_set_record
    foreign key (build_config_set_record_id)
    references BuildConfigSetRecord;

--------------------------------------------------------------------------------
-- Indexes
--------------------------------------------------------------------------------

-- Defined on Artifact.java
create index idx_artifact_targetrepository ON artifact (targetRepository_id);

-- Defined on BuildRecordPushResult.java
create index idx_buildrecordpushresult_buildrecord ON buildrecordpushresult (buildRecord_id);

-- BuildRecord index on join tables
create index idx_build_record_artifact_dependencies_map ON build_record_artifact_dependencies_map (dependency_artifact_id);
create index idx_build_record_built_artifact_map ON build_record_built_artifact_map (built_artifact_id);


--------------------------------------------------------------------------------
-- Commit changes
--------------------------------------------------------------------------------
COMMIT;
