'use strict';

(function () {

  var module = angular.module('pnc.milestone', [
    'ui.router',
    'ui.bootstrap',
    'pnc.product',
    'pnc.remote.restClient',
    'pnc.util.header',
    'angularUtils.directives.uiBreadcrumbs'
  ]);


  module.config(['$stateProvider', function ($stateProvider) {
    $stateProvider.state('product.version.milestone', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'product.version.milestone.create'
      }
    });


    $stateProvider.state('product.version.milestone.create', {
      url: '/milestone/create',
      templateUrl: 'milestone/views/milestone.create.html',
      data: {
        displayName: 'Create Milestone'
      },
      controller: 'MilestoneCreateController',
      controllerAs: 'ctrl'
    });

  }]);

})();
