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
  -- Mark all builds that has an entry in _archived_buildrecords with attribute BUILD_ARCHIVED=true
  INSERT INTO build_record_attributes (build_record_id, value, key) SELECT id, 'true', 'BUILD_ARCHIVED' FROM _archived_buildrecords JOIN buildrecord ON buildrecord_id = id;
  -- Drop the archival trigger
  DROP TRIGGER archive_build_record_ait ON buildrecord;
COMMIT;
