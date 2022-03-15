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

-- NCL-6695: Add Operation table
BEGIN;

    CREATE TABLE operation (
        operation_type varchar(50) NOT NULL,
        id bigint NOT NULL,
        endtime timestamptz,
        progressstatus varchar(255),
        result varchar(255),
        starttime timestamptz,
        submittime timestamptz,
        user_id integer NOT NULL,
        productmilestone_id integer,
        primary key (id)
    );

    CREATE INDEX idx_operation_user ON operation (user_id);
    ALTER TABLE operation ADD CONSTRAINT fk_operation_user
    FOREIGN KEY (user_id) REFERENCES usertable(id);

    CREATE INDEX idx_operation_milestone ON operation (productmilestone_id);
    ALTER TABLE operation ADD CONSTRAINT fk_operation_productmilestone
    FOREIGN KEY (productmilestone_id) REFERENCES productmilestone(id);


    CREATE TABLE operation_parameters (
        operation_id bigint NOT NULL,
        value varchar(8192) NOT NULL,
        key varchar(50) NOT NULL,
        primary key (operation_id, key)
    );

    ALTER TABLE operation_parameters ADD CONSTRAINT fk_operation_parameters_operation
    FOREIGN KEY (operation_id) REFERENCES operation(id);

COMMIT;
