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

  var module = angular.module('pnc.product', [
    'ui.router',
    'ui.bootstrap',
    'pnc.common.restclient',
    'pnc.common.authentication',
    'pnc.common.pnc-client',
    'pnc.common.select-modals',
    'patternfly',
    'patternfly.views'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('product', {
      url: '/product',
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'product.list'
      }
    });

    $stateProvider.state('product.list', {
      url: '/',
      templateUrl: 'product/views/product.list.html',
      data: {
        displayName: 'Products',
        title: 'Products'
      },
      controller: 'ProductListController',
      controllerAs: 'listCtrl',
      resolve: {
        productList: function(ProductDAO) {
          return ProductDAO.getAll().$promise;
        }
      }
    });

    $stateProvider.state('product.detail', {
      url: '/{productId:int}',
      templateUrl: 'product/views/product.detail.html',
      data: {
         displayName: '{{ productDetail.name }}',
         title: '{{ productDetail.name }}'
      },
      controller: 'ProductDetailController',
      controllerAs: 'detailCtrl',
      resolve: {
        productDetail: function(ProductDAO, $stateParams) {
          return ProductDAO.get({ productId: $stateParams.productId })
          .$promise;
        }
      }
    });

    $stateProvider.state('product.detail.version', {
      //parent: 'product.detail',
      url: '/version/{versionId:int}',
      views: {
        'content@': {
          templateUrl: 'product/views/product.version.html',
          controller: 'ProductVersionController',
          controllerAs: 'versionCtrl',
        }
      },
      data: {
         displayName: '{{ versionDetail.version }}',
         title: '{{ versionDetail.version }} | {{ productDetail.name }}'
      },
      resolve: {
        productDetail: function(ProductDAO, $stateParams) {
          return ProductDAO.get({ productId: $stateParams.productId })
          .$promise;
        },
        versionDetail: function(ProductVersionDAO, $stateParams) {
          return ProductVersionDAO.get({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        }
      }
    });

    $stateProvider.state('product.create', {
      url: '/create',
      templateUrl: 'product/views/product.create.html',
      data: {
        displayName: 'Create Product',
        title: 'Create Product',
        requireAuth: true
      },
      controller: 'ProductCreateController',
      controllerAs: 'productCreateCtrl'
    });

    $stateProvider.state('product.detail.createVersion', {
      url: '/createversion',
      views: {
        'content@': {
          templateUrl: 'product/views/product.version.create.html',
          controller: 'ProductVersionCreateController',
          controllerAs: 'productVersionCreateCtrl',
        }
      },
      data: {
        displayName: 'Create Product Version',
        title: '{{ productDetail.name }} | Create Product Version',
        requireAuth: true
      },
      resolve: {
        productDetail: function(ProductDAO, $stateParams) {
          return ProductDAO.get({ productId: $stateParams.productId })
          .$promise;
        },
      },
    });

  }]);

})();
