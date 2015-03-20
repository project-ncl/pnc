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
  var module = angular.module('pnc.remote.authentication', ['Notifications']);

  var auth = {};
  var logout = function(){
    console.log('*** LOGOUT');
    auth.loggedIn = false;
    auth.authz = null;
    window.location = auth.logoutUrl;
  };

  angular.element(document).ready(function () {
    var keycloakAuth = new Keycloak('keycloak.json');
    auth.loggedIn = false;

    keycloakAuth.init({ onLoad: 'login-required' }).success(function () {
      auth.loggedIn = true;
      auth.authz = keycloakAuth;
      auth.logoutUrl = keycloakAuth.authServerUrl +
        '/realms/PNC.REDHAT.COM/tokens/logout?redirect_uri=/pnc-web/index.html';
      module.factory('Auth', function() {
        return auth;
      });
      angular.bootstrap(document, ['pnc']);
    }).error(function () {
      window.location.reload();
    });

  });

  module.factory('authInterceptor', function($q, Auth) {
    return {
      request: function (config) {
        var deferred = $q.defer();
        if (Auth.authz.token) {
          Auth.authz.updateToken(5).success(function() {
            config.headers = config.headers || {};
            config.headers.Authorization = 'Bearer ' + Auth.authz.token;

            deferred.resolve(config);
          }).error(function() {
            deferred.reject('Failed to refresh token');
          });
        }
        return deferred.promise;
      }
    };
  });

  module.config(function($httpProvider) {
    $httpProvider.responseInterceptors.push('errorInterceptor');
    $httpProvider.interceptors.push('authInterceptor');

  });

  module.factory('errorInterceptor', function($q, Notifications) {
    return function(promise) {
      return promise.then(function(response) {
        return response;
      }, function(response) {
        if (response.status === 401) {
          console.log('session timeout?');
          logout();
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
