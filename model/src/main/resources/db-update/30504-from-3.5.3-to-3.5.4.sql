--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCL-9807] Start tracking rebuildMode of a build
BEGIN;

ALTER TABLE buildrecord ADD COLUMN rebuildmode varchar(255);
ALTER TABLE buildconfigsetrecord ADD COLUMN rebuildmode varchar(255);

COMMIT;