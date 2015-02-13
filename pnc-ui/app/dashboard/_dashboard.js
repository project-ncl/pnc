'use strict';

(function() {

  var module = angular.module('pnc.Dashboard', ['ui.router']);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('dashboard', {
      url: '',
      views: {
        'content@': {
          templateUrl: 'dashboard/views/dashboard.html',
          controller: 'DashboardController'
        }
      }
    });
  }]);
})();
