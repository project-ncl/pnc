'use strict';

(function () {

  var module = angular.module('pnc.release', [
    'ui.router',
    'ui.bootstrap',
    'pnc.product',
    'pnc.remote.restClient',
    'pnc.util.header',
    'angularUtils.directives.uiBreadcrumbs'
  ]);


  module.config(['$stateProvider', function ($stateProvider) {
    $stateProvider.state('product.version.release', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'product.version.release.create'
      }
    });


    $stateProvider.state('product.version.release.create', {
      url: '/release/create',
      templateUrl: 'release/views/release.create.html',
      data: {
        displayName: 'Create Release'
      },
      controller: 'ReleaseCreateController',
      controllerAs: 'releaseCreateCtrl'
    });

  }]);

})();
