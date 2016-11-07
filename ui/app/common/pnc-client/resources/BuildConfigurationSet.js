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

  module.value('BUILD_CONFIGURATION_SET_PATH', '/build-configuration-sets/:id');

  /**
   *
   * @author Alex Creasy
   */
  module.factory('BuildConfigurationSet', [
    '$resource',
    'restConfig',
    'BUILD_CONFIGURATION_SET_PATH',
    'rsqlQuery',
    function($resource, restConfig, BUILD_CONFIGURATION_SET_PATH, rsqlQuery) {
      var ENDPOINT = restConfig.getPncUrl() + BUILD_CONFIGURATION_SET_PATH;

      var resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        query: {
          method: 'GET',
          isPaged: true,
        },
        update: {
          method: 'PUT'
        },
        queryBuildConfigurations: {
          url: ENDPOINT + '/build-configurations',
          method: 'GET',
          isPaged: true
        },
        updateBuildConfigurations: {
          url: ENDPOINT + '/build-configurations',
          method: 'PUT'
        },
        addBuildConfiguration: {
          url: ENDPOINT + '/build-configurations',
          method: 'POST'
        },
        removeBuildConfiguration: {
          url: ENDPOINT + '/build-configurations/:configId',
          method: 'DELETE'
        }
      });

      /**
       * Queries for all BuildConfigurationSets that are not linked to a
       * product version.
       */
      resource.prototype.queryWithNoProductVersion = function () {
        return resource.query( { q: rsqlQuery().where('productVersion').isNull().end() });
      };

      return resource;
    }

  ]);


})();
