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

  module.value('PRODUCT_VERSION_PATH', '/product-versions/:id');

  /**
   *
   * @author Alex Creasy
   */
  module.factory('ProductVersion', [
    '$resource',
    'restConfig',
    'PRODUCT_VERSION_PATH',
    function($resource, restConfig, PRODUCT_VERSION_PATH) {
      var ENDPOINT = restConfig.getPncUrl() + PRODUCT_VERSION_PATH;

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
        queryBuildConfigurationSets: {
          url: ENDPOINT + '/build-configuration-sets',
          method: 'GET',
          isPaged: true
        },
        updateBuildConfigurationSets: {
          url: ENDPOINT + '/build-configuration-sets',
          method: 'PUT'
        }
      });

      return resource;
    }

  ]);

})();
