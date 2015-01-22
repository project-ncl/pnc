'use strict';

(function() {

  angular.module('pnc.BuildConfig', ['ngRoute'])  
    .config(['$routeProvider', function($routeProvider) {

      $routeProvider
        .when('/build-config', {
          templateUrl: 'scripts/build-config/build-config.html',
          controller: 'BuildConfigCtrl'
        })
        .when('/build-config/products', {
          templateUrl: 'scripts/build-config/build-config-select.html',
          controller: 'ProductCtrl'
        })
        .when('/build-config/products/:productId/version', {
          templateUrl: 'scripts/build-config/build-config-select.html',
          controller: 'VersionCtrl'
        })
    }]);
})();