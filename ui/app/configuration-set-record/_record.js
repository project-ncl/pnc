'use strict';

(function () {
  /* global _ */

  var module = angular.module('pnc.configuration-set-record', [
    'ui.router',
    'angularUtils.directives.uiBreadcrumbs',
    'pnc.common.restclient'
  ]);

  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {


      $stateProvider.state('configuration-set-record', {
        abstract: true,
        url: '/configuration-set-record',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'configuration-set-record.list'
        },
        resolve: {
          restClient: 'PncRestClient'
        }
      });


      $stateProvider.state('configuration-set-record.list', {
        url: '',
        templateUrl: 'configuration-set-record/views/record.list.html',
        data: {
          displayName: 'Configuration Set Records'
        },
        controller: 'CsRecordListController',
        controllerAs: 'ctrl'
      });


      $urlRouterProvider.when('/configuration-set-record/:recordId', '/configuration-set-record/:recordId/info');


      $stateProvider.state('configuration-set-record.detail', {
        url: '/{recordId:int}',
        templateUrl: 'configuration-set-record/views/record.detail.html',
        data: {
          displayName: '# {{ csRecordDetail.id }} \u2014 {{ csRecordDetail.configurationSet.name }} ' +
          '\u2014 {{ csRecordDetail.startTime | date:"medium"}}'
        },
        controller: 'CsRecordDetailController',
        controllerAs: 'ctrl',
        resolve: {
          // we cannot use csRecord.getConfigurationSet() from ConfigurationSetRecord
          // we have to load it immediately for displayName to work
          csRecordDetail: function (ConfigurationSetRecord, BuildConfigurationSet, $stateParams) {
            return ConfigurationSetRecord.get({recordId: $stateParams.recordId}).$promise
              .then(function (csRecord) {
                return BuildConfigurationSet.get({configurationSetId: csRecord.buildConfigurationSetId}).$promise
                  .then(function (configurationSet) {
                    csRecord.configurationSet = configurationSet;
                    return csRecord;
                  });
              });
          },
          // only records that belong to the current csRecord
          records: function ($q, csRecordDetail, BuildRecord) {
            return BuildRecord.query().$promise.then(function (r) {
              return _(r).where({buildConfigSetRecordId: csRecordDetail.id});
            });
          },
          // only running records that belong to the current csRecord
          runningRecords: function (csRecordDetail, RunningBuild) {
            return RunningBuild.query().$promise.then(function (r) {
              return _(r).where({buildConfigSetRecordId: csRecordDetail.id});
            });
          }
        }
      });


      $stateProvider.state('configuration-set-record.detail.info', {
        url: '/info',
        templateUrl: 'configuration-set-record/views/record.detail.info.html',
        data: {
          displayName: 'Build Info'
        },
        controller: 'CsRecordInfoController',
        controllerAs: 'ctrl'
      });


      $stateProvider.state('configuration-set-record.detail.result', {
        url: '/result',
        controller: 'CsRecordResultController',
        controllerAs: 'ctrl',
        templateUrl: 'configuration-set-record/views/record.detail.result.html',
        data: {
          displayName: 'Build Result'
        },
        resolve: {
          // load log for each record
          recordsLog: function ($q, BuildRecord, records) {
            var promises = _(records).map(function (record) {
              return BuildRecord.getLog({recordId: record.id}).$promise
                .then(function (log) {
                  var recordCopy = _.clone(record);
                  recordCopy.log = log;
                  return recordCopy;
                });
            });
            return $q.all(promises);
          }
        }
      });


      $stateProvider.state('configuration-set-record.detail.output', {
        url: '/output',
        controller: 'CsRecordOutputController',
        controllerAs: 'ctrl',
        templateUrl: 'configuration-set-record/views/record.detail.output.html',
        data: {
          displayName: 'Build Output'
        },
        resolve: {
          // load artifacts for each record
          recordsArtifacts: function ($q, BuildRecord, records) {
            var promises = _(records).map(function (record) {
              return BuildRecord.getArtifacts({recordId: record.id}).$promise
                .then(function (artifacts) {
                  var recordCopy = _.clone(record);
                  recordCopy.artifacts = artifacts;
                  return recordCopy;
                });
            });
            return $q.all(promises).then(function(result) {
              // skip when not available
              return _(result).filter(function(e) { return e.artifacts.length; });
            });
          }
        }
      });


    }]);

})();
