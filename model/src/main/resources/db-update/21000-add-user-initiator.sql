--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;
-- NCL-8808: add user-initiator for brew push
ALTER table buildrecordpushresult ADD userinitiator varchar(255);

COMMIT;
