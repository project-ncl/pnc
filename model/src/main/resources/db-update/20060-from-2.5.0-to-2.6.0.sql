--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014-2022 Red Hat, Inc., and individual contributors
-- as indicated by the @author tags.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--


BEGIN;
    CREATE TABLE build_configuration_align_strats
    (
        buildconfiguration_id integer                NOT NULL,
        allowlist             text,
        denylist              text,
        idallowlist           text,
        iddenylist            text,
        idranks               text,
        ranks                 text,
        dependencyscope       character varying(255) NOT NULL
    );

    ALTER TABLE build_configuration_align_strats
        ADD CONSTRAINT build_configuration_align_strats_pkey PRIMARY KEY (buildconfiguration_id, dependencyscope);

    ALTER TABLE build_configuration_align_strats
        ADD CONSTRAINT fk_align_strats_bc FOREIGN KEY (buildconfiguration_id) REFERENCES buildconfiguration(id);

CREATE TABLE build_configuration_align_strats_aud
    (
        rev                   integer                NOT NULL,
        revtype               smallint               NOT NULL,
        buildconfiguration_id integer                NOT NULL,
        dependencyscope       character varying(255) NOT NULL,
        ranks                 text,
        idallowlist           text,
        denylist              text,
        iddenylist            text,
        idranks               text,
        allowlist             text
    );
    ALTER TABLE build_configuration_align_strats_aud
        ADD CONSTRAINT build_configuration_align_strats_aud_pkey PRIMARY KEY (rev, revtype, buildconfiguration_id, dependencyscope);

    ALTER TABLE build_configuration_align_strats_aud
        ADD CONSTRAINT fk_build_configuration_align_strats_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo(rev);

    ALTER TABLE build_configuration_align_strats_aud
        ADD CONSTRAINT fk_buildconfiguration_align_strats_aud_buildconfiguration FOREIGN KEY (buildconfiguration_id, rev) REFERENCES buildconfiguration_aud(id, rev);
COMMIT;