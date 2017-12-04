--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014 Red Hat, Inc., and individual contributors
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

-- ## Updates required by new artifact repository relations
create table TargetRepository (
    id integer not null,
    identifier varchar(255) not null,
    repositoryPath varchar(255) not null,
    repositoryType varchar(255) not null,
    primary key (id)
);
-- insert default repositories
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (1, 'indy-maven', 'builds-untested', 'MAVEN');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (2, 'indy-maven', 'builds-untested-temp', 'MAVEN_TEMPORARY');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (3, 'indy-maven', 'shared-imports', 'MAVEN');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (4, 'indy-maven', 'shared-imports', 'MAVEN_TEMPORARY');
insert into TargetRepository (id, identifier, repositoryPath, repositoryType) values (5, 'indy-http', '', 'GENERIC_PROXY');

alter table Artifact add targetRepository_id integer;

-- Start the sequence id for target repository from last value = 6
-- We need to set last_value to 6 and not to 5 because the sequence has column
-- 'is_called' set to false
-- https://www.postgresql.org/docs/8.1/static/functions-sequence.html
alter sequence target_repository_repo_id_seq restart with 6;

-- migrate data
-- old repotype 0 -> maven (1)
-- old repotype 3 -> generic proxy (5)
update Artifact set targetRepository_id = 1 where repotype = 0;
update Artifact set targetRepository_id = 5 where repotype = 3;
alter table Artifact alter column targetRepository_id set not null;
alter table Artifact drop column repotype;

-- temporary build flags
alter table buildrecord add temporarybuild boolean;
update buildrecord set temporarybuild = true;
alter table buildrecord alter column temporarybuild set not null;

alter table buildconfigsetrecord add temporarybuild boolean;
update buildconfigsetrecord set temporarybuild = true;
alter table buildconfigsetrecord alter column temporarybuild set not null;

