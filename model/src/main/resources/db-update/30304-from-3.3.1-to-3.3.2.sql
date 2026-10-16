--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;

    -- [NCL-9421] Implement reasoned result for deliverable analysis
    ALTER TABLE operation
        ADD COLUMN reason varchar(1024),
        ADD COLUMN proposal text;

COMMIT;
