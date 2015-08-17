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

  module.value('BUILD_RECORD_ENDPOINT', '/build-records/:recordId');

  /**
   * @ngdoc service
   * @name pnc.common.restclient:BuildRecord
   * @description
   *
   */
  module.factory('BuildRecord', [
    '$resource',
    'cachedGetter',
    'REST_BASE_URL',
    'BUILD_RECORD_ENDPOINT',
    'BuildConfiguration',
    'User',
    function($resource, cachedGetter, REST_BASE_URL, BUILD_RECORD_ENDPOINT, BuildConfiguration, User) {
      var ENDPOINT = REST_BASE_URL + BUILD_RECORD_ENDPOINT;

      var BuildRecord = $resource(ENDPOINT, {
        recordId: '@id',
        q: '@q'
      }, {

        getLog: {
          method: 'GET',
          url: ENDPOINT + '/log',
          isArray: false,
          transformResponse: function(data) { return { payload: data }; }
        },

        getArtifacts: {
          method: 'GET',
          url: ENDPOINT + '/artifacts',
          isArray: true,
        },

        getAllForConfiguration: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records/build-configurations/:configurationId',
          isArray: true,
        },

        getAllForProject: {
          method: 'GET',
          url: REST_BASE_URL + 'record/projects/:projectId',
          isArray: true,
        },

        getLatestForConfiguration: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records/build-configurations/:configurationId?pageIndex=0&pageSize=1&sort==desc=id',
          isArray: true,
        },

        getAuditedBuildConfiguration: {
          method: 'GET',
          url: ENDPOINT + '/build-configuration-audited',
          isArray: false
        }

      });

      BuildRecord.prototype.getBuildConfiguration = cachedGetter(
        function(buildRecord) {
          return BuildConfiguration.get({ configurationId: buildRecord.buildConfigurationId });
        }
      );

      BuildRecord.prototype.getUser = cachedGetter(
        function(record) {
          return User.get({ userId: record.userId });
        }
      );

      return BuildRecord;
    }

  ]);


})();
