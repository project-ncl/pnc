/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function () {

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
        }
      });


      $stateProvider.state('configuration-set-record.list', {
        url: '',
        templateUrl: 'configuration-set-record/views/record.list.html',
        data: {
          displayName: 'Build Groups'
        },
        controller: 'CsRecordListController',
        controllerAs: 'ctrl'
      });


      $urlRouterProvider.when('/configuration-set-record/:recordId', '/configuration-set-record/:recordId/info');


      $stateProvider.state('configuration-set-record.detail', {
        url: '/{recordId:int}',
        templateUrl: 'configuration-set-record/views/record.detail.html',
        data: {
          displayName: '# {{ csRecordDetail.id }} \u2014 {{ csRecordDetail.configurationSet.name }} '
        },
        controller: 'CsRecordDetailController',
        controllerAs: 'ctrl',
        resolve: {
          // we cannot use csRecord.getConfigurationSet() from BuildConfigurationSetRecordDAO
          // we have to load it immediately for displayName to work
          csRecordDetail: function (BuildConfigurationSetRecordDAO, BuildConfigurationSetDAO, $stateParams) {
            return BuildConfigurationSetRecordDAO.get({recordId: $stateParams.recordId}).$promise
              .then(function (csRecord) {
                return BuildConfigurationSetDAO.get({configurationSetId: csRecord.buildConfigurationSetId}).$promise
                  .then(function (configurationSet) {
                    csRecord.configurationSet = configurationSet;
                    return csRecord;
                  });
              });
          },
          // only records that belong to the current csRecord
          records: function ($q, csRecordDetail, BuildRecordDAO) {
            return BuildRecordDAO.query().then(function (r) {
              return _(r).where({buildConfigSetRecordId: csRecordDetail.id});
            });
          }
        }
      });


      $stateProvider.state('configuration-set-record.detail.info', {
        url: '/info',
        templateUrl: 'configuration-set-record/views/record.detail.info.html',
        data: {
          displayName: 'Build Group Info'
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
          displayName: 'Build Group Result'
        },
        resolve: {
          // load log for each record
          recordsLog: function ($q, BuildRecordDAO, records) {
            var promises = _(records).map(function (record) {
              return BuildRecordDAO.getLog({recordId: record.id}).$promise
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
          displayName: 'Build Group Output'
        },
        resolve: {
          // load artifacts for each record
          recordsArtifacts: function ($q, BuildRecordDAO, records) {
            var promises = _(records).map(function (record) {
              return BuildRecordDAO.getArtifacts({recordId: record.id})
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
