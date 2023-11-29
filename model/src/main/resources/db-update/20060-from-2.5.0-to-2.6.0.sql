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

    ALTER TABLE buildrecord ADD COLUMN scmbuildconfigrevision varchar(255);
    ALTER TABLE buildrecord ADD COLUMN scmbuildconfigrevisioninternal boolean;

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
    brewbuildid         BIGINT,
    PRIMARY KEY (report_id, artifact_id)
);
ALTER TABLE deliverableartifact
    ADD CONSTRAINT fk_deliverableartifact_delanreport
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
    entrytime       TIMESTAMP,
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

--------------------------------------------------------------------------------
-- DeliverableAnalyzerDistribution
--------------------------------------------------------------------------------
BEGIN transaction;
    CREATE TABLE deliverableanalyzerdistribution
    (
        id                  BIGINT             NOT NULL,
        distributionUrl     TEXT               NOT NULL,
        creationtime        timestamptz        NOT NULL,
        PRIMARY KEY (id)
    );
COMMIT;

--------------------------------------------------------------------------------
-- Update to DeliverableArtifact
--------------------------------------------------------------------------------

BEGIN transaction;
   ALTER TABLE deliverableartifact ADD COLUMN archiveFilenames TEXT;
   ALTER TABLE deliverableartifact ADD COLUMN archiveUnmatchedFilenames TEXT;

   ALTER TABLE deliverableartifact
   ADD CONSTRAINT fk_deliverableartifact_distribution
       FOREIGN KEY (distribution_id)
           REFERENCES deliverableanalyzerdistribution(id);
COMMIT;


