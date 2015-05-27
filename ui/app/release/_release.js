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
    $stateProvider
    .state('product.version.release', {
      abstract: true,
      url: '/release',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'product.version.release.create'
      }
    })
    .state('product.version.release.create', {
      url: '/create',
      templateUrl: 'release/views/release.create-update.html',
      data: {
        displayName: 'Create Release'
      },
      controller: 'ReleaseCreateUpdateController',
      controllerAs: 'releaseCreateUpdateCtrl',
      resolve: {
        restClient: 'PncRestClient',
        releaseDetail: function() {
          return null;
        },
      },
    })
    .state('product.version.release.update', {
      url: '/{releaseId:int}/update',
      templateUrl: 'release/views/release.create-update.html',
      data: {
        displayName: 'Update Release'
      },
      controller: 'ReleaseCreateUpdateController',
      controllerAs: 'releaseCreateUpdateCtrl',
      resolve: {
        restClient: 'PncRestClient',
        releaseDetail: function(restClient, $stateParams) {
          return restClient.Release.get({ releaseId: $stateParams.releaseId })
          .$promise;
        },
      },
    });

  }]);

})();
