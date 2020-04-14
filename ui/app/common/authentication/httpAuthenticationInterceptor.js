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
(function() {
  'use strict';

  angular.module('pnc.common.authentication').factory('httpAuthenticationInterceptor', [
    '$q',
    '$log',
    '$injector',
    'keycloak',
    function ($q, $log, $injector, keycloak) {
      var interceptor = {};


      function addAuthHeaders(config, token) {
        config.headers = config.headers || {};
        config.headers.Authorization = 'Bearer ' + token;
      }

      interceptor.request = function (config) {
        var authService;

        if (keycloak) {
          authService = $injector.get('authService');
          /*
           * Verify that the token validity will not outlive the max SSO session time,
           * Force logout of user if they do not have minimum SSO session time left.
           */
          if (keycloak.authenticated && !authService.verifySsoTokenLifespan()) {
            $log.info('SSO token lifespace below threshold, terminating session');
            authService.logoutAsync();
            keycloak.clearToken();
          }

          if (keycloak.token) {
            // Prevents screen flicker by directly returning the config
            // object if the keycloak token does not need to be refreshed.
            if (!keycloak.isTokenExpired(3600 * 23)) { //token should be valid at least 23h

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
        }
        
        return config;
      };

      return interceptor;
    }
  ]);

})();
