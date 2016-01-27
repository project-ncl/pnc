/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.product', [
    'ui.router',
    'pnc.common.restclient',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('product', {
      url: '/product',
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'product.list'
      }
    });

    $stateProvider.state('product.list', {
      url: '',
      templateUrl: 'product/views/product.list.html',
      data: {
        displayName: 'Products'
      },
      controller: 'ProductListController',
      controllerAs: 'listCtrl',
      resolve: {
        productList: function(ProductDAO) {
          return ProductDAO.getAll();
        }
      }
    });

    $stateProvider.state('product.detail', {
      url: '/{productId:int}',
      templateUrl: 'product/views/product.detail.html',
      data: {
         displayName: '{{ productDetail.name }}',
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
         displayName: '{{ versionDetail.version }}'
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
        displayName: 'Create Product'
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
        displayName: 'Create Product Version'
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
