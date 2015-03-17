'use strict';

(function() {

  var module = angular.module('pnc.record', [
    'ui.router',
    'pnc.remote.restClient',
    'pnc.util.header'
  ]);

  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function($stateProvider, $urlRouterProvider) {

    $stateProvider.state('record', {
      abstract: true,
      url: '/record',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      }
    });

    $urlRouterProvider.when('/record/:recordId', '/record/:recordId/info');

    $stateProvider.state('record.detail', {
      url: '/{recordId:int}',
      templateUrl: 'record/views/record.detail.html',
      controller: 'RecordDetailController',
      controllerAs: 'recordCtrl',
      resolve: {
        restClient: 'PncRestClient',
        recordDetail: function(restClient, $stateParams) {
          return restClient.Record.get({
            recordId: $stateParams.recordId }).$promise;
        },
        configurationDetail: function(restClient, recordDetail) {
          return restClient.Configuration.get({
            configurationId: recordDetail.buildConfigurationId }).$promise;
        },
        projectDetail: function(restClient, configurationDetail) {
          return restClient.Project.get({
            projectId: configurationDetail.projectId }).$promise;
        }
      },
    });

    $stateProvider.state('record.detail.info', {
      url: '/info',
      templateUrl: 'record/views/record.detail.info.html',
      controller: 'RecordInfoController',
      controllerAs: 'infoCtrl',
    });

    $stateProvider.state('record.detail.result', {
      url: '/result',
      controller: 'RecordResultController',
      controllerAs: 'resultCtrl',
      templateUrl: 'record/views/record.detail.result.html',
      resolve: {
        restClient: 'PncRestClient',
        buildLog: function(restClient, recordDetail) {
          return restClient.Record.getLog({
            recordId: recordDetail.id}).$promise;
        }
      }
    });

    $stateProvider.state('record.detail.output', {
      url: '/output',
      controller: 'RecordOutputController',
      controllerAs: 'outputCtrl',
      templateUrl: 'record/views/record.detail.output.html'
    });

  }]);

})();
