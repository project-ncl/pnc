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

-- NCL-10018 - Remove SERVICE Build category
BEGIN;

-- UPDATE artifact SET buildCategory = 'STANDARD' WHERE buildCategory = 'SERVICE';
-- UPDATE artifact_aud SET buildCategory = 'STANDARD' WHERE buildCategory = 'SERVICE';

UPDATE build_configuration_parameters SET value = 'STANDARD'
    WHERE key = 'BUILD_CATEGORY' AND value = 'SERVICE';
UPDATE build_configuration_parameters_aud SET value = 'STANDARD'
    WHERE key = 'BUILD_CATEGORY' AND value = 'SERVICE';

COMMIT;
