/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

(function () {

  var module = angular.module('pnc.common.restclient');

  module.value('BUILD_CONFIGURATION_ENDPOINT',
    '/build-configurations/:configurationId');

  /**
   *
   */
  module.factory('BuildConfigurationDAO', [
    '$resource',
    'REST_BASE_URL',
    'BUILD_CONFIGURATION_ENDPOINT',
    'ProjectDAO',
    'cachedGetter',
    'PageFactory',
    'QueryHelper',
    function ($resource, REST_BASE_URL, BUILD_CONFIGURATION_ENDPOINT, ProjectDAO, cachedGetter, PageFactory, qh) {
      var ENDPOINT = REST_BASE_URL + BUILD_CONFIGURATION_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        configurationId: '@id'
      }, {
        _getAll: {
          method: 'GET',
          url: ENDPOINT + qh.searchOnly(['name', 'description', 'project.name'])
        },
        querySearch: {
          method: 'GET',
          url: ENDPOINT + '/?q=name=like=%25:name%25'
        },
        update: {
          method: 'PUT'
        },
        clone: {
          method: 'POST',
          url: ENDPOINT + '/clone'
        },
        forceBuild: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false,
          params: {
            rebuildAll: true
          }
        },
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false
        },
        _getBuildRecords: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records?q=latestBuildConfiguration.id==:configurationId'
        },
        _getProductVersions: {
          method: 'GET',
          url: ENDPOINT + '/product-versions'
        },
        _getDependencies: {
          method: 'GET',
          url: ENDPOINT + '/dependencies'
        },
        _getConfigurationSets: {
            method: 'GET',
            url: ENDPOINT + '/build-configuration-sets'
          },
        _getAllForProduct: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/products/:productId'
        },
        _getByProductVersion: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/products/:productId/product-versions/:versionId' +
            qh.searchOnly(['name'])
        },
        _getByProject: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/projects/:projectId'
        }
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getBuildRecords', 'getBuildRecords');
      PageFactory.decorateNonPaged(resource, '_getProductVersions', 'getProductVersions');
      PageFactory.decorateNonPaged(resource, '_getDependencies', 'getDependencies');
      PageFactory.decorateNonPaged(resource, '_getConfigurationSets', 'getConfigurationSets');
      PageFactory.decorateNonPaged(resource, '_getAllForProduct', 'getAllForProduct');
      PageFactory.decorateNonPaged(resource, '_getByProductVersion', 'getAllForProductVersion');
      PageFactory.decorateNonPaged(resource, '_getByProject', 'getAllForProject');

      PageFactory.decorate(resource, '_getAll', 'getAll');
      PageFactory.decorate(resource, '_getByProductVersion', 'getPagedByProductVersion');
      PageFactory.decorate(resource, '_getByProject', 'getPagedByProject');

      resource.prototype.getProject = cachedGetter(
        function (configuration) {
          return ProjectDAO.get({projectId: configuration.projectId});
        }
      );

      return resource;
    }
  ]);

})();
