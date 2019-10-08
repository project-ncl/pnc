--
-- JBoss, Home of Professional Open Source.
-- Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

-- NCL-4888 - Change EXECUTION_ROOT_NAME parameter name
update build_configuration_parameters set KEY='BREW_BUILD_NAME' where KEY='EXECUTION_ROOT_NAME';
update build_configuration_parameters_aud set KEY='BREW_BUILD_NAME' where KEY='EXECUTION_ROOT_NAME';

-- NCL-4679
alter table buildrecord add column buildoutputchecksum varchar(255);
