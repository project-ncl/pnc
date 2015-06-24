'use strict';

(function() {

  var module = angular.module('pnc.product');

  module.config(['$stateProvider', function($stateProvider) {


    $stateProvider.state('product.detail', {
      url: '/{productId:int}',
      templateUrl: 'product/detail/product.detail.html',
      data: {
         displayName: '{{ productDetail.name }}'
      },
      controller: 'ProductDetailController',
      controllerAs: 'detailCtrl',
      resolve: {
        productDetail: function(restClient, $stateParams) {
          return restClient.Product.get({ productId: $stateParams.productId })
          .$promise;
        },
        productVersions: function(restClient, productDetail) {
          return restClient.Version.query({ productId: productDetail.id }).$promise;
        },
      }
    });

  }]);

})();
