'use strict';

(function() {

  var module = angular.module('pnc.project', [
    'ui.router',
    'pnc.remote.restClient',
    'pnc.util.header'
  ]);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('project', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      }
    });

   $stateProvider.state('project.list', {
      url: '/project',
      templateUrl: 'project/views/project.list.html',
      controller: 'ProjectListController',
      controllerAs: 'listCtrl',
      resolve: {
        restClient: 'PncRestClient',
        projectList: function(restClient) {
          return restClient.Project.query().$promise;
        }
      },
    });

  $stateProvider.state('project.detail', {
    url: '/project/{projectId:int}',
    templateUrl: 'project/views/project.detail.html',
    controller: 'ProjectDetailController',
    controllerAs: 'detailCtrl',
    resolve: {
      restClient: 'PncRestClient',
      projectDetail: function(restClient, $stateParams) {
        return restClient.Project.get({
          projectId: $stateParams.projectId}).$promise;
      },
    }
  });

  }]);

})();
