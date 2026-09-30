--
-- SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
-- SPDX-License-Identifier: Apache-2.0
--

--------------------------------------------------------------------------------
-- DeliverableAnalyzerDistribution
--------------------------------------------------------------------------------

BEGIN transaction;
   ALTER TABLE deliverableanalyzerdistribution ADD COLUMN md5 varchar(32);
   ALTER TABLE deliverableanalyzerdistribution ADD COLUMN sha1 varchar(40);
   ALTER TABLE deliverableanalyzerdistribution ADD COLUMN sha256 varchar(64);
COMMIT;


