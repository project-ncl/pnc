'use strict';

(function() {

  var module = angular.module('pnc.BuildConfig', ['ui.router']);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider
      .state('build-config', {
        url: '/build-config',
        templateUrl: 'scripts/build-config/build-config.html',
        controller: 'BuildConfigCtrl'
      });
      // .when('/build-config/products', {
      //   templateUrl: 'scripts/build-config/build-config-select.html',
      //   controller: 'ProductCtrl'
      // })
      // .when('/build-config/products/:productId/version', {
      //   templateUrl: 'scripts/build-config/build-config-select.html',
      //   controller: 'VersionCtrl'
      // })
    }
  ]);
})();


//   module.config(['$stateProvider', function($stateProvider) {
//     $stateProvider.state('dashboard', {
//       url: '',
//       templateUrl: 'scripts/dashboard/dashboard.html',
//       controller: 'DashboardCtrl'
//     });


//   var module = angular.module('pnc.Dashboard', ['ui.router']);

//   module.config(['$stateProvider', function($stateProvider) {
//     $stateProvider.state('dashboard', {
//       url: '',
//       templateUrl: 'scripts/dashboard/dashboard.html',
//       controller: 'DashboardCtrl'
//     });
//   }]);
// })();
