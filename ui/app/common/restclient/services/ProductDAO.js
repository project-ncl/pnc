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

  module.value('PRODUCT_ENDPOINT', '/products/:productId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('ProductDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'PRODUCT_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function ($resource, $injector, REST_BASE_URL, PRODUCT_ENDPOINT, PageFactory, qh, PncCacheUtil) {
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
      });

      PncCacheUtil.decorateIndexId(resource, 'Product', 'get');

      _([['_getAll']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'Product', e[0]);
      });

      _([['_getAll', 'getAll']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      _([['_getAll', 'getAllPaged']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getProductVersions = function () {
        return $injector.get('ProductVersionDAO').getByProduct({productId: this.id});
      };

      return resource;
    }
  ]);

})();
