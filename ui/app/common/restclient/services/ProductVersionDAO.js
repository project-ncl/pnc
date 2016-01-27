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
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('ProductVersionDAO', [
    '$resource',
    'REST_BASE_URL',
    'PRODUCT_VERSION_ENDPOINT',
    'PageFactory',
    'ProductDAO',
    'ProductMilestoneDAO',
    'cachedGetter',
    'ProductReleaseDAO',
    'QueryHelper',
    function($resource, REST_BASE_URL, PRODUCT_VERSION_ENDPOINT,
             PageFactory, ProductDAO, ProductMilestoneDAO, cachedGetter, ProductReleaseDAO, qh) {
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
        _getBCSets: {
          method: 'GET',
          url: ENDPOINT + '/build-configuration-sets' + qh.searchOnly(['name'])
        },
        _getByProduct: {
          method: 'GET',
          url: REST_BASE_URL + '/products/:productId/product-versions' + qh.searchOnly(['version'])
        }
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getBCSets', 'getAllBuildConfigurationSets');
      PageFactory.decorateNonPaged(resource, '_getByProduct', 'getAllForProduct');

      PageFactory.decorate(resource, '_getBCSets', 'getPagedBCSets');
      PageFactory.decorate(resource, '_getByProduct', 'getPagedByProduct');

      resource.prototype.getProduct = cachedGetter(
        function (version) {
          return ProductDAO.get({productId: version.productId});
        }
      );

      resource.prototype.getMilestones = cachedGetter(
        function (version) {
          return ProductMilestoneDAO.getAllForProductVersion({versionId: version.id});
        }
      );

      resource.prototype.getReleases = cachedGetter(
        function (version) {
          return ProductReleaseDAO.getAllForProductVersion({versionId: version.id});
        }
      );
      return resource;
    }
  ]);

})();
