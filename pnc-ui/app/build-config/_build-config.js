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
            templateUrl: 'build-config/views/build-config.html',
            controller: 'BuildConfigCtrl'
          }
        }
      })
      .state('build-config.product', {
        url: '/product',
        resolve: {
          restClient: 'PncRestClient',
          productList: function(restClient) {
            return restClient.product.query().$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/views/product.html',
            controller: 'ProductListCtrl'
          }
        }
      })
      .state('build-config.product.show', {
        url: '/{productId:int}',
        resolve: {
          restClient: 'restClient',
          productDetails: function(restClient, $stateParams) {
            return restClient.product.get({ productId: $stateParams.productId })
              .$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/views/product.show.html',
            controller: 'ProductCtrl'
          }
        }
      })

      .state('build-config.product.show.version', {
        url: '/version',
        abstract: true/*,
        resolve: {
          restClient: 'PncRestClient',
          versionList: function(restClient, $stateParams) {
            return restClient.version.query(
              { productId: $stateParams.productId }).$promise;
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
          restClient: 'PncRestClient',
          versionDetails: function(restClient, $stateParams) {
            return restClient.version.get({
              productId: $stateParams.productId,
              versionId: $stateParams.versionId
            }).$promise;
          }
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/views/product.show.version.show.html',
            controller: 'VersionCtrl'
          }
        }
      });
  }]);

})();
