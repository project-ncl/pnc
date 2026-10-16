--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;

    -- Mark all builds that has an entry in _archived_buildrecords with attribute BUILD_ARCHIVED=true (only builds that do not have BUILD_ARCHIVED=true already)
    INSERT INTO build_record_attributes (build_record_id, value, key) SELECT id, 'true', 'BUILD_ARCHIVED' FROM _archived_buildrecords JOIN buildrecord ON buildrecord_id = id LEFT JOIN build_record_attributes ON buildrecord.id = build_record_id AND key = 'BUILD_ARCHIVED' WHERE key IS NULL;
    -- Drop the archival trigger
    DROP TRIGGER archive_build_record_ait ON buildrecord;

    ALTER TABLE buildrecord DROP COLUMN buildlog;
    ALTER TABLE buildrecord DROP COLUMN buildLogMd5;
    ALTER TABLE buildrecord DROP COLUMN buildLogSha256;
    ALTER TABLE buildrecord DROP COLUMN buildLogSize;

    ALTER TABLE buildrecord DROP COLUMN repourLog;
    ALTER TABLE buildrecord DROP COLUMN repourLogMd5;
    ALTER TABLE buildrecord DROP COLUMN repourLogSha256;
    ALTER TABLE buildrecord DROP COLUMN repourLogSize;

COMMIT;

