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

  app.factory('authService', [
    '$log',
    '$window',
    '$q',
    'keycloak',
    'UserDAO',
    function($log, $window, $q, keycloak, UserDAO) {

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
          return UserDAO.getAuthenticatedUser();
        },

        forUserId: function(userId) {
          var user = UserDAO.getAuthenticatedUser().$promise;
          var deferred = $q.defer();
          user.then(function(pncUser) {
            if (pncUser.id === userId) {
              deferred.resolve();
            } else {
              deferred.reject('userId: ' + pncUser.id + ' didn\'t match: ' + userId);
            }
          }, function(error) {
            deferred.reject(error);
          });

          return deferred.promise;
        },

        logout: function(redirectUri) {
          var redirectTo = redirectUri || $window.location.href;
          $log.info('Logout requested with post-logout redirect to: ' + redirectTo);
          keycloak.logout(redirectTo);
        },

        login: function(redirectUri) {
          var redirectTo = redirectUri || $window.location.href;
          $log.debug('RedirectUri=' + keycloak.createLoginUrl({ redirectUri: redirectTo }) );
          $log.info('Login requested with post-login redirect to: ' + redirectTo);
          keycloak.login(redirectTo);
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
                $log.warn('Failed to refresh authentication token');
                keycloak.clearToken();
                deferred.resolve(config);
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
         if(response.data.content && !_.isArray(response.data.content)) {
            response.data = response.data.content;
         }
         return response;
       }
     };
   });

  app.factory('httpResponseInterceptor', [
    '$q',
    '$log',
    'Notifications',
    'keycloak',
    function($q, $log, Notifications, keycloak) {

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
          Notifications.error('PNC REST Api returned an error in an invalid format: ' + rejection.status + ' ' + rejection.statusText);
          $log.error('PNC REST Api returned an error in an invalid format: response: %O', rejection);
          return rejection;
        }

        if (error.errorMessage.length > MAX_NOTIFICATION_LENGTH) {
          error.errorMessage = error.errorMessage.substring(0, MAX_NOTIFICATION_LENGTH -1) + ' ...';
        }

        Notifications.error(error.errorMessage);
        $log.error('PNC REST API returned the following error: type: "%s", message: "%s", details: "%s"',
            error.errorType, error.errorMessage, error.details);

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
            case 404:
              Notifications.error('Requested resource not found');
              break;
            default:
              handleError(rejection);
              break;
          }
          return $q.reject(rejection);
        }

      };
    }
  ]);

})();
