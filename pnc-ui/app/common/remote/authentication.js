'use strict';

(function() {

  /**
   * @ngdoc overview
   * @name pnc.remote.authentication
   * @description
   * Module for handling keycloak authentication to PNC REST API.
   *
   * @author Pavel Slegr
   * @author Alex Creasy
   */
  var module = angular.module('pnc.remote.authentication', []);

  module.config(function($httpProvider) {
    $httpProvider.responseInterceptors.push('errorInterceptor');
    $httpProvider.interceptors.push('authInterceptor');
  });

  module.service('AuthService', function($log, $window) {
    var redirectUrlSuffix =
    '/realms/PNC.REDHAT.COM/tokens/logout?redirect_uri=/pnc-web/index.html';
    var loggedIn = false;
    var keycloakAuth = null;

    this.logout = function() {
      $log.debug('Begin logout');
      loggedIn = false;
      keycloakAuth = null;
      $window.location = keycloakAuth.authServerUrl + redirectUrlSuffix;
    };

    this.login = function(configFileUrl, loginRequired) {
      $log.debug('Begin login');
      var authMode = loginRequired ? 'login-required' : 'check-sso';

      keycloakAuth = new Keycloak(configFileUrl);

      keycloakAuth.init({ onLoad: authMode })
        .success(function () {
          loggedIn = true;
          // angular.bootstrap(document, ['pnc']);
        }).error(function () {
          $window.location.reload();
        }
      );
    };

    this.getKeyCloak = function() {
      return keycloakAuth;
    };
  });

  module.factory('authInterceptor', function($q, AuthService) {
    var authz = AuthService.getKeyCloak();

    return {
      request: function (config) {
        var deferred = $q.defer();
        if (authz.token) {
          authz.updateToken(5).success(function() {
            config.headers = config.headers || {};
            config.headers.Authorization = 'Bearer ' + authz.token;

            deferred.resolve(config);
          }).error(function() {
            deferred.reject('Failed to refresh token');
          });
        }
        return deferred.promise;
      }
    };
  });

  module.factory('errorInterceptor', function($q, Notifications, AuthService) {
    return function(promise) {
      return promise.then(function(response) {
        return response;
      }, function(response) {
        if (response.status === 401) {
          console.log('session timeout?');
          AuthService.logout();
        } else if (response.status === 403) {
          Notifications.error('Forbidden');
        } else if (response.status === 404) {
          Notifications.error('Not found');
        } else if (response.status) {
          if (response.data && response.data.errorMessage) {
            Notifications.error(response.data.errorMessage);
          } else {
            Notifications.error('An unexpected server error has occurred');
          }
        }
        return $q.reject(response);
      });
    };
  });

})();
