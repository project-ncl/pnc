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
(function () {
  'use strict';

  angular.module('pnc.common.authentication').factory('authService', [
    '$log',
    '$window',
    '$q',
    'keycloak',
    'UserDAO',
    function ($log, $window, $q, keycloak, UserDAO) {
      var authService = {};

      authService.isAuthenticated = function () {
        return keycloak.authenticated;
      };

      authService.getPrinciple = function () {
        if (!keycloak.authenticated) {
          return null;
        }

        return keycloak.idTokenParsed.preferred_username; // jshint ignore:line
      };

      // returns user only if he is authenticated
      authService.getPncUser = function () {
        var deferred = $q.defer();
        if (keycloak.authenticated) {
          return UserDAO._getAuthenticatedUser().$promise;
        } else {
          var msg = 'There is no authenticated user, keycloak.authenticated: ' + keycloak.authenticated;
          $log.info(msg);
          deferred.reject(msg);
        }
        return deferred.promise;
      };

      authService.forUserId = function (userId) {
        var user = authService.getPncUser();
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
      };

      authService.logout = function (redirectUri) {
        var redirectTo = redirectUri || $window.location.href;
        $log.info('Logout requested with post-logout redirect to: ' + redirectTo);
        keycloak.logout(redirectTo);
      };

      authService.login = function(redirectUri) {
        var redirectTo = redirectUri || $window.location.href;
        $log.info('Login requested with post-login redirect to: ' + redirectTo);
        keycloak.login(redirectTo);
      };

      return authService;
    }
  ]);

})();
