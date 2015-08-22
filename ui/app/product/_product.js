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
        restClient: 'PncRestClient',
        productList: function(restClient) {
          return restClient.Product.query().$promise;
        }
      },
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
        restClient: 'PncRestClient',
        productDetail: function(restClient, $stateParams) {
          return restClient.Product.get({ productId: $stateParams.productId })
          .$promise;
        },
        productVersions: function(restClient, productDetail) {
          return restClient.Product.getVersions({ productId: productDetail.id }).$promise;
        },
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
        restClient: 'PncRestClient',
        productDetail: function(restClient, $stateParams) {
          return restClient.Product.get({ productId: $stateParams.productId })
          .$promise;
        },
        versionDetail: function(restClient, $stateParams) {
          return restClient.Version.get({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        },
        buildConfigurationSets: function(restClient, $stateParams) {
          return restClient.Version.getAllBuildConfigurationSets({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        },
        buildConfigurations: function(restClient, $stateParams) {
          return restClient.Configuration.getAllForProductVersion({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        },
        productReleases: function(restClient, $stateParams) {
          return restClient.Release.getAllForProductVersion({
            versionId: $stateParams.versionId }).$promise;
        },
        productMilestones: function(restClient, $stateParams) {
          return restClient.Milestone.getAllForProductVersion({
            versionId: $stateParams.versionId }).$promise;
        },
      }
    });

    $stateProvider.state('product.create', {
      url: '/product/create',
      templateUrl: 'product/views/product.create.html',
      data: {
        displayName: 'Create Product'
      },
      controller: 'ProductCreateController',
      controllerAs: 'productCreateCtrl',
      resolve: {
        restClient: 'PncRestClient'
      },
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
        restClient: 'PncRestClient',
        productDetail: function(restClient, $stateParams) {
          return restClient.Product.get({ productId: $stateParams.productId })
          .$promise;
        },
      },
    });

  }]);

})();
