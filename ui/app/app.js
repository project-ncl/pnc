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
    'patternfly.notification',
    'pnc.Dashboard',
    'pnc.remote',
    'pnc.product',
    'pnc.project',
    'pnc.configuration',
    'pnc.record',
    'pnc.configuration-set',
    'pnc.milestone',
    'pnc.release',
    'pnc.common.directives',
    'pnc.common.websockets',
    'pnc.common.buildNotifications'
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
    $httpProvider, keycloakProvider, NotificationsProvider, PROPERTIES) {

    $locationProvider.html5Mode(false).hashPrefix('!');

    // Allows dashboard to be root state.
    $urlRouterProvider.when('', '/record');

    // Redirect any unmatched URLs to the error state.
    $urlRouterProvider.otherwise('/error');

    // Create error state, the `title` and `message` state params can be
    // overidden.
    $stateProvider.state('error', {
      url: '/error',
      params: {
        title: 'Error',
        message: 'The requested resource could not be found.'
      },
      views: {
        'content@': {
          templateUrl: 'error.html',
          controller: ['$stateParams', function($stateParams) {
            this.title = $stateParams.title;
            this.message = $stateParams.message;
          }],
          controllerAs: 'errorCtrl'
        },
      }
    });

    // Configure pop-up notifications
    NotificationsProvider.setDelay(12000);

    if (PROPERTIES.AUTH_ENABLED) {
      keycloakProvider.setKeycloak(keycloak);
      $httpProvider.interceptors.push('authInterceptor');
    } else {
      keycloakProvider.useMockKeycloak();
    }

  });

  app.run(function($rootScope, $log, $state, authService, keycloak) {

    // Handle errors with state changes.
    $rootScope.$on('$stateChangeError',
      function(event, toState, toParams, fromState, fromParams, error) {

        $log.debug('Caught $stateChangeError: event=%O, toState=%O, ' +
                   'toParams=%O, fromState=%O, fromParams=%O, error=%O',
                   event, toState, toParams, fromState, fromParams, error);
        $log.error('Error navigating to "%s": %s %s', toState.url, error.status,
                   error.statusText);

        switch (error.status) {
          case 401:
            keycloak.login();
            break;
          case 403:
            $state.go('error', { message: 'You do not have the required permission to access this resource' });
            break;
        }

      }
    );

  });

})();
