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
            templateUrl: 'build-config/build-config.html',
            controller: 'BuildConfigCtrl'
          }
        }
      })
      .state('build-config.product', {
        url: '/product',
        resolve: {
          productFactory: 'Product',
          productList: function(productFactory) {
            return productFactory.query().$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/build-config.product.html',
            controller: 'ProductListCtrl'
          }
        }
      })
      .state('build-config.product.show', {
        url: '/{productId:int}',
        resolve: {
          productFactory: 'Product',
          productDetails: function(productFactory, $stateParams) {
            return productFactory.get({ productId: $stateParams.productId })
              .$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/build-config.product.show.html',
            controller: 'ProductCtrl'
          }
        }
      })

      .state('build-config.product.show.version', {
        url: '/version',
        abstract: true/*,
        resolve: {
          versionFactory: 'Version',
          versionList: function(versionFactory, $stateParams) {
            return versionFactory.query({ productId: $stateParams.productId })
              .$promise;
          }
        },
        views: {
          'content@build-config': {
            template: '<span></span>',
            controller: 'VersionListCtrl'
          }
        }*/
      })
      .state('build-config.product.show.version.show', {
        url: '/{versionId:int}',
        resolve: {
          versionFactory: 'Version',
          versionDetails: function(versionFactory, $stateParams) {
            return versionFactory.get({
              productId: $stateParams.productId,
              versionId: $stateParams.versionId
            }).$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/build-config.product.show.version.show.html',
            controller: 'VersionCtrl'
          }
        }
      });
  }]);

})();
