--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

BEGIN;

CREATE SEQUENCE IF NOT EXISTS attachment_id_seq INCREMENT 1 START 100;

CREATE TABLE IF NOT EXISTS attachment
(
    id integer NOT NULL,
    creationtime timestamp without time zone,
    description text,
    sha256 character varying(64) NOT NULL,
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

-- Index: idx_attachment_sha256
CREATE INDEX IF NOT EXISTS idx_attachment_sha256
    ON attachment USING btree(sha256);


COMMIT;