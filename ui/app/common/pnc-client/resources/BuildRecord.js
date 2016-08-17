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
  module.value('BUILDS_PATH', '/builds/:id');

  /**
   *
   */
  module.factory('BuildRecord', [
    '$resource',
    'restConfig',
    'BUILD_RECORD_PATH',
    'BUILDS_PATH',
    'rsqlQuery',
    function($resource, restConfig, BUILD_RECORD_PATH, BUILDS_PATH, rsqlQuery) {
      var ENDPOINT = restConfig.getPncUrl() + BUILD_RECORD_PATH;
      var BUILDS_ENDPOINT = restConfig.getPncUrl() + BUILDS_PATH;


      var resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        /**
         * Gets all running and completed BuildRecords
         */
        query: {
          method: 'GET',
          isPaged: true,
          url: BUILDS_ENDPOINT
        },
        /**
         * Gets running or completed BuildRecord by id.
         */
        get: {
          method: 'GET',
          url: BUILDS_ENDPOINT
        },
        queryCompleted: {
          method: 'GET',
          isPaged: true,
          url: ENDPOINT,
        },
        getCompleted: {
          method: 'GET',
          url: ENDPOINT
        },
        getArtifacts: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/artifacts'
        },
        getDependencyArtifacts: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/dependency-artifacts'
        },
        getBuiltArtifacts: {
          isPaged: true,
          method: 'GET',
          url: ENDPOINT + '/built-artifacts'
        },
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

      return resource;
    }

  ]);


})();
