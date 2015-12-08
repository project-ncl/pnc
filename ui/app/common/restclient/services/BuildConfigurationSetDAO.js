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

  module.value('BUILD_CONFIGURATION_SET_ENDPOINT', '/build-configuration-sets/:configurationSetId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('BuildConfigurationSetDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'BUILD_CONFIGURATION_SET_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function($resource, $injector, REST_BASE_URL, BUILD_CONFIGURATION_SET_ENDPOINT, PageFactory, qh,
              PncCacheUtil) {
      var ENDPOINT = REST_BASE_URL + BUILD_CONFIGURATION_SET_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        'configurationSetId': '@id'
      },{
        _getAll: {
          method: 'GET',
          url: ENDPOINT + qh.searchOnly(['name'])
        },
        update: {
          method: 'PUT'
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
        removeConfiguration: {
          method: 'DELETE',
          url: ENDPOINT + '/build-configurations/:configurationId'
        },
        addConfiguration: {
          method: 'POST',
          url: ENDPOINT + '/build-configurations'
        },
        _getByProductVersion: {
          method: 'GET',
          url: REST_BASE_URL + '/product-versions/:versionId/build-configuration-sets' + qh.searchOnly(['name'])
        }
      });

      PncCacheUtil.decorateIndexId(resource, 'BuildConfigurationSet', 'get');

      _([['_getAll'],
         ['_getByProductVersion']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'BuildConfigurationSet', e[0]);
      });

      _([['_getAll', 'getAll'],
         ['_getByProductVersion', 'getByProductVersion']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      _([['_getAll', 'getAllPaged'],
         ['_getByProductVersion', 'getPagedByProductVersion']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getBuildConfigurations = function () {
        return $injector.get('BuildConfigurationDAO').getByBCSet({ configurationSetId: this.id });
      };

      resource.prototype.getPagedBuildConfigurations = function () {
        return $injector.get('BuildConfigurationDAO').getPagedByBCSet({ configurationSetId: this.id });
      };

      resource.prototype.getBuildRecords = function () {
        return $injector.get('BuildRecordDAO').getByBCSet({ configurationSetId: this.id });
      };

      return resource;
    }
  ]);

})();
