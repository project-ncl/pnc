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
    'pnc.websockets'
  ]);

  app.config(['$stateProvider', '$urlRouterProvider', '$locationProvider',
    function($stateProvider, $urlRouterProvider, $locationProvider) {

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
    ]);

    app.run(['$rootScope', '$log',

         // Handle errors with state changes.
         function($rootScope, $log) {
           $rootScope.$on('$stateChangeError',
             function(event, toState, toParams, fromState, fromParams, error) {
               $log.debug('Caught $stateChangeError: event=%O, toState=%O, ' +
                          'toParams=%O, fromState=%O, fromParams=%O, error=%O',
                          event, toState, toParams, fromState, fromParams, error);
             }
           );
         }
       ]);

    var auth = {};
    var logout = function(){
        console.log('*** LOGOUT');
        auth.loggedIn = false;
        auth.authz = null;
        window.location = auth.logoutUrl;
    };
    
    angular.element(document).ready(function ($http) {
        var keycloakAuth = new Keycloak('keycloak.json');
        auth.loggedIn = false;
    
        keycloakAuth.init({ onLoad: 'login-required' }).success(function () {
            auth.loggedIn = true;
            auth.authz = keycloakAuth;
            auth.logoutUrl = keycloakAuth.authServerUrl + '/realms/PNC.REDHAT.COM/tokens/logout?redirect_uri=/pnc-web/index.html';
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
    
    module.factory('errorInterceptor', function($q) {
        return function(promise) {
            return promise.then(function(response) {
                return response;
            }, function(response) {
                if (response.status === 401) {
                    console.log('session timeout?');
                    logout();
                } else if (response.status === 403) {
                    alert('Forbidden');
                } else if (response.status === 404) {
                    alert('Not found');
                } else if (response.status) {
                    if (response.data && response.data.errorMessage) {
                        alert(response.data.errorMessage);
                    } else {
                        alert('An unexpected server error has occurred');
                    }
                }
                return $q.reject(response);
            });
        };
    });
})();
