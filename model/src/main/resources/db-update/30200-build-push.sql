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

-- Old table:

--                           Table "buildrecordpushresult"
--            Column           |          Type          | Collation | Nullable | Default
-- ----------------------------+------------------------+-----------+----------+---------
--  id                         | bigint                 |           | not null |
--  brewbuildid                | integer                |           |          |
--  brewbuildurl               | character varying(255) |           |          |
--  log                        | text                   |           |          |
--  status                     | character varying(255) |           |          |
--  tagprefix                  | character varying(255) |           |          |
--  buildrecord_id             | bigint                 |           |          |
--  productmilestonerelease_id | bigint                 |           |          |
--  userinitiator              | character varying(255) |           |          |


-- New tables

--                             Table "operation"
--        Column        |           Type           | Collation | Nullable | Default
-- ---------------------+--------------------------+-----------+----------+---------
--  operation_type      | character varying(31)    |           | not null |
--  id                  | bigint                   |           | not null |
--  endtime             | timestamp with time zone |           |          |
--  progressstatus      | character varying(255)   |           |          |
--  result              | character varying(255)   |           |          |
--  starttime           | timestamp with time zone |           |          |
--  submittime          | timestamp with time zone |           |          |
--  user_id             | integer                  |           | not null |
--  productmilestone_id | integer                  |           |          |
--  build_id            | bigint                   |           |          |

--                    Table "operation_parameters"
--     Column    |          Type           | Collation | Nullable | Default
-- --------------+-------------------------+-----------+----------+---------
--  operation_id | bigint                  |           | not null |
--  value        | character varying(8192) |           | not null |
--  key          | character varying(50)   |           | not null |

--                     Table "buildpushreport"
--    Column    |          Type          | Collation | Nullable | Default
-- -------------+------------------------+-----------+----------+---------
-- brewbuildid  | integer                |           | not null |
-- brewbuildurl | character varying(255) |           |          |
-- operation_id | bigint                 |           | not null |


BEGIN; -- New and updated tables

    ALTER table operation ADD build_id bigint;
    ALTER TABLE operation
        ADD CONSTRAINT fk_operation_buildrecord FOREIGN KEY (build_id) REFERENCES buildrecord(id);
    CREATE INDEX idx_operation_build_id ON operation(build_id);

    CREATE TABLE buildpushreport
    (
        brewbuildid  integer NOT NULL,
        brewbuildurl character varying(255),
        operation_id bigint  NOT NULL
    );
    ALTER TABLE buildpushreport
        ADD CONSTRAINT buildpushreport_pkey PRIMARY KEY (operation_id);
    ALTER TABLE buildpushreport
        ADD CONSTRAINT fk_buildpushreport_operation FOREIGN KEY (operation_id) REFERENCES operation(id);

COMMIT;

BEGIN; -- Data migration

    INSERT INTO operation
    SELECT
        'BuildPush' AS operation_type,
        bpr.id AS id,
        to_timestamp(((bpr.id >> 22) + 1577836800000 + 1800000)::double precision /1000) AS endtime,
        'FINISHED' AS progressstatus,
        CASE WHEN bpr.status = 'ACCEPTED' THEN 'TIMEOUT'
             WHEN bpr.status = 'SUCCESS' THEN 'SUCCESSFUL'
             WHEN bpr.status = 'REJECTED' THEN 'REJECTED'
             WHEN bpr.status = 'FAILED' THEN 'FAILED'
             WHEN bpr.status = 'SYSTEM_ERROR' THEN 'SYSTEM_ERROR'
             WHEN bpr.status = 'CANCELED' THEN 'CANCELLED'
             ELSE 'SYSTEM_ERROR'
        END as result,
        to_timestamp(((bpr.id >> 22) + 1577836800000 + 1)::double precision /1000) AS starttime,
        to_timestamp(((bpr.id >> 22) + 1577836800000 + 0)::double precision /1000) AS submittime,
        CASE WHEN bpr.userinitiator IS NULL THEN (SELECT id FROM usertable WHERE username = 'pnc-admin')
             ELSE (SELECT id FROM usertable WHERE username = bpr.userinitiator)
        END as user_id,
        NULL As productmilestone_id,
        bpr.buildrecord_id AS build_id
    FROM buildrecordpushresult bpr
    WHERE bpr.id > 10000000000000000;

    -- workaround for pre-base32 ids
    INSERT INTO operation
    SELECT
        'BuildPush' AS operation_type,
        -- this id will not match the time, but at least keeps original order
        (((bpr.id % 100000000) + 529820) << 22) + floor(random()*4194304)::bigint AS id,
        b.endtime + INTERVAL '30 minutes' AS endtime,
        'FINISHED' AS progressstatus,
        CASE WHEN bpr.status = 'ACCEPTED' THEN 'TIMEOUT'
             WHEN bpr.status = 'SUCCESS' THEN 'SUCCESSFUL'
             WHEN bpr.status = 'REJECTED' THEN 'REJECTED'
             WHEN bpr.status = 'FAILED' THEN 'FAILED'
             WHEN bpr.status = 'SYSTEM_ERROR' THEN 'SYSTEM_ERROR'
             WHEN bpr.status = 'CANCELED' THEN 'CANCELLED'
             ELSE 'SYSTEM_ERROR'
        END as result,
        b.endtime + INTERVAL '11 seconds' AS starttime,
        b.endtime + INTERVAL '10 second' AS submittime,
        CASE WHEN bpr.userinitiator IS NULL THEN (SELECT id FROM usertable WHERE username = 'pnc-admin')
             ELSE (SELECT id FROM usertable WHERE username = bpr.userinitiator)
        END as user_id,
        NULL As productmilestone_id,
        bpr.buildrecord_id AS build_id
    FROM buildrecordpushresult bpr
    JOIN buildrecord b ON bpr.buildrecord_id = b.id
    WHERE bpr.id < 10000000000000000;

    INSERT INTO operation_parameters
    SELECT
        bpr.id AS operation_id,
        CASE WHEN bpr.tagprefix IS NOT NULL AND bpr.tagprefix != '' THEN bpr.tagprefix
             ELSE pva.value
            END AS value,
            'tagPrefix' AS key
    FROM buildrecordpushresult bpr
        LEFT JOIN productmilestonerelease mr ON bpr.productmilestonerelease_id = mr.id
        LEFT JOIN productmilestone m ON mr.milestone_id = m.id
        LEFT JOIN product_version_attributes pva ON m.productversion_id = pva.product_version_id AND pva.key = 'BREW_TAG_PREFIX'
    WHERE (bpr.tagprefix IS NOT NULL AND bpr.tagprefix != '') OR pva.value IS NOT NULL;

    INSERT INTO buildpushreport
    SELECT
        bpr.brewbuildid AS brewbuildid,
        bpr.brewbuildurl AS brewbuildurl,
        bpr.id AS operation_id
    FROM buildrecordpushresult bpr
    WHERE status = 'SUCCESS';

COMMIT;

