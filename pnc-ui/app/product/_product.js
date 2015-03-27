'use strict';

(function() {

  var module = angular.module('pnc.product', [
    'ui.router',
    'pnc.remote.restClient',
    'pnc.util.header',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('product', {
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
      url: '/product',
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
      url: '/product/{productId:int}',
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
          return restClient.Version.query({ productId: productDetail.id });
        }
      }
    });
    
    $stateProvider.state('product.version', {
      //parent: 'product.detail',
      url: '/product/{productId:int}/version/{versionId:int}',
      templateUrl: 'product/views/product.version.html',
      data: {
         displayName: '{{ versionDetail.version }}'
      },
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
        buildConfigurationSets: function(restClient, $stateParams) {
          return restClient.Version.getAllBuildConfigurationSets({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        }
      }
    });

  }]);
  
})();
