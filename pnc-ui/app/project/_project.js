'use strict';

(function() {

  var module = angular.module('pnc.project', [
    'ui.router',
    'pnc.remote.restClient',
    'pnc.util.header', 
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('project', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'project.list'
      }
    });

   $stateProvider.state('project.list', {
      url: '/project',
      templateUrl: 'project/views/project.list.html',
      data: {
        displayName: 'Projects'
      },
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
    data: {
       displayName: '{{ projectDetail.name }}',
    },
    controller: 'ProjectDetailController',
    controllerAs: 'detailCtrl',
    resolve: {
      restClient: 'PncRestClient',
      projectDetail: function(restClient, $stateParams) {
        return restClient.Project.get({
          projectId: $stateParams.projectId}).$promise;
      },
      projectConfigurationList: function(restClient, $stateParams) {
        return restClient.Configuration.getAllForProject({
          projectId: $stateParams.projectId}).$promise;
      },
    }
  });

  }]);

})();
