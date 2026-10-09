--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- [NCL-4652]
-- Copy scmRevision to scmTag for old builds
alter table buildrecord add column scmTag varchar(255);

UPDATE buildrecord
SET scmTag = scmRevision;
