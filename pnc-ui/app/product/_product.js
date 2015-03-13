'use strict';

(function() {

  var module = angular.module('pnc.product', [
    'ui.router',
    'pnc.remote.restClient',
    'pnc.util.header'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('product', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: '/common/templates/single-col-center.tmpl.html'
        }
      }
    });

    $stateProvider.state('product.list', {
      url: '/product',
      templateUrl: '/product/views/product.list.html',
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
      url: '/product/{productId:int}',
      templateUrl: 'product/views/product.detail.html',
      controller: 'ProductDetailController',
      controllerAs: 'detailCtrl',
      resolve: {
        restClient: 'PncRestClient',
        productDetail: function(restClient, $stateParams) {
          return restClient.Product.get({ productId: $stateParams.productId })
          .$promise;
        },
        productVersions: function(restClient, productDetail) {
          return restClient.Version.query({ productId: productDetail.id });
        }
      }
    });

    $stateProvider.state('product.version', {
      url: '/product/{productId:int}/version/{versionId:int}',
      templateUrl: 'product/views/product.version.html',
      controller: 'ProductVersionController',
      controllerAs: 'versionCtrl',
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
      }
    });

  }]);

})();
