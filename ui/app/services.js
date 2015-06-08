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

  app.factory('authInterceptor', [
    '$q',
    '$log',
    'keycloak',
    function ($q, $log, keycloak) {
      return {
        request: function (config) {
          var deferred = $q.defer();

          if (keycloak && keycloak.token) {
            keycloak.updateToken(5).success(function () {
              config.headers = config.headers || {};
              config.headers.Authorization = 'Bearer ' + keycloak.token;

              deferred.resolve(config);
            }).error(function () {
              deferred.reject('Failed to refresh token');
            });
          }
          return deferred.promise;
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

      idTokenParsed: {
          preferred_username: 'Authentication Disabled' // jshint ignore:line
      },

    };
  }

})();
