'use strict';

(function () {

  var module = angular.module('pnc.release');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.release.detail.update', {
      url: '/update',
      templateUrl: 'product/detail/version/detail/release/create/release.create-update.html',
      data: {
        displayName: 'Update Release {{ releaseDetail.version }}'
      },
      controller: 'ReleaseCreateUpdateController',
      controllerAs: 'releaseCreateUpdateCtrl'
    });

  }]);

})();
