--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;
    -- new column that deliverableartifactlicenseinfo.sourceUrl that replaces deliverableartifactlicenseinfo.source
    ALTER TABLE deliverableartifactlicenseinfo ADD COLUMN sourceUrl text;
    UPDATE deliverableartifactlicenseinfo SET sourceUrl = '';
    ALTER TABLE deliverableartifactlicenseinfo ALTER COLUMN sourceUrl SET NOT NULL;
    ALTER TABLE deliverableartifactlicenseinfo ALTER COLUMN spdxLicenseId SET NOT NULL;
COMMIT;