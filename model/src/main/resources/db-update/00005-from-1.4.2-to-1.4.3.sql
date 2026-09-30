--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

CREATE INDEX idx_artifact_md5 ON Artifact (md5);
CREATE INDEX idx_artifact_sha1 ON Artifact (sha1);
