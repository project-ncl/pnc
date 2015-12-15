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

      $get: ['$log', function($log) {
        $log.debug('keycloak=%O', keycloak);
        return keycloak;
      }]
    };
  });

  app.factory('authService', [
    '$window',
    'keycloak',
    'UserDAO',
    function($window, kc, UserDAO) {
      var keycloak = kc;
      var pncUser = null;

      return {
        isAuthenticated: function() {
          return keycloak.authenticated;
        },

        getPrinciple: function() {
          if (!keycloak.authenticated) {
            return null;
          }

          return keycloak.idTokenParsed.preferred_username; // jshint ignore:line
        },

        getPncUser: function() {
          if (!pncUser) {
            return UserDAO.getAuthenticatedUser().$promise.then(function(result) {
              pncUser = result;
            });
          }
          return pncUser;
        },

        logout: function() {
          keycloak.logout({ redirectUri: $window.location.href });
        },

        login: function() {
          keycloak.login({ redirectUri: $window.location.href });
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
          }
          return config;
        }

      };
    }
  ]);

   app.factory('unwrapPageResponseInterceptor', function() {
     return {
       response: function(response) {
         if(_(response.data).has('content') && !_.isArray(response.data.content)) {
            response.data = response.data.content;
         }
         return response;
       }
     };
   });

  app.factory('httpResponseInterceptor', [
    '$log',
    'Notifications',
    'keycloak',
    function($log, Notifications, keycloak) {

      function defaultSuccessNotification(response) {
        if (response.config.method !== 'GET') {
          $log.debug('HTTP response: %O', response);
          Notifications.success('Request successful');
        }
      }

      function handleError(rejection) {
        var MAX_NOTIFICATION_LENGTH = 120;

        var error;

        if (rejection && rejection.data && rejection.data.errorMessage) {
          error = rejection.data;
        } else {
          $log.error('PNC REST Api returned an error in an invalid format: response: %O', rejection);
          return rejection;
        }

        if (error.errorMessage.length > MAX_NOTIFICATION_LENGTH) {
          error.errorMessage = error.errorMessage.substring(0, MAX_NOTIFICATION_LENGTH -1) + ' ...';
        }

        Notifications.error(error.errorMessage);
        $log.error('PNC REST API returned the following error: type: "%s", message: "%s", details: "%s"',
            error.errorType, error.errorMessage, error.errorDetails);

      }

      return {

        response: function(response) {
          var notify = response.config.successNotification;

          if (angular.isUndefined(notify)) {
            defaultSuccessNotification(response);
          } else if (angular.isFunction(notify)) {
            notify(response);
          } else if (angular.isString(notify)) {
            Notifications.success(notify);
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
            case 409:
              Notifications.error('Build rejected because the same build configuration is already running', rejection);
              break;
            default:
              handleError(rejection);
              break;
          }
          return rejection;
        }

      };
    }
  ]);

})();
