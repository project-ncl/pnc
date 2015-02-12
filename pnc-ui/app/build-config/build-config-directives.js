
'use strict';

(function() {

  var DEFAULT_ID_FIELD = 'id';
  var DEFAULT_NAME_FIELD = 'name';

  var module = angular.module('pnc.BuildConfig');

  module.directive('pncBrowseColumn', function () {
    return {
      restrict: 'EA',
      scope: {
        pncTitle: '@',
        pncTopLevel: '@',
        pncIdField: '@',
        pncNameField: '@',
        pncItems: '=',
        pncSelected: '=',
        pncWatch: '=',
        pncClick: '&'
      },
      templateUrl: 'build-config/pnc-browse-column.html',
      controller: ['$scope', function($scope) {

        $scope.pncIdField = $scope.pncIdField || DEFAULT_ID_FIELD;
        $scope.pncNameField = $scope.pncNameField || DEFAULT_NAME_FIELD;

        $scope.isActive = function(item) {
          if (!$scope.pncSelected) {
            return false;
          }
          return item.id === $scope.pncSelected.id;
        };

        $scope.shouldShow = function() {
          return ($scope.pncTopLevel || ($scope.pncItems.length > 0));
        };

      }],
    };
  });
})();
