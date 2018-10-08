/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
(function () {
  'use strict';

  var app = angular.module('pnc', [
    'ngAnimate',
    'angular-loading-bar',
    'ui.router',
    'ui.bootstrap',
    'patternfly.notification',
    'pnc.common',
    'pnc.dashboard',
    'pnc.product',
    'pnc.projects',
    'pnc.build-configs',
    'pnc.build-records',
    'pnc.build-groups',
    'pnc.milestone',
    'pnc.release',
    'pnc.build-group-records',
    'pnc.report',
    'pnc.properties',
    'pnc.repository-configurations'
  ]);

  app.config([
    '$stateProvider',
    '$urlRouterProvider',
    '$locationProvider',
    '$httpProvider',
    'NotificationsProvider',
    'cfpLoadingBarProvider',
    '$animateProvider',
    function($stateProvider, $urlRouterProvider, $locationProvider,
      $httpProvider, NotificationsProvider, cfpLoadingBarProvider, $animateProvider) {

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

    $animateProvider.classNameFilter(/pnc-animate/);
  }]);

  /**
   * Configure remote api clients with addresses from the pncProperties
   * retrieved in initialise.js.
   */
  app.config([
    'pncProperties',
    'restConfigProvider',
    'daConfigProvider',
    'authConfigProvider',
    function (pncProperties, restConfigProvider, daConfigProvider, authConfigProvider) {
      restConfigProvider.setPncUrl(pncProperties.pncUrl);
      restConfigProvider.setPncNotificationsUrl(pncProperties.pncNotificationsUrl);
      restConfigProvider.setDaUrl(pncProperties.daUrl);

      daConfigProvider.setDaUrl(pncProperties.daUrl);

      authConfigProvider.setSsoTokenLifespan(pncProperties.ssoTokenLifespan);
    }
  ]);

  app.run([
    '$log',
    'authService',
    'messageBus',
    'restConfig',
    'pncNotify',
    'onBootNotifications',
    function($log, authService, messageBus, restConfig, pncNotify, onBootNotifications) {

      Object.keys(onBootNotifications).forEach(function (key) {
        onBootNotifications[key].forEach(function (notification) {
          if(angular.isString(notification)) {
            pncNotify[key](notification);
          } else if (angular.isObject(notification)) {
            pncNotify[key](notification.message, notification.actionTitle, notification.actionCallback, notification.menuActions);
          }
        });
      });

      messageBus.registerListener(['$log', function ($log) {
        return function (message) {
          $log.debug('MessageBus received: %O', message);
        };
      }]);

      messageBus.connect(restConfig.getPncNotificationsUrl());

      if (authService.isAuthenticated()) {
        authService.getPncUser().then(function (result) {
          $log.info('Authenticated with PNC as: %O', result);
        });
      }
  }]);

})();
