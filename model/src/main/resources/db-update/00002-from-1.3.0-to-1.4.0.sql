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

-- insert npm repositories
insert into TargetRepository (temporaryRepo, identifier, repositoryPath, repositoryType) values (false, 'indy-npm', '/api/content/npm/group/builds-untested', 'NPM');
insert into TargetRepository (temporaryRepo, identifier, repositoryPath, repositoryType) values (true, 'indy-npm', '/api/content/npm/group/temporary-builds', 'NPM');
insert into TargetRepository (temporaryRepo, identifier, repositoryPath, repositoryType) values (false, 'indy-npm', '/api/content/npm/hosted/shared-imports', 'NPM');

-- insert new columns in build record
alter table buildrecord add column dependencybuildrecordids text;
alter table buildrecord add column dependentbuildrecordids text;
