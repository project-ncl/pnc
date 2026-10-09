--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- NCL-4581: add health check
create sequence generic_setting_id_seq;
create table GenericSetting (
    id integer default nextval('generic_setting_id_seq') not null,
    key varchar(255) unique not null,
    value text not null,
    primary key (id)
);