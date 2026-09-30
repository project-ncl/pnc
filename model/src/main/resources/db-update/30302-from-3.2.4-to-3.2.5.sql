--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
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
