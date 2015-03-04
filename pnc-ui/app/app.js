'use strict';

(function() {
  var app = angular.module('pnc', [
    'ngResource',
    'ui.router',
    'pnc.Dashboard',
    'pnc.BuildConfig',
    'pnc.remote'
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
