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
    'pnc.Dashboard',
    'pnc.remote',
    'pnc.product',
    'pnc.project',
    'pnc.configuration',
    'pnc.record',
    'pnc.configuration-set',
    'pnc.websockets',
    ]);

  /*jshint camelcase: false */
  if (pnc_globals.enableAuth) {
    var auth = {};

    angular.element(document).ready(function () {
      var keycloak = new Keycloak('keycloak.json');
      auth.loggedIn = false;

      keycloak.init({ onLoad: 'login-required' }).success(function () {
        auth.loggedIn = true;
        auth.keycloak = keycloak;
        auth.logout = function() {
          auth.loggedIn = false;
          auth.keycloak = null;
          window.location = keycloak.authServerUrl + '/realms/PNC.REDHAT.COM/tokens/logout?redirect_uri=/pnc-web/index.html';
        };
        angular.bootstrap(document, ['pnc']);
      }).error(function () {
        window.location.reload();
      });

    });

    app.factory('Auth', function () {
      return auth;
    });

    app.factory('authInterceptor', function ($q, $log, Auth) {
      return {
        request: function (config) {
          var deferred = $q.defer();

          if (Auth.keycloak && Auth.keycloak.token) {
            Auth.keycloak.updateToken(5).success(function () {
              config.headers = config.headers || {};
              config.headers.Authorization = 'Bearer ' + Auth.keycloak.token;

              deferred.resolve(config);
            }).error(function () {
              deferred.reject('Failed to refresh token');
            });
          }
          return deferred.promise;
        }
      };
    });

    app.config(function ($httpProvider) {
      $httpProvider.interceptors.push('authInterceptor');
    });
  } else {
    angular.bootstrap(document, ['pnc']);
  }

  app.config(function($stateProvider, $urlRouterProvider, $locationProvider) {
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
  );

  app.run(function($rootScope, $log) {
    // Handle errors with state changes.
    $rootScope.$on('$stateChangeError',
      function(event, toState, toParams, fromState, fromParams, error) {
        $log.debug('Caught $stateChangeError: event=%O, toState=%O, ' +
          'toParams=%O, fromState=%O, fromParams=%O, error=%O',
          event, toState, toParams, fromState, fromParams, error);
      }
    );
  });

})();
