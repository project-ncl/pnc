--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;

    -- [NCLSUP-1338] Increase Artifact's deploypath and originurl size to 1024
    ALTER TABLE artifact ALTER deploypath TYPE VARCHAR(1024);
    ALTER TABLE artifact ALTER originurl TYPE VARCHAR(1024);

COMMIT;
