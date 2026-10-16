--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- insert new columns in build record
alter table buildrecord add column dependencybuildrecordids text;
alter table buildrecord add column dependentbuildrecordids text;

-- drop unique constraint on artifact.originurl
alter table artifact drop constraint uk_o2n9o8hiuspeyqh0t6nkgnvs8;
