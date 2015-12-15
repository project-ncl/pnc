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

  module.value('PRODUCT_VERSION_ENDPOINT', '/product-versions/:versionId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('ProductVersionDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'PRODUCT_VERSION_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function($resource, $injector, REST_BASE_URL, PRODUCT_VERSION_ENDPOINT,
             PageFactory, qh, PncCacheUtil) {
      var ENDPOINT = REST_BASE_URL + PRODUCT_VERSION_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        versionId: '@id'
      },{
        _getAll: {
          method: 'GET'
        },
        update: {
          method: 'PUT'
        },
        _getByProduct: {
          method: 'GET',
          url: REST_BASE_URL + '/products/:productId/product-versions' + qh.searchOnly(['version'])
        },
        _getByBC: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/:configurationId/product-versions'
        }
      });

      _([['get', 'versionId']]).each(function(e) {
        PncCacheUtil.decorateIndexId(resource, 'ProductVersion', e[0], e[1]);
      });

      _([['_getAll'],
         ['_getByBC'],
         ['_getByProduct']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'ProductVersion', e[0]);
      });

      _([['_getAll', 'getAll'],
         ['_getByBC', 'getByBC'],
         ['_getByProduct', 'getByProduct']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      _([['_getByProduct', 'getPagedByProduct']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getProduct = function () {
        return $injector.get('ProductDAO').get({ productId: this.productId });
      };

      resource.prototype.getMilestones = function () {
        return $injector.get('ProductMilestoneDAO').getByProductVersion({ versionId: this.id });
      };

      resource.prototype.getReleases = function () {
        return $injector.get('ProductReleaseDAO').getByProductVersion({ versionId: this.id });
      };

      resource.prototype.getBCSets = function () {
        return $injector.get('BuildConfigurationSetDAO').getByProductVersion({ versionId: this.id });
      };

      resource.prototype.getPagedBCSets = function () {
        return $injector.get('BuildConfigurationSetDAO').getPagedByProductVersion({ versionId: this.id });
      };

      resource.prototype.getBCs = function () {
        return $injector.get('BuildConfigurationDAO').getByProductVersion({ productId: this.productId, versionId: this.id });
      };

      return resource;
    }
  ]);

})();
