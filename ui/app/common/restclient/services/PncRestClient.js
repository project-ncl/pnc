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
    'Product',
    'ProductVersion',
    'BuildRecord',
    'ConfigurationSetRecord',
    'BuildConfiguration',
    'Milestone',
    'Release',
    'Project',
    'Environment',
    'RunningBuild',
    'BuildConfigurationSet',
    'BuildRecordSet',
    'User',
    function($resource, REST_BASE_URL, Product, ProductVersion, BuildRecord,
      ConfigurationSetRecord, BuildConfiguration, Milestone, Release, Project, Environment, RunningBuild,
      BuildConfigurationSet, BuildRecordSet, User) {

      return {

        Product: Product,

        Version: ProductVersion,

        Milestone: Milestone,

        Release: Release,

        Project: Project,

        Environment: Environment,

        Configuration: BuildConfiguration,

        Record: BuildRecord,

        ConfigurationSetRecord: ConfigurationSetRecord,

        Running: RunningBuild,

        ConfigurationSet: BuildConfigurationSet,

        RecordSet: BuildRecordSet,

        User: User
      };
    }
  ]);

})();
