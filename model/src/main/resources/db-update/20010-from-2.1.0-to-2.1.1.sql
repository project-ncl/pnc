--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCL-6652] Add to the Artifacts model a new field purl
BEGIN transaction;
    ALTER TABLE artifact ADD COLUMN purl varchar(1024);
COMMIT;

BEGIN transaction;
    CREATE INDEX idx_artifact_purl ON artifact (purl text_pattern_ops);
COMMIT;


