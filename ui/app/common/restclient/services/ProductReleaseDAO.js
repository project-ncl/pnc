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
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('ProductReleaseDAO', [
    '$resource',
    'REST_BASE_URL',
    'RELEASE_ENDPOINT',
    'PageFactory',
    'PncCacheUtil',
    function($resource, REST_BASE_URL, RELEASE_ENDPOINT, PageFactory, PncCacheUtil) {
      var ENDPOINT = REST_BASE_URL + RELEASE_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        releaseId: '@id'
      },{
        _getAll: {
          method: 'GET',
          isArray: false
        },
        update: {
          method: 'PUT'
        },
        _getByProductVersion: {
          method: 'GET',
          url: REST_BASE_URL + '/product-releases/product-versions/:versionId',
          isArray: false
        },
        save: {
          method: 'POST'
        },
        _getSupportLevels: {
          method: 'GET',
          url: REST_BASE_URL + '/product-releases/support-level'
        }
      });

      PncCacheUtil.decorateIndexId(resource, 'ProductRelease', 'get');

      _([['_getAll'],
         ['_getByProductVersion']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'ProductRelease', e[0]);
      });

      _([['_getAll', 'getAll'],
         ['_getByProductVersion', 'getByProductVersion'],
         ['_getSupportLevels', 'getSupportLevels']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      PageFactory.decorate(resource, '_getByProductVersion', 'getPagedByProductVersion');

      return resource;
    }
  ]);

})();
