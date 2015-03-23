'use strict';

(function() {
  var app = angular.module('pnc', [
    'ui.router',
    'pnc.Dashboard',
    'pnc.remote',
    'pnc.product',
    'pnc.project',
    'pnc.configuration',
    'pnc.record',
    'pnc.configuration-set',
    'pnc.websockets'
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

  app.run(['$rootScope', '$log', 'AuthService',

    // Handle errors with state changes.
    function($rootScope, $log, AuthService) {
      AuthService.login('keycloak.json');

      $rootScope.$on('$stateChangeError',
        function(event, toState, toParams, fromState, fromParams, error) {
          $log.debug('Caught $stateChangeError: event=%O, toState=%O, ' +
            'toParams=%O, fromState=%O, fromParams=%O, error=%O',
            event, toState, toParams, fromState, fromParams, error);
        }
      );
    }
  ]);

})();
