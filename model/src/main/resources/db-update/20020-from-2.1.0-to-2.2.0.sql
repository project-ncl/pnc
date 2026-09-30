--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCL-6718] Rename "distributed artifacts" to "delivered artifacts"
BEGIN transaction;
    ALTER TABLE product_milestone_distributed_artifacts_map RENAME TO product_milestone_delivered_artifacts_map;
    ALTER INDEX idx_product_milestone_distr_art_map_artifact RENAME TO idx_product_milestone_del_art_map_artifact;
    ALTER INDEX idx_product_milestone_distr_art_map_productmilestone RENAME TO idx_product_milestone_del_art_map_productmilestone;
    ALTER TABLE product_milestone_delivered_artifacts_map RENAME CONSTRAINT fk_product_milestone_distr_art_map_artifact TO fk_product_milestone_del_art_map_artifact;
    ALTER TABLE product_milestone_delivered_artifacts_map RENAME CONSTRAINT fk_product_milestone_distr_art_map_productmilestone TO fk_product_milestone_del_art_map_productmilestone;
    ALTER TABLE productmilestone RENAME CONSTRAINT fk_distributed_artifacts_importer_user TO fk_delivered_artifacts_importer_user;
    ALTER TABLE productmilestone RENAME COLUMN distributedartifactsimporter_id TO deliveredartifactsimporter_id;
COMMIT;

-- Maybe this old constraint is not on all environments
BEGIN transaction;
    ALTER TABLE product_milestone_delivered_artifacts_map DROP CONSTRAINT fk_product_milestone_distributed_artifacts_map;
COMMIT;

-- [NCL-6790] - Extend BuildRecord model in Orchestrator to add a lastUpdated column
BEGIN transaction;
    ALTER TABLE buildrecord ADD COLUMN lastupdatetime timestamptz;
    UPDATE buildrecord set lastupdatetime = COALESCE(endtime, starttime, submittime);
COMMIT;
-- [NCL-6790] - Update the _archived_buildrecords table (not mapped by Hibernate) to let the native queries work
BEGIN transaction;
    ALTER TABLE _archived_buildrecords ADD COLUMN lastupdatetime timestamptz;
    UPDATE _archived_buildrecords set lastupdatetime = COALESCE(endtime, starttime, submittime);
COMMIT;
