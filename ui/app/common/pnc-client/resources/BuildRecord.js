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
(function () {
  'use strict';

  var module = angular.module('pnc.common.pnc-client.resources');

  module.value('BUILD_RECORD_PATH', '/build-records/:id');
  module.value('BUILDS_PATH', '/builds');
  module.value('BUILD_PATH', '/builds/:id');
  module.value('SSH_CREDENTIALS_PATH', '/builds/ssh-credentials/:recordId');
  module.value('BUILD_RECORD_PUSH_PATH', '/build-record-push');

  /**
   *
   */
  module.factory('BuildRecord', [
    '$resource',
    '$q',
    '$http',
    'restConfig',
    'BUILD_RECORD_PATH',
    'BUILDS_PATH',
    'BUILD_PATH',
    'SSH_CREDENTIALS_PATH',
    'BUILD_RECORD_PUSH_PATH',
    'rsqlQuery',
    'authService',
    function($resource, $q, $http, restConfig, BUILD_RECORD_PATH, BUILDS_PATH, BUILD_PATH, SSH_CREDENTIALS_PATH,
             BUILD_RECORD_PUSH_PATH, rsqlQuery, authService) {
      var ENDPOINT = restConfig.getPncUrl() + BUILD_RECORD_PATH;
      var BUILDS_ENDPOINT = restConfig.getPncUrl() + BUILDS_PATH;
      var BUILD_ENDPOINT = restConfig.getPncUrl() + BUILD_PATH;
      var SSH_CREDENTIALS_ENDPOINT = restConfig.getPncUrl() + SSH_CREDENTIALS_PATH;
      var BUILD_RECORD_PUSH_ENDPOINT = restConfig.getPncUrl() + BUILD_RECORD_PUSH_PATH;

      var FINAL_STATUSES = [
        'DONE',
        'REJECTED',
        'REJECTED_FAILED_DEPENDENCIES',
        'REJECTED_ALREADY_BUILT',
        'SYSTEM_ERROR',
        'DONE_WITH_ERRORS',
        'CANCELLED'
      ];

      var CANCELABLE_STATUSES = [
        'NEW',
        'ENQUEUED',
        'WAITING_FOR_DEPENDENCIES',
        'BUILDING'
      ];

      function isCompleted(status) {
        return FINAL_STATUSES.includes(status);
      }

      function isSuccess(status) {
        return status === 'DONE';
      }

      function hasFailed(status) {
        return isCompleted(status) && !isSuccess(status);
      }

      function isCancelable(status) {
        return CANCELABLE_STATUSES.includes(status);
      }

      function canonicalName(buildRecord) {
        return buildRecord.buildConfigurationName + '#' + buildRecord.id;
      }

      var resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        /**
         * Gets all running and completed BuildRecords
         */
        query: {
          method: 'GET',
          isPaged: true,
          url: BUILD_ENDPOINT
        },
        /**
         * Gets running or completed BuildRecord by id.
         */
        get: {
          method: 'GET',
          url: BUILD_ENDPOINT
        },
        /**
         * Gets last BuildRecord by configuration.
         */
        getLastByConfiguration: {
          method: 'GET',
          url: BUILDS_ENDPOINT + '/?q=buildConfigurationId==:id&pageIndex=0&pageSize=1&sort==desc=id'
        },
        /**
         * Gets BuildRecords by configuration.
         */
        getByConfiguration: {
          method: 'GET',
          isPaged: true,
          url: BUILDS_ENDPOINT + '/?q=buildConfigurationId==:id'
        },
        /**
         * Gets BuildRecords by user.
         */
        getByUser: {
          method: 'GET',
          isPaged: true,
          url: BUILDS_ENDPOINT + '/?q=user.id==:userId'
        },
        /**
         * Gets all completed BuildRecords
         */
        queryCompleted: {
          method: 'GET',
          isPaged: true,
          url: ENDPOINT
        },
        /**
         * Get completed BuildRecord by id.
         */
        getCompleted: {
          method: 'GET',
          url: ENDPOINT
        },
        /**
         * Gets all artifacts for a given BuildRecord.
         */
        getArtifacts: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/artifacts'
        },
        /**
         * Gets all build dependencies for a given BuildRecord
         */
        getDependencies: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/dependency-artifacts'
        },
        /**
         * Get all artifacts produced by the build for a given BuildRecord.
         */
        getBuiltArtifacts: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/built-artifacts'
        },
        doGetSshCredentials :{
          isPaged: false,
          method: 'GET',
          url: SSH_CREDENTIALS_ENDPOINT
        },
        getLog: {
          method: 'GET',
          url: ENDPOINT + '/log',
          cache: true,
          transformResponse: function(data) { return { payload: data }; }
        },
        getRepourLog: {
          method: 'GET',
          url: ENDPOINT + '/repour-log',
          cache: true,
          transformResponse: function(data) { return { payload: data }; }
        },
        cancel: {
          method: 'POST',
          url: BUILD_ENDPOINT + '/cancel'
        }
      });

      /**
       * Gets all records with the given user id.
       *
       * @example
       * var records = BuildRecord.queryWithUserId({ userId: 4 });
       */
      resource.queryWithUserId = function (params) {
        return resource.query({ q: rsqlQuery().where('user.id').eq(params.userId).end() });
      };

      resource.getSshCredentials = function (params) {
        return $q.when(authService.isAuthenticated())
            .then(function (authenticated) {
              if (authenticated) {
                return resource.doGetSshCredentials(params).$promise;
              }
            });
      };

      resource.push = function (buildRecordId, tagPrefix) {
        return $http.post(BUILD_RECORD_PUSH_ENDPOINT, {
          buildRecordId: buildRecordId,
          tagPrefix: tagPrefix
        });
      };

      resource.getLatestPushStatus = function (buildRecordId) {
        return $http.get(BUILD_RECORD_PUSH_ENDPOINT + '/status/' + buildRecordId).then(function (result) {
          return result.data;
        });
      };

      resource.prototype.$isCompleted = function () {
        return isCompleted(this.status);
      };

      resource.prototype.$isSuccess = function () {
        return isSuccess(this.status);
      };

      resource.prototype.$hasFailed = function () {
        return hasFailed(this.status);
      };

      resource.prototype.$isCancelable = function () {
        return isCancelable(this.status);
      };

      resource.prototype.$canonicalName = function () {
        return canonicalName(this);
      };

      return resource;
    }

  ]);


})();
