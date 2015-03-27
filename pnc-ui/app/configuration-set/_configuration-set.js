'use strict';

(function() {

  var module = angular.module('pnc.configuration-set', [
    'ui.router',
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
        proxy: 'configuration-set.list'
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
      controllerAs: 'detailCtrl',
      resolve: {
        restClient: 'PncRestClient',
        configurationSetDetail: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.get({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        configurations: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.getConfigurations({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        }
      }
    });
  }]);

})();
