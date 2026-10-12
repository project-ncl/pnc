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

-- NCL-10027 - Remove Brew (push, pull, tag prefix, id references)

BEGIN;

    ALTER TABLE buildconfiguration DROP COLUMN brewpullactive;
    ALTER TABLE buildconfiguration_aud DROP COLUMN brewpullactive;
    ALTER TABLE _archived_buildrecords DROP COLUMN IF EXISTS brewpullactive;

    ALTER TABLE deliverableartifact DROP COLUMN brewbuildid;

    DROP INDEX idx_operation_build_id;
    ALTER TABLE operation DROP CONSTRAINT fk_operation_buildrecord;
    ALTER TABLE operation DROP COLUMN build_id;

    DROP TABLE buildpushreport;

    DELETE FROM operation_parameters
        WHERE operation_id IN (SELECT id FROM operation WHERE operation_type = 'BuildPush');
    DELETE FROM operation WHERE operation_type = 'BuildPush';

    DELETE FROM product_version_attributes WHERE key = 'BREW_TAG_PREFIX';

    UPDATE build_configuration_parameters SET key = 'EXECUTION_ROOT_NAME' WHERE key = 'BREW_BUILD_NAME';
    UPDATE build_configuration_parameters_aud SET key = 'EXECUTION_ROOT_NAME' WHERE key = 'BREW_BUILD_NAME';

COMMIT;
