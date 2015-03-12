
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
      templateUrl: 'build-config/views/pnc-browse-column.html',
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
          return ($scope.pncTopLevel || ($scope.pncItems && $scope.pncItems.length > 0));
        };

      }],
    };
  });

  module.directive('pncHeader', function() {
    var tmpl = '<div class="row"><div class="col-md-12"><div class="row"><span ng-transclude></span></div><div class="row"><hr></div></div></div>';

    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      template: tmpl
    };
  });

  module.directive('pncHeaderTitle', function() {
    var tmpl = '<div class="col-md-6"><h1 ng-transclude></h1></div>';

    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      template: tmpl
    };
  });

  module.directive('pncHeaderButtons', function() {
    var tmpl = '<div class="col-md-6">' +
    '<span class="btn-group pull-right h1" role="group" ' +
    'aria-label="Action Toolbar" ng-transclude></span></div>';
    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      template: tmpl
    };
  });
})();
