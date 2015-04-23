'use strict';

(function() {

  var module = angular.module('pnc.configuration-set', [
    'ui.router',
    'xeditable',
    'pnc.remote.restClient',
    'pnc.util.header',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('configuration-set', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'configuration-set.list',
      }
    });

    $stateProvider.state('configuration-set.list', {
      url: '/configuration-set',
      templateUrl: 'configuration-set/views/configuration-set.list.html',
      data: {
        displayName: 'BuildConfiguration Sets'
      },
      controller: 'ConfigurationSetListController',
      controllerAs: 'setlistCtrl',
      resolve: {
        restClient: 'PncRestClient',
        configurationSetList: function(restClient) {
          return restClient.ConfigurationSet.query().$promise;
        }
      }
    });

    $stateProvider.state('configuration-set.detail', {
      url: '/configuration-set/{configurationSetId:int}',
      templateUrl: 'configuration-set/views/configuration-set.detail.html',
      data: {
        displayName: '{{ configurationSetDetail.name }}',
      },
      controller: 'ConfigurationSetDetailController',
      controllerAs: 'detailSetCtrl',
      resolve: {
        restClient: 'PncRestClient',
        configurationSetDetail: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.get({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        configurations: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.getConfigurations({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        records: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.getRecords({
            configurationSetId: $stateParams.configurationSetId}).$promise;
        },
        previousState: ['$state', function ($state) {
          var currentStateData = {
            Name: $state.current.name,
            Params: $state.params,
            URL: $state.href($state.current.name, $state.params)
          };
          return currentStateData;
        }],
      }
    });

    $stateProvider.state('configuration-set.create', {
      url: '/configuration-set/create',
      templateUrl: 'configuration-set/views/configuration-set.create.html',
      data: {
        displayName: 'Create Build Configuration Set'
      },
      controller: 'ConfigurationSetCreateController',
      controllerAs: 'createSetCtrl',
      resolve: {
        restClient: 'PncRestClient',
        products: function(restClient) {
          return restClient.Product.query().$promise;
        },
      },
    });

    $stateProvider.state('configuration-set.add-configuration', {
      url: '/configuration-set/{configurationSetId:int}/add-configuration',
      templateUrl: 'configuration-set/views/configuration-set.add.configuration.html',
      data: {
        displayName: 'Add Build Configuration'
      },
      controller: 'ConfigurationSetAddConfigurationController',
      controllerAs: 'addConfigurationSetCtrl',
      resolve: {
        restClient: 'PncRestClient',
        configurationSetDetail: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.get({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        projects: function(restClient) {
          return restClient.Project.query().$promise;
        },
      },
    });

  }]);

})();
