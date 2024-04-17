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

BEGIN;
ALTER TABLE buildconfigsetrecord ALTER COLUMN id SET DATA TYPE bigint;
-- update foreign bcsr keys
ALTER TABLE buildrecord ALTER COLUMN buildconfigsetrecord_id TYPE bigint;
ALTER TABLE build_config_set_record_attributes ALTER COLUMN build_config_set_record_id TYPE bigint;
-- update archived build records foreign key
ALTER TABLE _archived_buildrecords ALTER COLUMN buildconfigsetrecord_id TYPE bigint;

COMMIT;

