--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCLSUP-598] Increase size column for username to 255
BEGIN;
ALTER TABLE
    usertable
ALTER COLUMN username TYPE varchar(255);
COMMIT;