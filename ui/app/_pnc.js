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
    'pnc.report',
    'pnc.properties',
    'pnc.common.authentication'
  ]);

  app.config(function($stateProvider, $urlRouterProvider, $locationProvider,
    $httpProvider, NotificationsProvider, cfpLoadingBarProvider) {

    $locationProvider.html5Mode(false);

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

    // Show loading bar without additional spinner.
    cfpLoadingBarProvider.includeSpinner = false;

    $httpProvider.interceptors.push('httpResponseInterceptor');
    $httpProvider.interceptors.push('unwrapPageResponseInterceptor');
    $httpProvider.interceptors.push('httpAuthenticationInterceptor');
  });

  /**
   * Configure remote api clients with addresses from the pncProperties
   * retrieved in initialise.js.
   */
  app.config([
    'pncProperties',
    'restConfigProvider',
    'daConfigProvider',
    function (pncProperties, restConfigProvider, daConfigProvider) {
      restConfigProvider.setPncUrl(pncProperties.pncUrl);
      restConfigProvider.setPncNotificationsUrl(pncProperties.pncNotificationsUrl);
      restConfigProvider.setDaUrl(pncProperties.daUrl);
      restConfigProvider.setDaImportUrl(pncProperties.daImportUrl);

      daConfigProvider.setDaUrl(pncProperties.daUrl);
      daConfigProvider.setDaImportUrl(pncProperties.daImportUrl);
      daConfigProvider.setDaImportRpcUrl(pncProperties.daImportRpcUrl);
  }]);

  app.run(function($rootScope, $log, $state, authService) {

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
      }
    );

  });

})();
