'use strict';

(function () {

  var module = angular.module('pnc.milestone', [
    'ui.router',
    'ui.bootstrap',
    'pnc.product',
    'pnc.remote.restClient',
    'pnc.util.header',
    'pnc.util.date_utils',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider
    .state('product.version.milestone', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
    })
    .state('product.version.milestone.create', {
      url: '/milestone/create',
      templateUrl: 'milestone/views/milestone.create-update.html',
      data: {
        displayName: 'Create Milestone'
      },
      controller: 'MilestoneCreateUpdateController',
      controllerAs: 'milestoneCreateUpdateCtrl',
      resolve: {
        restClient: 'PncRestClient',
        milestoneDetail: function() {
          return null;
        },
      },
    })
    .state('product.version.milestone.update', {
      url: '/milestone/{milestoneId:int}/update',
      templateUrl: 'milestone/views/milestone.create-update.html',
      data: {
        displayName: 'Update Milestone'
      },
      controller: 'MilestoneCreateUpdateController',
      controllerAs: 'milestoneCreateUpdateCtrl',
      resolve: {
        restClient: 'PncRestClient',
        milestoneDetail: function (restClient, $stateParams) {
          return restClient.Milestone.get({milestoneId: $stateParams.milestoneId})
            .$promise;
        }
      }
    })
    .state('product.version.milestone.close', {
      url: '/milestone/{milestoneId:int}/close',
      templateUrl: 'milestone/views/milestone.close.html',
      data: {
        displayName: 'Close Milestone'
      },
      controller: 'MilestoneCloseController',
      controllerAs: 'milestoneCloseCtrl',
      resolve: {
        restClient: 'PncRestClient',
        milestoneDetail: function (restClient, $stateParams) {
          return restClient.Milestone.get({milestoneId: $stateParams.milestoneId})
            .$promise;
        }
      }
    });

  }]);

})();
