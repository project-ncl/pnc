'use strict';

(function() {

  var module = angular.module('pnc.BuildConfig');

  module.controller('BuildConfigCtrl',
    ['$scope', '$state', 'Product', 'Version',
    function($scope, $state, Product, Version) {

      var newColumn = function(navigateFn, updateListFn, parent) {
        var columnPrototype = {
          parent: null,
          child: null,
          list: [],
          selected: null,

          clearSelected: function() {
            var that = this;
            (function() {
              that.selected = null;
              if (that.child) {
                that.child.clearSelected();
              }
            })();
          }
        };

        var newCol = Object.create(columnPrototype);

        if (parent) {
          newCol.parent = parent;
          parent.child = newCol;
        }

        newCol.click = function(clickedItem) {
          console.log('Click >> clickedItem: %O', clickedItem);
          newCol.selected = clickedItem;
          if (newCol.child) {
            newCol.child.updateList();
          }
          navigateFn(clickedItem);
        };

        newCol.updateList = function() {
          newCol.clearSelected();
          newCol.list = updateListFn();
        };

        return newCol;
      };

      var productCol = newColumn(
        function(product) {
          $state.go('build-config.product-list.product', {
            productId: product.id });
        },
        function() {
          return Product.query();
        }
      );

      var versionCol = newColumn(
        function(version) {
          console.log('VersionColumn >> selected: %O', version);
        },
        function() {
          console.log('productCol = %O', productCol);
          return Version.query({ productId:
            productCol.selected.id });
        },
        productCol
      );

      $scope.columnBrowse = {
        products: productCol,
        versions: versionCol
      }

      $scope.columnBrowse.products.updateList();
    }
  ]);

  module.controller('ProductListCtrl',
    ['$scope', '$state', '$stateParams', 'productList',
    function($scope, $state, $stateParams, productList) {
      console.log('currentProduct=%O', $scope.currentProduct);
      $scope.products = productList;
      $scope.navigate = function(product) {
        $state.go('build-config.product-list.product',
          { productId: product.id });
      };

      console.log('ProductListCtrl::productList=%O', productList);
    }
  ]);

  module.controller('ProductCtrl', ['$scope', '$stateParams', 'productDetails',
    function ($scope, $stateParams, productDetails) {
      console.log('ProductCtrl::productDetails=%O', productDetails);
      $scope.product = productDetails;
    }
  ]);

  module.controller('VersionListCtrl', ['$scope', '$stateParams', 'versionList',
    function ($scope, $stateParams, versionList) {
      console.log('VersionListCtrl::versionList=%O', versionList);
      console.log('VersionListCtrl::$stateParams=%O', $stateParams);
      $scope.versions = versionList;
    }
  ]);

  module.controller('VersionCtrl',
    ['$scope', '$stateParams', '$state', 'versionDetails',
    function ($scope, $stateParams, $state, versionDetails) {
      console.log('VersionCtrl::versionDetails=%O', versionDetails);
      console.log('VersionCtrl::$stateParams=%O', $stateParams);
      console.log('VersionCtrl::$state=%O', $state);
      console.log('VersionCtrl::$scope=%O', $scope);
      $scope.version = versionDetails;
    }
  ]);

})();
