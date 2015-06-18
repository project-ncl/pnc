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
  var app = angular.module('pnc');


  app.provider('keycloak', function() {
    var keycloak;

    return {
      setKeycloak: function(kc) {
        keycloak = kc;
      },

      useMockKeycloak: function() {
        keycloak = newMockKeycloak();
      },

      $get: function() {
        return keycloak;
      }
    };
  });

  app.factory('authService', [
    '$window',
    'keycloak',
    function($window, kc) {
      var keycloak = kc;

      return {
        isAuthenticated: function() {
          return keycloak.authenticated;
        },

        getPrinciple: function() {
          return keycloak.idTokenParsed.preferred_username; // jshint ignore:line
        },

        logout: function() {
          keycloak.logout({ redirectUri: $window.location.href });
        }
      };
    }
    ]);

  app.factory('httpAuthenticationInterceptor', [
    '$q',
    '$log',
    'keycloak',
    function ($q, $log, keycloak) {

      function addAuthHeaders(config, token) {
        config.headers = config.headers || {};
        config.headers.Authorization = 'Bearer ' + token;
      }

      return {

        request: function (config) {
          if (keycloak && keycloak.token) {

            // Prevents screen flicker by directly returning the config
            // object if the keycloak token does not need to be refreshed.
            if (!keycloak.isTokenExpired(5)) {

              addAuthHeaders(config, keycloak.token);
              return config;

            } else {

              var deferred = $q.defer();

              keycloak.updateToken(0).success(function () {
                addAuthHeaders(config, keycloak.token);
                deferred.resolve(config);
              }).error(function () {
                deferred.reject('Failed to refresh token');
              });

              return deferred.promise;
            }

            return config;

          }
        }

      };
    }
  ]);

  app.factory('httpResponseInterceptor', [
    '$log',
    '$q',
    'Notifications',
    'keycloak',
    function($log, $q, Notifications, keycloak) {
      return {

        response: function(response) {
          if (response.config.method !== 'GET') {
            $log.debug('HTTP response: %O', response);
            Notifications.success(response.status + ': ' + response.statusText);
          }
          return response;
        },

        responseError: function(rejection) {
          switch(rejection.status) {
            case 0:
              Notifications.error('Unable to connect to server');
              break;
            case 401:
              keycloak.login();
              break;
            default:
              $log.debug('HTTP response: %O', rejection);
              Notifications.error(rejection.status + ': ' +
                                  rejection.statusText);
              break;
          }
          return rejection;
        }

      };
    }
  ]);

  function newMockKeycloak() {

    function nullFunction() {
      return null;
    }

    return {

      authenticated: false,
      logout: nullFunction,
      login: nullFunction,
      token: 'token',

      isTokenExpired: function() {
        return false;
      },

      idTokenParsed: {
          preferred_username: 'Authentication Disabled' // jshint ignore:line
      },

    };
  }

})();
