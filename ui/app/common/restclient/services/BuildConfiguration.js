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

  module.value('BUILD_CONFIGURATION_ENDPOINT',
    '/build-configurations/:configurationId');

  /**
   * @ngdoc service
   * @name pnc.common.restclient:BuildConfiguration
   * @description
   *
   */
  module.factory('BuildConfiguration', [
    '$resource',
    'REST_BASE_URL',
    'BUILD_CONFIGURATION_ENDPOINT',
    'Project',
    'cachedGetter',
    function($resource, REST_BASE_URL, BUILD_CONFIGURATION_ENDPOINT, Project, cachedGetter) {
      var ENDPOINT = REST_BASE_URL + BUILD_CONFIGURATION_ENDPOINT;

      var BuildConfiguration = $resource(ENDPOINT, {
        configurationId: '@id'
      },{
        update: {
          method: 'PUT'
        },
        clone: {
          method: 'POST',
          url: ENDPOINT + '/clone',
          isArray: false,
        },
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          isArray: false,
        },
        getBuildRecords: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records?q=latestBuildConfiguration.id==:configurationId',
          isArray: true,
        },
        getProductVersions: {
          method: 'GET',
          url: ENDPOINT + '/product-versions',
          isArray: true
        },
        getDependencies: {
          method: 'GET',
          url: ENDPOINT + '/dependencies',
          isArray: true
        },
        getAllForProduct: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/products/:productId',
          isArray: true,
        },
        getAllForProductVersion: {
          method: 'GET',
          url: REST_BASE_URL +
          '/build-configurations/products/:productId/product-versions/:versionId',
          isArray: true,
        },
        getAllForProject: {
         method: 'GET',
         url: REST_BASE_URL + '/build-configurations/projects/:projectId',
         isArray: true,
        },
      });

      BuildConfiguration.prototype.getProject = cachedGetter(
        function(configuration) {
          return Project.get({ projectId: configuration.projectId });
        }
      );

      return BuildConfiguration;
    }
  ]);

})();
