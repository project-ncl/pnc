/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function() {

  var module = angular.module('pnc.common.restclient');

  /**
   * @ngdoc service
   * @name pnc.common.restclient:PncRestClient
   * @description
   * Client service for PNC REST API.
   *
   * @deprecated
   * Please inject the specific model classes rather than using this facade,
   * this is only kept for the time being to support older code that will be
   * refactored in the future.
   *
   * @author Alex Creasy
   * @author Andrea Vibelli
   * @author Jakub Senko
   */
  module.factory('PncRestClient', [
    '$resource',
    'REST_BASE_URL',
    'ProductDAO',
    'ProductVersionDAO',
    'BuildRecordDAO',
    'BuildConfigurationSetRecordDAO',
    'BuildConfigurationDAO',
    'ProductMilestoneDAO',
    'ProductReleaseDAO',
    'ProjectDAO',
    'EnvironmentDAO',
    'RunningBuildRecordDAO',
    'BuildConfigurationSetDAO',
    'BuildRecordSetDAO',
    'UserDAO',
    function($resource, REST_BASE_URL, ProductDAO, ProductVersionDAO, BuildRecordDAO,
      BuildConfigurationSetRecordDAO, BuildConfigurationDAO, ProductMilestoneDAO, ProductReleaseDAO,
      ProjectDAO, EnvironmentDAO, RunningBuildRecordDAO,
      BuildConfigurationSetDAO, BuildRecordSetDAO, UserDAO) {

      return {

        Product: ProductDAO,

        Version: ProductVersionDAO,

        Milestone: ProductMilestoneDAO,

        Release: ProductReleaseDAO,

        Project: ProjectDAO,

        Environment: EnvironmentDAO,

        Configuration: BuildConfigurationDAO,

        Record: BuildRecordDAO,

        ConfigurationSetRecord: BuildConfigurationSetRecordDAO,

        Running: RunningBuildRecordDAO,

        ConfigurationSet: BuildConfigurationSetDAO,

        RecordSet: BuildRecordSetDAO,

        User: UserDAO
      };
    }
  ]);

})();
