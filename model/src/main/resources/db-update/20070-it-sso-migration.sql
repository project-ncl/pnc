--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- Drop the unique constrain on email, to allow multiple system accounts
-- share the same email.
BEGIN;
    ALTER TABLE usertable DROP CONSTRAINT uk_user_email;
END;
