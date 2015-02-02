'use strict';

(function() {

  var module = angular.module('pnc.BuildConfig', ['ui.router']);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider
      .state('build-config', {
        abstract: true,
        url: '/build-config',
        views: {
          'content@': {
            templateUrl: 'scripts/build-config/build-config.html',
          }
        }
      })

      .state('build-config.product-list', {
        url: '/product',
        resolve: {
          productFactory: 'Product',
          productList: function(productFactory) {
            return productFactory.query().$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'scripts/build-config/build-config.product-list.html',
            controller: 'ProductListCtrl'
          }
        }
      })
      .state('build-config.product-list.product', {
        url: '/product/{productId:int}',
        resolve: {
          productFactory: 'Product',
          productDetails: function(productFactory, $stateParams) {
            return productFactory.get({ productId: $stateParams.productId })
              .$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'scripts/build-config/build-config.product.show.html',
            controller: 'ProductCtrl'
          }
        }
      })

      .state('build-config.product.show.version', {
        abstract: true
      });
  }]);

})();
