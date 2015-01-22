'use strict';

(function() {
  var module = angular.module('pnc.BuildConfig');

  module.controller('BuildConfigCtrl', ['$scope', '$routeParams', 
    function ($scope, $routeParams) {
      $scope.title = 'BuildConfig';
      console.log($routeParams);
    }
  ]);

  module.controller('ProductCtrl', ['$scope', 'Product',
    function ($scope, Product) {
      $scope.tbody = Product.query();
      $scope.title = 'Products';
      $scope.thead = ['Product Id', 'Name', 'Description'];
      $scope.properties = ['id', 'name', 'description'];
      $scope.href = function(product) {
        return '#!/build-config/products/' + product.id + '/version'; 
      };
      $scope.action = 'View Versions';
    }
  ]);

  module.controller('VersionCtrl', ['$scope', '$routeParams', 'Version',
    function ($scope, $routeParams, Version) {
      console.log('build-config/VersionCtrl: $routeParams=%O', 
        $routeParams);
      $scope.productId = $routeParams.productId;
      $scope.tbody = Version.query({productId: $scope.productId});
      $scope.title = 'Versions';
      $scope.thead = ['Version Id', 'Version'];
      $scope.properties = ['id', 'version'];
      $scope.href = function(version) {
        return '#!/build-config/products/' +  $routeParams.productId + '/version' + 
                version.id + '/project'; 
      };
      $scope.action = 'View Projects';
    }
  ]);

})();