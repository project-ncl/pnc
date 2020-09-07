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

  angular.module('pnc.common.authentication').factory('authService', [
    '$log',
    '$window',
    '$q',
    '$http',
    '$httpParamSerializerJQLike',
    'keycloak',
    'authConfig',
    'UserResource',
    function($log, $window, $q, $http, $httpParamSerializerJQLike, keycloak, authConfig, UserResource) {
      var authService = {};

      var pncUser;

      authService.isAuthenticated = function () {
        return keycloak.authenticated;
      };

      authService.getPrinciple = function () {
        if (!keycloak.authenticated) {
          return null;
        }

        return keycloak.idTokenParsed.preferred_username; // jshint ignore:line
      };

      authService.isCurrentUser = function (user) {
        return authService.getPrinciple() === user.username;
      };

      /**
       * Verifies the minimum lifespan of the refresh token.
       */
      authService.verifySsoTokenLifespan = function () {
        const MIN_REFRESH_TOKEN_EXPIRY_DATE = Date.now() + authConfig.getSsoTokenLifespan();

        // Keycloak timestamp is in seconds, not milliseconds
        return keycloak.authenticated && MIN_REFRESH_TOKEN_EXPIRY_DATE < keycloak.refreshTokenParsed.exp * 1000;
      };

      authService.getPncUser = function () {
        return $q((resolve, reject) => {
          if (!authService.isAuthenticated) {
            return reject('User is not authenticated');
          }

          if (!pncUser) {
            pncUser =  UserResource.getAuthenticatedUser().$promise;
          }

          resolve(pncUser);
        });
      };

      authService.forUserId = function (userId) {
        return authService.getPncUser().then(user => {
          console.log('authService.forUserId -> userId: %s | user: %O', userId, user);
            if (user.id.toString() !== userId) {
              return $q.reject();
            }
        });
      };

      authService.logout = function (redirectUri) {
        pncUser = undefined;
        keycloak.logout({ redirectUri: redirectUri || $window.location.href });
      };

      authService.login = function(redirectUri) {
        keycloak.login({ redirectUri: redirectUri || $window.location.href });
      };

      authService.logoutAsync = function () {
        return $http({
          url: keycloak.tokenParsed.iss + '/protocol/openid-connect/logout',
          method: 'POST',
          data: $httpParamSerializerJQLike({
            'client_id': keycloak.clientId,
            'client_secret': keycloak.clientSecret,
            'refresh_token': keycloak.refreshToken
          }),
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          successNotification: false
        })
        .finally(() => {
          keycloak.clearToken();
          pncUser = undefined;
        });
      };

      authService.getUserRole = function () {
        return keycloak.authenticated ? keycloak.realmAccess.roles : null;
      };

      return authService;
    }
  ]);

})();
