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
   * @ngdoc service
   * @name pnc.common.restclient:BuildConfigurationSet
   * @description
   *
   * @author Alex Creasy
   */
  module.factory('BuildConfigurationSet', [
    '$resource',
    'REST_BASE_URL',
    'BUILD_CONFIGURATION_SET_ENDPOINT',
    function($resource, REST_BASE_URL, BUILD_CONFIGURATION_SET_ENDPOINT) {
      var ENDPOINT = REST_BASE_URL + BUILD_CONFIGURATION_SET_ENDPOINT;

      var BuildConfigurationSet = $resource(ENDPOINT, {
        'configurationSetId': '@id'
      },{
        update: {
          method: 'PUT'
        },
        getConfigurations: {
          method: 'GET',
          url: ENDPOINT + '/build-configurations',
          isArray: true
        },
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          isArray: false,
        },
        removeConfiguration: {
          method: 'DELETE',
          url: ENDPOINT + '/build-configurations/:configurationId',
          isArray: false,
        },
        addConfiguration: {
          method: 'POST',
          url: ENDPOINT + '/build-configurations',
          isArray: false,
        },
        getRecords: {
          method: 'GET',
          url: ENDPOINT + '/build-records',
          isArray: true,
        },
      });

      return BuildConfigurationSet;
    }
  ]);

})();
