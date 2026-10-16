--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
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


