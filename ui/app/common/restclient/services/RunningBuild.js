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
   * @ngdoc service
   * @name pnc.common.restclient:RunningBuild
   * @description
   *
   * @author Alex Creasy
   */
  module.factory('RunningBuild', [
    '$resource',
    'REST_BASE_URL',
    'RUNNING_BUILD_ENDPOINT',
    function($resource, REST_BASE_URL, RUNNING_BUILD_ENDPOINT) {
      var ENDPOINT = REST_BASE_URL + RUNNING_BUILD_ENDPOINT;

      var RunningBuild = $resource(ENDPOINT, {
        recordId: '@id'
      }, {
        getLog: {
          method: 'GET',
          url: ENDPOINT + '/log',
          isArray: false,
          transformResponse: function(data) { return { payload: data }; }
        },
      });

      return RunningBuild;
    }
  ]);

})();
