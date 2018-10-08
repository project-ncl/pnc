/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

  module.value('PRODUCT_ENDPOINT', '/products/:productId');

  /**
   *
   */
  module.factory('ProductDAO', [
    '$resource',
    'REST_BASE_URL',
    'PRODUCT_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    function ($resource, REST_BASE_URL, PRODUCT_ENDPOINT, PageFactory, qh) {
      var ENDPOINT = REST_BASE_URL + PRODUCT_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        productId: '@id'
      }, {
        _getAll: {
          method: 'GET',
          url: ENDPOINT + qh.searchOnly(['name', 'description', 'abbreviation', 'productCode', 'pgmSystemName'])
        },
        update: {
          method: 'PUT'
        },
        _getVersions: {
          method: 'GET',
          url: ENDPOINT + '/product-versions'
        }
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getVersions', 'getVersions');

      PageFactory.decorate(resource, '_getAll', 'getAll');

      return resource;
    }
  ]);

})();
