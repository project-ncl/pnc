'use strict';

(function () {

  var module = angular.module('pnc.release');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.release.create', {
      url: '/create',
      templateUrl: 'product/detail/version/detail/release/create/release.create-update.html',
      data: {
        displayName: 'Create Release'
      },
      controller: 'ReleaseCreateUpdateController',
      controllerAs: 'releaseCreateUpdateCtrl',
      resolve: {
        releaseDetail: function() {
          return null;
        }
      }
    });

  }]);

})();
