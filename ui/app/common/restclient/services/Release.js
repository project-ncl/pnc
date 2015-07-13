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

  module.value('RELEASE_ENDPOINT', '/product-releases/:releaseId');
  /**
   * @ngdoc service
   * @name pnc.common.restclient:Release
   * @description
   *
   */
  module.factory('Release', [
    '$resource',
    'REST_BASE_URL',
    'RELEASE_ENDPOINT',
    function($resource, REST_BASE_URL, RELEASE_ENDPOINT) {
      var ENDPOINT = REST_BASE_URL + RELEASE_ENDPOINT;

      var Release = $resource(ENDPOINT, {
        releaseId: '@id'
      },{
        update: {
          method: 'PUT',
        },
        getAllForProductVersion: {
          method: 'GET',
          url: REST_BASE_URL + '/product-releases/product-versions/:versionId',
          isArray: true
        },
        saveForProductVersion: {
          method: 'POST',
          url: REST_BASE_URL + '/product-releases/product-versions/:versionId'
        },
        getAllSupportLevel: {
          method: 'GET',
          url: REST_BASE_URL + '/product-releases/support-level',
          isArray: true
        }
      });

      return Release;
    }
  ]);

})();
