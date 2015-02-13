'use strict';

(function() {

  var module = angular.module('pnc.BuildConfig');

  module.controller('BuildConfigCtrl',
    ['$scope', '$state', 'PncRestClient',
    function($scope, $state, PncRestClient) {

      /* Creates new column object for use with the column-browse-column
       * directive the column browse UI element directive. The object will
       * be wired up so as to automatically keep child and parent objects
       * in sync.
       *
       * param: navigateFn - a function which should navigate to the
       *        desired UI state when an item in the column is clicked.
       *        The function will be passed the clicked item as the sole
       *        parameter.
       *
       * param: updateListFn - a function that should return the list of
       *         items to display.
       *
       * param: parent - the column's parent column.
       */
      var newColumn = function(navigateFn, updateListFn, parent) {
        var columnPrototype = {
          parent: null,
          child: null,
          list: [],
          selected: null,

          setSelected: function(item) {
            var that = this;
            (function() {
              that.selected = item;
              if (that.child) {
                that.child.updateList();
              }
            })();
          },

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
          // newCol.selected = clickedItem;
          // if (newCol.child) {
          //   newCol.child.updateList();
          // }
          navigateFn(clickedItem);
        };

        newCol.updateList = function() {
          newCol.clearSelected();
          newCol.list = updateListFn();
        };

        return newCol;
      };

      /*
       * Create columns for GUI.
       */

      var productCol = newColumn(
        function(product) {
          $state.go('build-config.product.show', {
            productId: product.id });
        },
        function() {
          return PncRestClient.Product.query();
        }
      );

      var versionCol = newColumn(
        function(version) {
          console.log('versionCol >> selected: %O', version);
            $state.go('build-config.product.show.version.show', {
              versionId: version.id,
              productId: productCol.selected.id });
        },
        function() {
          console.log('versionCol.updateList >> productCol = %O', productCol);
          return PncRestClient.Version.query({ productId:
            productCol.selected.id });
        },
        productCol
      );

      // Add columns to scope so can be accessed in the view and
      // from inherriting controllers.
      $scope.columnBrowse = {
        products: productCol,
        versions: versionCol
      };

      // Initialise the first column with values.
      $scope.columnBrowse.products.updateList();
    }
  ]);

  module.controller('ProductListCtrl',
    ['$scope','productList',
    function($scope, productList) {
      console.log('ProductListCtrl >> scope=%O, productList=%O', $scope, productList);
      $scope.products = productList;
    }
  ]);

  module.controller('ProductCtrl', ['$scope', '$stateParams', 'productDetails',
    function ($scope, $stateParams, productDetails) {
      console.log('ProductCtrl::productDetails=%O', productDetails);
      $scope.product = productDetails;
      $scope.columnBrowse.products.setSelected(productDetails);
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
    ['$scope', '$stateParams', '$state', 'productDetails', 'versionDetails',
    function ($scope, $stateParams, $state, productDetails, versionDetails) {
      console.log('VersionCtrl::versionDetails=%O', versionDetails);
      console.log('VersionCtrl::$stateParams=%O', $stateParams);
      console.log('VersionCtrl::$state=%O', $state);
      console.log('VersionCtrl::$scope=%O', $scope);
      $scope.product = productDetails;
      $scope.version = versionDetails;
      $scope.columnBrowse.products.setSelected(productDetails);
      $scope.columnBrowse.versions.setSelected(versionDetails);
    }
  ]);
})();
