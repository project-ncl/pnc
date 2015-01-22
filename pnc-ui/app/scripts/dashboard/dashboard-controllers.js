'use strict';

(function() {

  var module = angular.module('pnc.Dashboard');

  module.controller('DashboardCtrl', ['$scope', '$stateParams',
    function ($scope, $stateParams) {
      $scope.title = 'Dashboard';
    }
  ]);

})();
