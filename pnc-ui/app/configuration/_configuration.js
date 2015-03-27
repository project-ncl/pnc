'use strict';

(function() {

  var module = angular.module('pnc.configuration', [
    'ui.router',
    'xeditable',
    'pnc.remote.restClient',
    'pnc.util.header',
    'pnc.util.confirmClick',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('configuration', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'configuration.list'
      }
    });

    $stateProvider.state('configuration.list', {
      url: '/configuration',
      templateUrl: 'configuration/views/configuration.list.html',
      data: {
        displayName: 'Build Configurations'
      },
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
      data: {
        displayName: 'Create Build Configuration'
      },
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
      },
      data: {
        proxy: 'configuration.detail.show'
      }
    });

    // Populate main and sidebar views.
    $stateProvider.state('configuration.detail.show', {
      url: '/configuration/{configurationId:int}',
      data: {
         displayName: '{{ configurationDetail.name }}',
      },
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
