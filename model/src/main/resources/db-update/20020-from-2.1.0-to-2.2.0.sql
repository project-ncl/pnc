--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
    ALTER TABLE buildrecord ADD COLUMN last_update_time timestamptz;
    UPDATE buildrecord set lastupdatetime = COALESCE(endtime, starttime, submittime);
COMMIT;
