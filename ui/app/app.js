/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function() {
  var app = angular.module('pnc', [
    'ui.router',
    'ui.bootstrap',
    'pnc.Dashboard',
    'pnc.remote',
    'pnc.product',
    'pnc.project',
    'pnc.configuration',
    'pnc.record',
    'pnc.configuration-set',
    'pnc.websockets',
    'pnc.milestone',
    'pnc.release'
  ]);

  var authEnabled = pnc_globals.enableAuth; // jshint ignore:line
  var keycloak;


  (function bootstrapPncUi() {

    var startAngular = function() {
      angular.bootstrap(document, ['pnc']);
    };

    angular.element(document).ready(function () {

      if (authEnabled) {

        keycloak = new Keycloak('keycloak.json');
        keycloak.init({ onLoad: 'login-required' }).success(function () {
          startAngular();
        }).error(function () {
          window.location.reload();
        });

      } else {

        startAngular();

      }

    });
  })();


  app.constant('PROPERTIES', {
    AUTH_ENABLED: authEnabled
  });

  app.config(function($stateProvider, $urlRouterProvider, $locationProvider,
                      $httpProvider, keycloakProvider, PROPERTIES) {
    $locationProvider.html5Mode(false).hashPrefix('!');

    $stateProvider.state('error', {
      url: '/error',
      views: {
        'content@': {
          templateUrl: 'error.html'
        }
      }
    });

    $urlRouterProvider.when('', '/');

    // Redirect any unmatched URLs to the error state.
    $urlRouterProvider.otherwise('/error');

    if (PROPERTIES.AUTH_ENABLED) {
      keycloakProvider.setKeycloak(keycloak);
      $httpProvider.interceptors.push('authInterceptor');
    } else {
      keycloakProvider.useMockKeycloak();
    }

  });

  app.run(function($rootScope, $log, authService) {

    // Handle errors with state changes.
    $rootScope.$on('$stateChangeError',
      function(event, toState, toParams, fromState, fromParams, error) {
        $log.debug('Caught $stateChangeError: event=%O, toState=%O, ' +
          'toParams=%O, fromState=%O, fromParams=%O, error=%O',
          event, toState, toParams, fromState, fromParams, error);
      }
    );

    $rootScope.auth = authService;
  });

})();
