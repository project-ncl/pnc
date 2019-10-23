/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  const module = angular.module('pnc.common.pnc-client.resources');

  module.value('BUILD_PATH', '/builds/:id');
  module.value('BUILD_SSH_CREDENTIALS_PATH', '/builds/ssh-credentials/:id');

  /**
   * @author Martin Kelnar
   */
  module.factory('BuildResource', [
    '$resource',
    '$q',
    'restConfig',
    'authService',
    'BUILD_PATH',
    'BUILD_SSH_CREDENTIALS_PATH',
    ($resource, $q, restConfig, authService, BUILD_PATH, BUILD_SSH_CREDENTIALS_PATH) => {
      const ENDPOINT = restConfig.getPncRestUrl() + BUILD_PATH;
      const BUILD_SSH_CREDENTIALS_ENDPOINT = restConfig.getPncRestUrl() + BUILD_SSH_CREDENTIALS_PATH;

      const CANCELABLE_STATUSES = [
        'NEW',
        'ENQUEUED',
        'WAITING_FOR_DEPENDENCIES',
        'BUILDING'
      ];

      const resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        /**
         * Gets All builds.
         */
        query: {
          method: 'GET',
          isPaged: true,
          url: ENDPOINT
        },

        // getLastByConfiguration should be part of the BuildConfigResource

        // getByConfiguration should be part of the BuildConfigResource

        queryByUser: {
          method: 'GET',
          isPaged: true,
          url: ENDPOINT + '/?q=user.id==:userId'
        },

        /**
         * Gets dependency artifacts for specific build.
         */
        getArtifactsDependencies: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/artifacts/dependencies'
        },

        /**
         * Gets dependency graph for a build.
         */
        getDependencyGraph: {
          method: 'GET',
          url: ENDPOINT + '/dependency-graph'
        },

        /**
         * Gets artifacts built in a specific build.
         */
        getBuiltArtifacts: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/artifacts/built'
        },

        /**
         * ssh-credentials is the only one GET REST endpoint requiring authentication, this is the reason
         * why url structure is not compliant with the REST standards: /builds/:id/ssh-credentials, see NCL-5250
         * 
         * This method shouldn't be called directly, but getSshCredentials() should be used.
         */
        _getSshCredentials :{
          isPaged: false,
          method: 'GET',
          url: BUILD_SSH_CREDENTIALS_ENDPOINT
        },

        /**
         * Gets build logs for specific build.
         */
        getLogBuild: {
          method: 'GET',
          url: ENDPOINT + '/logs/build',
          cache: true,
          transformResponse: function(data) { return { payload: data }; }
        },

        /**
         * Gets alignment logs for specific build.
         */
        getLogAlign: {
          method: 'GET',
          url: ENDPOINT + '/logs/align',
          cache: true,
          transformResponse: function(data) { return { payload: data }; }
        },

        /**
         * Cancel running build.
         */
        cancel: {
          method: 'POST',
          url: ENDPOINT + '/cancel'
        },

        getBrewPushResult: {
          method: 'GET',
          url: ENDPOINT + '/brew-push'
        },

        /**
         * Push build to Brew.
         * 
         * {
         *  "tagPrefix": "string",
         *  "buildId": "string",
         *  "reimport": true
         * }
         */
        brewPush: {
          method: 'POST',
          url: ENDPOINT + '/brew-push'
        },

        /**
         * Cancels push of build to Brew.
         */
        brewPushCancel: {
          method: 'DELETE',
          url: ENDPOINT + '/brew-push'
        }

      });


      resource.getSshCredentials = params => $q.when(authService.isAuthenticated()).then(authenticated => {
        if (authenticated) {
          return resource._getSshCredentials(params);
        }
      });


      function isSuccess(status) {
        return status === 'DONE';
      }
      resource.prototype.$isSuccess = function () {
        return isSuccess(this.status);
      };


      function isCancelable(status) {
        return CANCELABLE_STATUSES.includes(status);
      }
      resource.prototype.$isCancelable = function () {
        return isCancelable(this.status);
      };


      function canonicalName(build) {
        return build.buildConfigurationName + '#' + build.id;
      }
      resource.prototype.$canonicalName = function () {
        return canonicalName(this);
      };


      function buildLogUrl(build) {
        return ENDPOINT.replace(':id', build.id) + '/logs/build';
      }
      resource.prototype.$buildLogUrl = function () {
        return buildLogUrl(this);
      };

      return resource;
    }

  ]);

})();
