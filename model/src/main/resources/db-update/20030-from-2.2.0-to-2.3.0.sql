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

-- [NCL-6895] Temp build alignment preference
BEGIN transaction;
    ALTER TABLE buildrecord ADD COLUMN alignmentpreference varchar(255);
    ALTER TABLE buildconfigsetrecord ADD COLUMN alignmentpreference varchar(255);
    UPDATE buildrecord SET alignmentpreference = 'PREFER_TEMPORARY' WHERE temporarybuild IS TRUE AND alignmentpreference IS NULL;
    UPDATE buildconfigsetrecord SET alignmentpreference = 'PREFER_TEMPORARY' WHERE temporarybuild IS TRUE AND alignmentpreference IS NULL;
COMMIT;
