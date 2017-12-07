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

  module.value('BUILD_CONFIG_SET_RECORD_PATH', '/build-config-set-records/:id');
  module.value('BUILD_CONFIG_SET_RECORD_PUSH_PATH', '/build-record-push/record-set');


  /**
   * @author Martin Kelnar
   */
  module.factory('BuildConfigSetRecord', [
    '$resource',
    '$http',
    'restConfig',
    'BUILD_CONFIG_SET_RECORD_PATH',
    'BUILD_CONFIG_SET_RECORD_PUSH_PATH',
    function($resource, $http, restConfig, BUILD_CONFIG_SET_RECORD_PATH, BUILD_CONFIG_SET_RECORD_PUSH_PATH) {
      var ENDPOINT = restConfig.getPncUrl() + BUILD_CONFIG_SET_RECORD_PATH;
      var PUSH_ENDPOINT = restConfig.getPncUrl() + BUILD_CONFIG_SET_RECORD_PUSH_PATH;

      var resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        getBuildRecords: {
          url: ENDPOINT + '/build-records',
          method: 'GET',
          isPaged: true
        }
      });

      resource.push = function (buildConfigSetRecordId, tagPrefix) {
        return $http.post(PUSH_ENDPOINT, {
          buildConfigSetRecordId: buildConfigSetRecordId,
          tagPrefix: tagPrefix
        });
      };

      return resource;
    }

  ]);

})();
