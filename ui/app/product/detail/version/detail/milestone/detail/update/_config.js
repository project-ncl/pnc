'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.milestone.detail.update', {
      url: '/update',
      templateUrl: 'product/detail/version/detail/milestone/create/milestone.create-update.html',
      data: {
        displayName: 'Update Milestone {{ milestoneDetail.version }}'
      },
      controller: 'MilestoneCreateUpdateController',
      controllerAs: 'milestoneCreateUpdateCtrl'
    });

  }]);

})();
