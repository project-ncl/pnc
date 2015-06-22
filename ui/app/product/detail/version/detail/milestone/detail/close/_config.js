'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.milestone.detail.close', {
      url: '/close',
      templateUrl: 'product/detail/version/detail/milestone/detail/close/milestone.close.html',
      data: {
        displayName: 'Update Milestone {{ milestoneDetail.version }}'
      },
      controller: 'MilestoneCloseController',
      controllerAs: 'milestoneCloseCtrl'
    });

  }]);

})();
