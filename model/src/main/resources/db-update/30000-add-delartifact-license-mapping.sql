--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
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
    source                  VARCHAR(255) NOT NULL,
    delartifact_report_id   BIGINT NOT NULL,
    delartifact_artifact_id INTEGER NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE deliverableartifactlicenseinfo
    ADD CONSTRAINT fk_delartifact FOREIGN KEY (delartifact_report_id, delartifact_artifact_id)
    REFERENCES deliverableartifact(report_id, artifact_id);

COMMIT;

