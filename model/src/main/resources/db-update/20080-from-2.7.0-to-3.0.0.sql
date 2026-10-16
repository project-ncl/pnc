--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;
ALTER TABLE buildconfigsetrecord ALTER COLUMN id SET DATA TYPE bigint;
-- update foreign bcsr keys
ALTER TABLE buildrecord ALTER COLUMN buildconfigsetrecord_id TYPE bigint;
ALTER TABLE build_config_set_record_attributes ALTER COLUMN build_config_set_record_id TYPE bigint;
-- update archived build records foreign key
ALTER TABLE _archived_buildrecords ALTER COLUMN buildconfigsetrecord_id TYPE bigint;

COMMIT;

