--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

-- Change BuildEnvironment unique constraint
BEGIN transaction;

    ALTER TABLE BuildEnvironment
        ADD CONSTRAINT uk_buildenvironment_imageid_imagerepositoryurl unique (systemImageId, systemImageRepositoryUrl);

    ALTER TABLE BuildEnvironment
        DROP CONSTRAINT uk_buildenvironment_name;
COMMIT;
