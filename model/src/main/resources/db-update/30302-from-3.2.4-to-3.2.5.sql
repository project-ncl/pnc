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

-- [NCL-9215] Change related to deliverable artifact now having a composite of
-- 3 keys
BEGIN;

ALTER table deliverableartifactlicenseinfo
ADD COLUMN delartifact_distribution_id bigint;

ALTER table deliverableartifactlicenseinfo
DROP CONSTRAINT fk_delartifact;

ALTER table deliverableartifactlicenseinfo
DROP CONSTRAINT fkmg9y25ryfimmkttpn72n5x86f;

ALTER table deliverableartifact
DROP CONSTRAINT deliverableartifact_pkey;

CREATE UNIQUE INDEX deliverableartifact_pkey ON deliverableartifact(artifact_id, report_id, distribution_id);

-- check if there are similar constraints in the table

ALTER table deliverableartifactlicenseinfo
ADD CONSTRAINT fk_delartifact FOREIGN KEY
    (delartifact_artifact_id, delartifact_report_id, delartifact_distribution_id)
REFERENCES deliverableartifact(artifact_id, report_id, distribution_id);

-- Now migrate the data
UPDATE deliverableartifactlicenseinfo license
SET delartifact_distribution_id =
    (SELECT distribution_id
     FROM deliverableartifact
     WHERE artifact_id = license.delartifact_artifact_id
         AND
           report_id = license.delartifact_report_id);

COMMIT;
