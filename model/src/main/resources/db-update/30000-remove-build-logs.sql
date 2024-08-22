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

