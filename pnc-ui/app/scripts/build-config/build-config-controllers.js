'use strict';

(function() {
  var module = angular.module('pnc.BuildConfig');

  module.controller('ProductListCtrl', ['$scope', '$state', '$stateParams', 'productList',
    function($scope, $state, $stateParams, productList) {
      $scope.products = productList;

      $scope.navigate = function(product) {
        $state.go('build-config.product-list.product', { productId: product.id });
      }

      console.log('ProductListCtrl::productList=%O', productList);
    }
  ]);

  module.controller('ProductCtrl', ['$scope', '$stateParams', 'productDetails',
    function ($scope, $stateParams, productDetails) {
      console.log('ProductCtrl::productDetails=%O', productDetails);
      $scope.product = productDetails;
    }
  ]);

})();
