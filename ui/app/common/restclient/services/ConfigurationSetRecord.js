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

(function () {

  var module = angular.module('pnc.common.restclient');

  module.value('BUILD_CONFIG_SET_RECORD_ENDPOINT', '/build-config-set-records/:recordId');

  /**
   * @ngdoc service
   */
  module.factory('ConfigurationSetRecord', [
    '$resource',
    'BuildConfigurationSet',
    'User',
    'cachedGetter',
    'REST_BASE_URL',
    'BUILD_CONFIG_SET_RECORD_ENDPOINT',
    function ($resource, BuildConfigurationSet, User, cachedGetter, REST_BASE_URL, BUILD_CONFIG_SET_RECORD_ENDPOINT) {

      var ENDPOINT = REST_BASE_URL + BUILD_CONFIG_SET_RECORD_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        recordId: '@id'
      }, {});

      resource.prototype.getConfigurationSet = cachedGetter(
        function (record) {
          return BuildConfigurationSet.get({ configurationSetId: record.buildConfigurationSetId });
        }
      );

      resource.prototype.getUser = cachedGetter(
        function (record) {
          return User.get({ userId: record.userId });
        }
      );

      return resource;
    }
  ]);

})();
