/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.products', []).config([
    '$stateProvider',
    ($stateProvider) => {

      $stateProvider.state('products', {
        url: '/products',
        abstract: true,
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'products.list'
        }
      });

      $stateProvider.state('products.list', {
        url: '',
        component: 'pncProductsListPage',
        resolve: {
          products: ['ProductResource', 'SortHelper', (ProductResource, SortHelper) => ProductResource.query(SortHelper.getSortQueryString('productsList')).$promise]
        },
        data: {
          displayName: 'Products',
          title: 'Products'
        }
      });

      $stateProvider.state('products.create', {
        url: '/create',
        component: 'pncCreateProductPage',
        data: {
          displayName: 'Create Product',
          title: 'Create Product',
          requireAuth: true
        }
      });

      $stateProvider.state('products.detail', {
        url: '/{productId}',
        component: 'pncProductDetailPage',
        data: {
          displayName: '{{ product.name }}',
          title: '{{ product.name }}'
        },
        resolve: {
          product: [
            'ProductResource',
            '$stateParams',
            (ProductResource, $stateParams) => ProductResource.get({ id: $stateParams.productId }).$promise
          ],
          productVersions: ['product', product => product.$queryProductVersions()]
        }
      });

    }]);

})();
