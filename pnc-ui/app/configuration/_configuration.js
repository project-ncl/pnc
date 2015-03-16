'use strict';

(function() {

  var module = angular.module('pnc.configuration', [
    'ui.router',
    'xeditable',
    'pnc.remote.restClient',
    'pnc.util.header',
    'pnc.util.confirmClick'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('configuration', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: '/common/templates/single-col-center.tmpl.html'
        }
      }
    });

    $stateProvider.state('configuration.list', {
      url: '/configuration',
      templateUrl: 'configuration/views/configuration.list.html',
      controller: 'ConfigurationListController',
      controllerAs: 'listCtrl',
      resolve: {
        restClient: 'PncRestClient',
        configurationList: function(restClient) {
          return restClient.Configuration.query().$promise;
        }
      }
    });

    $stateProvider.state('configuration.create', {
      url: '/configuration/create',
      templateUrl: 'configuration/views/configuration.create.html',
      controller: 'ConfigurationCreateController',
      controllerAs: 'createCtrl',
      resolve: {
        restClient: 'PncRestClient',
        environments: function(restClient) {
          return restClient.Environment.query().$promise;
        },
        projects: function(restClient) {
          return restClient.Project.query().$promise;
        }
      },
    });

    // Sets up a view with a sidebar
    $stateProvider.state('configuration.detail', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/two-col-right-sidebar.tmpl.html'
        }
      }
    });

    // Populate main and sidebar views.
    $stateProvider.state('configuration.detail.show', {
      url: '/configuration/{configurationId:int}',
      views: {
        '': {
          templateUrl: 'configuration/views/configuration.detail-main.html',
          controller: 'ConfigurationDetailController',
          controllerAs: 'detailCtrl'
        },
        'sidebar': {
          templateUrl: 'configuration/views/configuration.detail-sidebar.html',
          controller: 'ConfigurationSidebarController',
          controllerAs: 'sidebarCtrl'
        },
      },
      resolve: {
        restClient: 'PncRestClient',
        configurationDetail: function(restClient, $stateParams) {
          return restClient.Configuration.get({
            configurationId: $stateParams.configurationId }).$promise;
        },
        environmentDetail: function(restClient, $stateParams,
                                     configurationDetail) {
          return restClient.Environment.get({
            environmentId: configurationDetail.environmentId  }).$promise;
        },
        projectDetail: function(restClient, $stateParams,
                                 configurationDetail) {
          return restClient.Project.get({
            projectId: configurationDetail.projectId }).$promise;
        },
        buildRecordList: function(restClient, $stateParams) {
          return restClient.Record.getAllForConfiguration({
            configurationId: $stateParams.configurationId }).$promise;
        }
      }
    });

  }]);

})();
