/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
    'pnc.products',
    'pnc.product-versions',
    'pnc.projects',
    'pnc.build-configs',
    'pnc.builds',
    'pnc.group-configs',
    'pnc.product-milestones',
    'pnc.product-releases',
    'pnc.group-builds',
    'pnc.report',
    'pnc.properties',
    'pnc.scm-repositories',
    'pnc.artifacts'
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
    $locationProvider.hashPrefix('');  // remove a '!' prefix to restore the original behavior

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
      data: {
        displayName: 'Error',
        title: 'Error'
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
    $httpProvider.defaults.headers.patch = { 'Content-Type': 'application/json-patch+json; charset=utf-8' };

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
      // representing PNC REST API version 1
      restConfigProvider.setPncUrl(pncProperties.legacyExternalPncUrl);

      // representing PNC REST API version 2+
      restConfigProvider.setPncRestUrl(pncProperties.externalPncUrl);

      restConfigProvider.setPncNotificationsUrl(pncProperties.pncNotificationsUrl);
      restConfigProvider.setDaUrl(pncProperties.externalDaUrl);
      restConfigProvider.setKafkaStoreUrl(pncProperties.externalKafkaStoreUrl);

      daConfigProvider.setDaUrl(pncProperties.externalDaUrl);

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

      if (authService.isAuthenticated()) {
        authService.getPncUser();
      }

      Object.keys(onBootNotifications).forEach(function (key) {
        onBootNotifications[key].forEach(function (notification) {
          if(angular.isString(notification)) {
            pncNotify[key](notification);
          } else if (angular.isObject(notification)) {
            pncNotify[key](notification.message, notification.actionTitle, notification.actionCallback, notification.menuActions, notification.persistent);
          }
        });
      });

      messageBus.connect();
  }]);

})();
