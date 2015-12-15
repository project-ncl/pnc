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

(function () {

  var module = angular.module('pnc.common.restclient');

  module.value('BUILD_CONFIGURATION_ENDPOINT',
    '/build-configurations/:configurationId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('BuildConfigurationDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'BUILD_CONFIGURATION_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function ($resource, $injector, REST_BASE_URL, BUILD_CONFIGURATION_ENDPOINT,
      PageFactory, qh, PncCacheUtil) {

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
        _getDependencies: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/:configurationId/dependencies'
        },
        _getByProduct: {
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
        },
        _getByBCSet: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configuration-sets/:configurationSetId/build-configurations' +
            qh.searchOnly(['name'])
        }
      });

      PncCacheUtil.decorateIndexId(resource, 'BuildConfiguration', 'get');

      _([['_getAll'],
         ['_getByBCSet'],
         ['_getByProduct'],
         ['_getByProductVersion'],
         ['_getByProject'],
         ['_getDependencies']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'BuildConfiguration', e[0]);
      });

      _([['_getAll', 'getAll'],
         ['_getByBCSet', 'getByBCSet'],
         ['_getByProduct', 'getByProduct'],
         ['_getByProductVersion', 'getByProductVersion'],
         ['_getByProject', 'getByProject'],
         ['_getDependencies', 'getDependencies']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      _([['_getAll', 'getAllPaged'],
         ['_getByBCSet', 'getPagedByBCSet'],
         ['_getByProductVersion', 'getPagedByProductVersion'],
         ['_getByProject', 'getPagedByProject']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getProject = function() {
        return $injector.get('ProjectDAO').get({ projectId: this.projectId });
      };

      resource.prototype.getProductVersions = function() {
        return $injector.get('ProductVersionDAO').getByBC({ configurationId: this.id });
      };

      resource.prototype.getLatestBuildRecord = function() {
        return $injector.get('BuildRecordDAO').getLatestByBC({ configurationId: this.id }).$promise;
      };

      resource.prototype.getDependencies = function() {
        return resource.getDependencies({ configurationId: this.id });
      };

      return resource;
    }
  ]);

})();
