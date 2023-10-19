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


--------------------------------------------------------------------------------
-- DeliverableAnalyzerReport
--------------------------------------------------------------------------------
CREATE TABLE deliverableanalyzerreport
(
    operation_id        BIGINT                  NOT NULL,
    labels              TEXT,
    PRIMARY KEY (operation_id)
);
ALTER TABLE deliverableanalyzerreport
    ADD CONSTRAINT fk_deliverableanalyzerreport_operation
        FOREIGN KEY (operation_id)
            REFERENCES operation(id);

--------------------------------------------------------------------------------
-- DeliverableArtifact
--------------------------------------------------------------------------------
CREATE TABLE deliverableartifact
(
    report_id           BIGINT                  NOT NULL,
    artifact_id         INTEGER                 NOT NULL,
    builtfromsource     BOOLEAN                 NOT NULL,
    brewbuildid         INTEGER,
    PRIMARY KEY (report_id, artifact_id)
);
ALTER TABLE deliverableartifact
    ADD CONSTRAINT fk_deliverableartifact_report
        FOREIGN KEY (report_id)
            REFERENCES deliverableanalyzerreport(operation_id);
ALTER TABLE deliverableartifact
    ADD CONSTRAINT fk_deliverableartifact_artifact
        FOREIGN KEY (artifact_id)
            REFERENCES artifact(id);

--------------------------------------------------------------------------------
-- DeliverableAnalyzerLabelEntry
--------------------------------------------------------------------------------
CREATE TABLE deliverableanalyzerlabelentry
(
    id              INTEGER                 NOT NULL,
    report_id       BIGINT,
    changeorder     INTEGER,
    entry_time      TIMESTAMP,
    user_id         INTEGER,
    reason          TEXT,
    label           TEXT,
    change          TEXT,
    PRIMARY KEY (id)
);
ALTER TABLE deliverableanalyzerlabelentry
    ADD CONSTRAINT uk_reportid_orderid
        UNIQUE (report_id, changeorder);
ALTER TABLE deliverableanalyzerlabelentry
    ADD CONSTRAINT fk_deliverableanalyzerlabelentry_report
        FOREIGN KEY (report_id)
            REFERENCES deliverableanalyzerreport(operation_id);
ALTER TABLE deliverableanalyzerlabelentry
    ADD CONSTRAINT fk_deliverableanalyzerlabelentry_user
        FOREIGN KEY (user_id)
            REFERENCES usertable(id);
COMMIT;
