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

  module.value('ARTIFACT_PATH', '/artifacts/:id');

  module.factory('Artifact', [
    '$resource',
    'restConfig',
    'ARTIFACT_PATH',
    'BuildRecord',
    'rsqlQuery',
    '$q',
    function ($resource, restConfig, ARTIFACT_PATH, BuildRecord, rsqlQuery, $q) {
      const ENDPOINT = restConfig.getPncUrl() + ARTIFACT_PATH;


      const resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        query: {
          url: ENDPOINT,
          method: 'GET',
          isPaged: true
        }
      });

      resource.getDependantBuildRecords = function (artifact, params = {}) {
        if (artifact && artifact.dependantBuildRecordIds && artifact.dependantBuildRecordIds.length === 0) {
          return $q.when(null);
        } else {
          return BuildRecord.query(Object.assign(params, { q: rsqlQuery().where('id').in(artifact.dependantBuildRecordIds).end() })).$promise;
        }
      };

      resource.prototype.$getDependantBuildRecords = function (params) {
        return resource.getDependantBuildRecords(this, params);
      };

      return resource;
    }

  ]);

})();
