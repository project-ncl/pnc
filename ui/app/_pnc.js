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
    'angular-loading-bar',
    'ui.router',
    'ui.bootstrap',
    'patternfly.notification',
    'pnc.dashboard',
    'pnc.product',
    'pnc.project',
    'pnc.configuration',
    'pnc.record',
    'pnc.configuration-set',
    'pnc.milestone',
    'pnc.release',
    'pnc.common.directives',
    'pnc.common.websockets',
    'pnc.common.events',
    'pnc.common.buildNotifications',
    'pnc.configuration-set-record',
    'pnc.common.restclient',
    'pnc.import',
    'pnc.report'
  ]);

  var keycloak;

  // Bootstrap UI.
  angular.element(document).ready(function () {
    keycloak = new Keycloak('keycloak.json');

    keycloak.init({ onLoad: 'check-sso' }).success(function () {
      angular.bootstrap(document, ['pnc']);
    }).error(function () {
      $(document.body).append('<div class="page-header"><h1>Error in authentication bootstrap process</h1></div>');
      $(document.body).append('<p>Please report this error to the system administrator.</p>');
    });
  });

  app.config(function($stateProvider, $urlRouterProvider, $locationProvider,
    $httpProvider, keycloakProvider, NotificationsProvider) {

    $locationProvider.html5Mode(false);

    // Redirects URLS with the old '#!' URL prefix to the newer
    // format without the !. This should be removed after 0.7.
    $urlRouterProvider.rule(function ($injector, $location) {
        var path = $location.path();
        if (path.indexOf('!') > -1) {
          return path.replace(/\/!/, '');
        }
    });

    // Allows dashboard to be root state.
    $urlRouterProvider.when('', '/');

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

    $httpProvider.interceptors.push('httpResponseInterceptor');
    $httpProvider.interceptors.push('unwrapPageResponseInterceptor');

    keycloakProvider.setKeycloak(keycloak);
    $httpProvider.interceptors.push('httpAuthenticationInterceptor');
  });

  app.run(function($rootScope, $log, $state, authService, keycloak) {

    if (authService.isAuthenticated()) {
      authService.getPncUser().$promise.then(function(result) {
        $log.info('Authenticated with PNC as: %O', result);
      });
    }

    // Handle errors with state changes.
    $rootScope.$on('$stateChangeError',
      function(event, toState, toParams, fromState, fromParams, error) {

        $log.debug('Caught $stateChangeError: event=%O, toState=%O, ' +
                   'toParams=%O, fromState=%O, fromParams=%O, error=%O',
                   event, toState, toParams, fromState, fromParams, error);
        $log.error('Error navigating to "%s": %s %s', toState.url, error.status,
                   error.statusText);

        $rootScope.showSpinner = false;

        switch (error.status) {
          case 401:
            keycloak.login();
            break;
          case 403:
            $state.go('error', {
              message: 'You do not have the required permission to access this resource'
            });
            break;
        }

      }
    );

    $rootScope.$on('$stateChangeStart', function(event, toState) {
      if (toState.resolve) {
        $rootScope.showSpinner = true;
      }
    });
    $rootScope.$on('$stateChangeSuccess', function(event, toState) {
      if (toState.resolve) {
        $rootScope.showSpinner = false;
      }
    });
  });

})();
