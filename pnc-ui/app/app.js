'use strict';

(function() {
  var app = angular.module('pnc', [
    'ui.router',
    'pnc.Dashboard',
    'pnc.remote',
    'pnc.product',
    'pnc.project',
    'pnc.configuration'

  ]);

  app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider',
    function($stateProvider, $urlRouterProvider, $locationProvider) {

      $locationProvider.html5Mode(false).hashPrefix('!');

      $stateProvider.state('error', {
        url: '/error',
        views: {
          'content@': {
            templateUrl: 'error.html'
          }
        }
      });

      // Redirect any unmatched URLs to the error state.
      $urlRouterProvider.otherwise('/error');
    }
  ]);
})();
