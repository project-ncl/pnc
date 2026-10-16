--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

ALTER TABLE build_configuration
	ADD COLUMN name character varying(40) NOT NULL,
	ALTER COLUMN project_id SET NOT NULL;

COMMENT ON COLUMN build_configuration.name IS 'Descriptive name of this configuration';
