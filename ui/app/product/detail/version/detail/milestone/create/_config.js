'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.milestone.create', {
      url: '/create',
      templateUrl: 'product/detail/version/detail/milestone/create/milestone.create-update.html',
      data: {
        displayName: 'Create Milestone'
      },
      controller: 'MilestoneCreateUpdateController',
      controllerAs: 'milestoneCreateUpdateCtrl',
      resolve: {
        milestoneDetail: function() {
          return null;
        }
      }
    });

  }]);

})();
