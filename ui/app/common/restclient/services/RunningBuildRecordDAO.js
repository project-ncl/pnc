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

(function() {

  var module = angular.module('pnc.common.restclient');

  module.value('RUNNING_BUILD_ENDPOINT', '/running-build-records/:recordId');

  /**
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('RunningBuildRecordDAO', [
    '$resource',
    'REST_BASE_URL',
    'RUNNING_BUILD_ENDPOINT',
    'cachedGetter',
    'BuildConfigurationDAO',
    'UserDAO',
    'PageFactory',
    function($resource, REST_BASE_URL, RUNNING_BUILD_ENDPOINT, cachedGetter, BuildConfigurationDAO, UserDAO, PageFactory) {
      var ENDPOINT = REST_BASE_URL + RUNNING_BUILD_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        recordId: '@id'
      }, {
        _getAll: {
          method: 'GET',
          isArray: false
        },
        _getByConfiguration: {
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

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getByConfiguration', 'getByConfiguration');

      PageFactory.decorate(resource, '_getAll', 'getAll');
      PageFactory.decorate(resource, '_getByConfiguration', 'getPagedByConfiguration');
      PageFactory.decorate(resource, '_getByBCSetRecord', 'getPagedByBCSetRecord');

      resource.prototype.getBuildConfiguration = cachedGetter(
        function(buildRecord) {
          return BuildConfigurationDAO.get({ configurationId: buildRecord.buildConfigurationId });
        }
      );

      resource.prototype.getUser = cachedGetter(
        function(record) {
          return UserDAO.get({ userId: record.userId });
        }
      );

      return resource;
    }
  ]);

})();
