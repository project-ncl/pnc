--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

--------------------------------------------------------------------------------
-- BuildEnvironment
--------------------------------------------------------------------------------
BEGIN transaction;
    ALTER TABLE buildenvironment ADD COLUMN hidden BOOLEAN;

    -- by default all existing buildenvironment are not hidden
    UPDATE buildenvironment SET hidden = false;
    ALTER TABLE buildenvironment ALTER COLUMN hidden SET NOT NULL;
COMMIT;

BEGIN transaction;
    UPDATE buildenvironment SET hidden = true WHERE systemImageRepositoryUrl LIKE 'docker-registry-default.cloud.registry.upshift.redhat.com%';
    UPDATE buildenvironment SET description = name || ' [' || systemImageId || ']';
COMMIT;

