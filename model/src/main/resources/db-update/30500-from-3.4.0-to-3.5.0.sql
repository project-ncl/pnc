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

CREATE SEQUENCE IF NOT EXISTS attachment_id_seq INCREMENT 1 START 100;

CREATE TABLE IF NOT EXISTS attachment
(
    id integer NOT NULL,
    creationtime timestamp without time zone,
    description text,
    md5 character varying(32) NOT NULL,
    name text NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(1024) NOT NULL,
    buildrecord_id bigint,
    CONSTRAINT attachment_pkey PRIMARY KEY (id),
    CONSTRAINT uk_attachment_url UNIQUE (url),
    CONSTRAINT uk_attachment_recordid_name UNIQUE (buildrecord_id, name),
    CONSTRAINT fk_artifact_buildrecord FOREIGN KEY (buildrecord_id) REFERENCES buildrecord (id)
);

-- Index: idx_attachment_buildrecord
CREATE INDEX IF NOT EXISTS idx_attachment_buildrecord
    ON attachment USING btree(buildrecord_id);

-- Index: idx_attachment_creationtime
CREATE INDEX IF NOT EXISTS idx_attachment_creationtime
    ON attachment USING btree(creationtime);

-- Index: idx_attachment_name
CREATE INDEX IF NOT EXISTS idx_attachment_name
    ON attachment USING btree(name);

-- Index: idx_attachment_type
CREATE INDEX IF NOT EXISTS idx_attachment_type
    ON attachment USING btree(type);

-- Index: idx_attachment_url
CREATE INDEX IF NOT EXISTS idx_attachment_url
    ON attachment USING btree(url);


COMMIT;