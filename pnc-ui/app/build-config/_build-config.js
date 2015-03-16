'use strict';

(function() {

  var module = angular.module('pnc.BuildConfig', [
    'ui.router',
    'ngResource',
    'xeditable',
    'patternfly.notification',
    'pnc.util.confirmClick',
    'pnc.remote.restClient'
  ]);

  module.run(function(editableOptions) {
    editableOptions.theme = 'default';
  });

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('build-config', {
      abstract: true,
      url: '/build-config',
      views: {
        'content@': {
          templateUrl: 'build-config/views/build-config.html',
          controller: 'BuildConfigController'
        }
      }
    });

    $stateProvider.state('build-config.record', {
      abstract: true,
      url: '/record',
      views: {
        'content@': {
          template: '<div ui-view></div>'
        }
      }
    });

    $stateProvider.state('build-config.record.detail', {
      url: '/{recordId:int}',
      templateUrl: 'build-config/views/record.detail.html',
      controller: 'RecordDetailController',
      controllerAs: 'recordCtrl',
      resolve: {
        restClient: 'PncRestClient',
        recordDetails: function(restClient, $stateParams) {
          return restClient.Record.get({
            recordId: $stateParams.recordId }).$promise;
        },
        configurationDetails: function(restClient, recordDetails) {
          return restClient.Configuration.get({
            configurationId: recordDetails.buildConfigurationId }).$promise;
        },
        projectDetails: function(restClient, configurationDetails) {
          return restClient.Project.get({
            projectId: configurationDetails.projectId }).$promise;
        }
      },
    });

    $stateProvider.state('build-config.record.detail.info', {
      url: '/info',
      templateUrl: 'build-config/views/record.detail.info.html',
      controller: 'RecordInfoController',
      controllerAs: 'infoCtrl',
    });

    $stateProvider.state('build-config.record.detail.result', {
      url: '/result',
      controller: 'RecordResultController',
      controllerAs: 'resultCtrl',
      templateUrl: 'build-config/views/record.detail.result.html',
      resolve: {
        restClient: 'PncRestClient',
        buildLog: function(restClient, recordDetails) {
          return restClient.Record.getLog({
            recordId: recordDetails.id}).$promise;
        }
      }
    });

    $stateProvider.state('build-config.record.detail.output', {
      url: '/output',
      controller: 'RecordOutputController',
      controllerAs: 'outputCtrl',
      templateUrl: 'build-config/views/record.detail.output.html'
    });

    $stateProvider.state('build-config.product', {
      url: '/product',
      resolve: {
        restClient: 'PncRestClient',
        productList: function(restClient) {
          return restClient.Product.query().$promise;
        }
      },
      views: {
        'content@build-config': {
          templateUrl: 'build-config/views/product.html',
          controller: 'ProductListController'
        }
      }
    });

    $stateProvider.state('build-config.configuration', {
      url: '/configuration',
      resolve: {
        restClient: 'PncRestClient',
        configurationList: function(restClient) {
          return restClient.Configuration.query().$promise;
        }
      },
      views: {
        'content@': {
          templateUrl: 'build-config/views/configuration.html',
          controller: 'ConfigurationListController'
        }
      }
    });

    $stateProvider.state('build-config.configuration.show', {
      url: '/{configurationId:int}',
      resolve: {
        restClient: 'PncRestClient',
        configurationDetails: function(restClient, $stateParams) {
          return restClient.Configuration.get({
            configurationId: $stateParams.configurationId }).$promise;
        },
        environmentDetails: function(restClient, $stateParams,
                                     configurationDetails) {
          return restClient.Environment.get({
            environmentId: configurationDetails.environmentId  }).$promise;
        },
        projectDetails: function(restClient, $stateParams,
                                 configurationDetails) {
          return restClient.Project.get({
            projectId: configurationDetails.projectId }).$promise;
        },
        buildRecords: function(restClient, $stateParams) {
          return restClient.Record.getAllForConfiguration({
            configurationId: $stateParams.configurationId }).$promise;
        },
      },
      views: {
        'content@': {
          templateUrl: 'build-config/views/configuration.show.html',
          controller: 'ConfigurationShowController'
        }
      }
    });

    $stateProvider.state('build-config.configuration.create', {
      url: '/create',
      resolve: {
        restClient: 'PncRestClient',
        environments: function(restClient) {
          return restClient.Environment.query().$promise;
        },
        projects: function(restClient) {
          return restClient.Project.query().$promise;
        }
      },
      views: {
        'content@': {
          templateUrl: 'build-config/views/configuration.create.html',
          controller: 'ConfigurationCreateController'
        }
      }
    });

    $stateProvider.state('build-config.product.show', {
      url: '/{productId:int}',
      resolve: {
        restClient: 'PncRestClient',
        productDetails: function(restClient, $stateParams) {
          return restClient.Product.get({ productId: $stateParams.productId })
          .$promise;
        }
      },
      views: {
        'content@build-config': {
          templateUrl: 'build-config/views/product.show.html',
          controller: 'ProductShowController'
        }
      }
    });

    $stateProvider.state('build-config.product.show.version', {
      url: '/version',
      abstract: true
    });

    $stateProvider.state('build-config.product.show.version.show', {
      url: '/{versionId:int}',
      resolve: {
        restClient: 'PncRestClient',
        versionDetails: function(restClient, $stateParams) {
          return restClient.Version.get({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId
          }).$promise;
        }
      },
      views: {
        'content@build-config': {
          templateUrl: 'build-config/views/version.show.html',
          controller: 'VersionShowController'
        }
      }
    });

    $stateProvider.state('build-config.product.show.version.show.project', {
      url: '/project',
      abstract: true
    });

    $stateProvider.state(
      'build-config.product.show.version.show.project.show', {
        url: '/{projectId:int}',
        resolve: {
          restClient: 'PncRestClient',
          projectDetails: function(restClient, $stateParams) {
            return restClient.Project.get({
              projectId: $stateParams.projectId
            }).$promise;
          }
        },
        views:{
          'content@build-config': {
            templateUrl: 'build-config/views/project.show.html',
            controller: 'ProjectShowController'
          }
        }
      });

    $stateProvider.state(
      'build-config.product.show.version.show.project.show.configuration', {
        url: '/configuration',
        abstract: true
      });

    $stateProvider.state(
      'build-config.product.show.version.show.project.show.configuration.show', {
        url: '/{configurationId:int}',
        resolve: {
          restClient: 'PncRestClient',
          configurationDetails: function(restClient, $stateParams) {
            return restClient.Configuration.get({
              configurationId: $stateParams.configurationId }).$promise;
          },
          environmentDetails: function(restClient, $stateParams,
                                       configurationDetails) {
            return restClient.Environment.get({
              environmentId: configurationDetails.environmentId  }).$promise;
          },
          buildRecords: function(restClient, $stateParams) {
            return restClient.Record.getAllForConfiguration({
              configurationId: $stateParams.configurationId }).$promise;
          },
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/views/configuration.show.html',
            controller: 'ConfigurationShowController'
          }
        }
      });

    $stateProvider.state('build-config.product.show.version.show.configurationSet', {
      url: '/configuration-set',
      abstract: true
    });

    $stateProvider.state(
      'build-config.product.show.version.show.configurationSet.show', {
        url: '/{configurationSetId:int}',
        resolve: {
          restClient: 'PncRestClient',
          configurationSetDetails: function(restClient, $stateParams) {
            return restClient.ConfigurationSet.get({
              configurationSetId: $stateParams.configurationSetId
            }).$promise;
          }
        },
        views:{
          'content@build-config': {
            templateUrl: 'build-config/views/configuration-set.show.html',
            controller: 'ConfigurationSetShowController'
          }
        }
      });

    $stateProvider.state(
      'build-config.product.show.version.show.configurationSet.show.configuration', {
        url: '/configuration',
        abstract: true
      });

    $stateProvider.state(
      'build-config.product.show.version.show.configurationSet.show.configuration.show', {
        url: '/{configurationId:int}',
        resolve: {
          restClient: 'PncRestClient',
          configurationDetails: function(restClient, $stateParams) {
            return restClient.Configuration.get({
              configurationId: $stateParams.configurationId }).$promise;
          },
          environmentDetails: function(restClient, $stateParams,
                                       configurationDetails) {
            return restClient.Environment.get({
              environmentId: configurationDetails.environmentId  }).$promise;
          },
          projectDetails: function(restClient, $stateParams,
                                       configurationDetails) {
            return restClient.Project.get({
              projectId: configurationDetails.projectId }).$promise;
          },
          buildRecords: function(restClient, $stateParams) {
            return restClient.Record.getAllForConfiguration({
              configurationId: $stateParams.configurationId }).$promise;
          },
        },
        views: {
          'content@build-config': {
            templateUrl: 'build-config/views/configuration.show.html',
            controller: 'ConfigurationShowController'
          }
        }
      });
  }]);

})();
