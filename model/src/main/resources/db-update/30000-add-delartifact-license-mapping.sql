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

-------------------------------------------------------------------------------
-- DeliverableArtifactLicenseInfo
--------------------------------------------------------------------------------
CREATE TABLE deliverableartifactlicenseinfo
(
    id                      BIGINT NOT NULL,
    spdxLicenseId           VARCHAR(255),
    name                    TEXT,
    url                     TEXT,
    comments                TEXT,
    distribution            VARCHAR(255),
    source                  VARCHAR(255),
    delartifact_report_id   BIGINT NOT NULL,
    delartifact_artifact_id INTEGER NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE deliverableartifactlicenseinfo
    ADD CONSTRAINT fk_delartifact FOREIGN KEY (delartifact_report_id, delartifact_artifact_id)
    REFERENCES deliverableartifact(report_id, artifact_id);

COMMIT;

