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

  module.value('RUNNING_BUILD_ENDPOINT', '/running-build-records/:recordId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('RunningBuildRecordDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'RUNNING_BUILD_ENDPOINT',
    'PageFactory',
    function($resource, $injector, REST_BASE_URL, RUNNING_BUILD_ENDPOINT, PageFactory) {
      var ENDPOINT = REST_BASE_URL + RUNNING_BUILD_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        recordId: '@id'
      }, {
        _getAll: {
          method: 'GET',
          isArray: false
        },
        _getByBC: {
          method: 'GET',
          url: REST_BASE_URL + '/running-build-records/build-configurations/:configurationId',
          isArray: false
        },
        getLog: {
          method: 'GET',
          url: ENDPOINT + '/log',
          isArray: false,
          transformResponse: function(data) { return { payload: data }; }
        },
        _getByBCSetRecord: {
          method: 'GET',
          url: REST_BASE_URL + '/running-build-records/build-config-set-records/:bcSetRecordId',
          isArray: false
        }
      });


      _([['_getAll', 'getAll'],
         ['_getByBC', 'getByBC']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      _([['_getAll', 'getAllPaged'],
         ['_getByBC', 'getPagedByBC'],
         ['_getByBCSetRecord', 'getPagedByBCSetRecord']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getBuildConfiguration = function() {
        return $injector.get('BuildConfigurationDAO').get({ configurationId: this.buildConfigurationId });
      };

      resource.prototype.getUser = function() {
        return $injector.get('UserDAO').get({ userId: this.userId });
      };

      return resource;
    }
  ]);

})();
