'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.milestone.detail', {
      abstract: true,
      url: '/{milestoneId:int}',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      resolve: {
        milestoneDetail: function (restClient, $stateParams) {
          return restClient.Milestone.get({milestoneId: $stateParams.milestoneId})
            .$promise;
        }
      }
    });

  }]);

})();
