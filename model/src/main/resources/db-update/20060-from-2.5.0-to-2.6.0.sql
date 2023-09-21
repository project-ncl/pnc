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
    ALTER TABLE buildconfigsetrecord ALTER COLUMN id SET DATA TYPE bigint;
    -- update foreign bcsr keys
    ALTER TABLE buildrecord ALTER COLUMN buildconfigsetrecord_id TYPE bigint;
    ALTER TABLE build_config_set_record_attributes ALTER COLUMN build_config_set_record_id TYPE bigint;
    -- update archived build records foreign key
    ALTER TABLE _archived_buildrecords ALTER COLUMN buildconfigsetrecord_id TYPE bigint;

    CREATE TABLE build_configuration_align_strats
    (
        buildconfiguration_id integer                NOT NULL,
        allowlist             text,
        denylist              text,
        idallowlist           text,
        iddenylist            text,
        idranks               text,
        ranks                 text,
        dependencyscope       character varying(255) NOT NULL
    );

    ALTER TABLE build_configuration_align_strats
        ADD CONSTRAINT build_configuration_align_strats_pkey PRIMARY KEY (buildconfiguration_id, dependencyscope);

    ALTER TABLE build_configuration_align_strats
        ADD CONSTRAINT fk_align_strats_bc FOREIGN KEY (buildconfiguration_id) REFERENCES buildconfiguration(id);

    CREATE TABLE build_configuration_align_strats_aud
    (
        rev                   integer                NOT NULL,
        revtype               smallint               NOT NULL,
        buildconfiguration_id integer                NOT NULL,
        dependencyscope       character varying(255) NOT NULL,
        ranks                 text,
        idallowlist           text,
        denylist              text,
        iddenylist            text,
        idranks               text,
        allowlist             text
    );
    ALTER TABLE build_configuration_align_strats_aud
        ADD CONSTRAINT build_configuration_align_strats_aud_pkey PRIMARY KEY (rev, revtype, buildconfiguration_id, dependencyscope);

    ALTER TABLE build_configuration_align_strats_aud
        ADD CONSTRAINT fk_build_configuration_align_strats_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo(rev);

    ALTER TABLE build_configuration_align_strats_aud
        ADD CONSTRAINT fk_buildconfiguration_align_strats_aud_buildconfiguration FOREIGN KEY (buildconfiguration_id, rev) REFERENCES buildconfiguration_aud(id, rev);

    ALTER TABLE buildrecord ADD COLUMN scmbuildconfigrevision varchar(255);
    ALTER TABLE buildrecord ADD COLUMN scmbuildconfigrevisioninternal boolean;

--------------------------------------------------------------------------------
-- DeliverableAnalyzerReport
--------------------------------------------------------------------------------
CREATE TABLE delan_report
    (
        operation_id        BIGINT                  NOT NULL,
        labels              VARCHAR(1023),
        PRIMARY KEY (operation_id)
    );
    ALTER TABLE delan_report
        ADD CONSTRAINT fk_delanreport_operation
            FOREIGN KEY (operation_id)
            REFERENCES operation(id);

--------------------------------------------------------------------------------
-- DeliverableArtifact
--------------------------------------------------------------------------------
CREATE TABLE deliverable_artifact
    (
        report_id           BIGINT                  NOT NULL,
        artifact_id         INTEGER                 NOT NULL,
        built_from_source   BOOLEAN                 NOT NULL,
        brew_build_id       INTEGER,
        PRIMARY KEY (report_id, artifact_id)
    );
    ALTER TABLE deliverable_artifact
        ADD CONSTRAINT fk_deliverableartifact_report
            FOREIGN KEY (report_id)
            REFERENCES delan_report(operation_id);
    ALTER TABLE deliverable_artifact
        ADD CONSTRAINT fk_deliverableartifact_artifact
            FOREIGN KEY (artifact_id)
            REFERENCES artifact(id);

--------------------------------------------------------------------------------
-- DeliverableAnalyzerLabelEntry
--------------------------------------------------------------------------------
CREATE TABLE delan_label_entry
    (
        id                  INTEGER                 NOT NULL,
        report_id           BIGINT,
        order_id            INTEGER,
        entry_time          TIMESTAMP,
        user_id             INTEGER,
        reason              TEXT,
        delan_report_label  TEXT,
        change              TEXT,
        PRIMARY KEY (id)
    );
    ALTER TABLE delan_label_entry
        ADD CONSTRAINT uk_reportid_orderid
            UNIQUE (report_id, order_id);
    ALTER TABLE delan_label_entry
        ADD CONSTRAINT fk_delanlabelentry_report
            FOREIGN KEY (report_id)
            REFERENCES delan_report(operation_id);
    ALTER TABLE delan_label_entry
        ADD CONSTRAINT fk_delanlabelentry_user
            FOREIGN KEY (user_id)
            REFERENCES usertable(id);
COMMIT;
