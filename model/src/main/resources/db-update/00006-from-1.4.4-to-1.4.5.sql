--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014 Red Hat, Inc., and individual contributors
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

-- see [NCL-4506]
-- finds conflicting repositoryConfigurations
-- if it returns 1 or more rows you have to resolve the conflicts before running update script
select *
from repositoryconfiguration
where externalurlnormalized in
    (select substring(externalurlnormalized from '%@#"%#"' for '#')
        from repositoryconfiguration
        where externalurlnormalized
        like '%@%');

-- repairs externalNormalized URLs
update repositoryconfiguration
set externalurlnormalized = subquery.cutnormalized
from
    (select substring(externalurlnormalized from '%@#"%#"' for '#') as cutnormalized, id
        from repositoryconfiguration
        where externalurlnormalized
        like '%@%')
    as subquery
where repositoryconfiguration.id = subquery.id;


