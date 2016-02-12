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

  var module = angular.module('pnc.record', [
    'ui.router',
    'angularUtils.directives.uiBreadcrumbs',
    'angular-websocket',
    'pnc.common.events',
    'pnc.common.directives',
    'pnc.common.restclient',
  ]);

  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

      $stateProvider.state('record', {
        abstract: true,
        url: '/record',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'record.list',
        }
      });

      $urlRouterProvider.when('/record/:recordId', '/record/:recordId/info');

      $stateProvider.state('record.detail', {
        abstract: true,
        url: '/{recordId:int}',
        templateUrl: 'record/views/record.detail.html',
        data: {
          displayName: '{{ recordDetail.name }}',
        },
        controller: 'RecordDetailController',
        controllerAs: 'recordCtrl',
        resolve: {
          recordDetail: function (BuildRecordDAO, $stateParams) {
            return BuildRecordDAO.getCompletedOrRunning({
              recordId: $stateParams.recordId
            }).$promise;
          },
          configurationDetail: function (BuildConfigurationDAO, recordDetail) {
            return BuildConfigurationDAO.get({
              configurationId: recordDetail.buildConfigurationId
            }).$promise;
          }
        }
      });

      $stateProvider.state('record.detail.info', {
        url: '/info',
        templateUrl: 'record/views/record.detail.info.html',
        data: {
          displayName: '{{ recordDetail.id }}',
        },
        controller: 'RecordInfoController',
        controllerAs: 'infoCtrl',
      });

      $stateProvider.state('record.detail.result', {
        url: '/result',
        controller: 'RecordResultController',
        controllerAs: 'resultCtrl',
        templateUrl: 'record/views/record.detail.result.html',
        data: {
          displayName: '{{ recordDetail.id }}',
        },
        resolve: {
          buildLog: function (BuildRecordDAO, recordDetail) {
            return BuildRecordDAO.getLog({
              recordId: recordDetail.id
            }).$promise;
          }
        }
      });

      $stateProvider.state('record.detail.output', {
        url: '/output',
        controller: 'RecordOutputController',
        controllerAs: 'outputCtrl',
        templateUrl: 'record/views/record.detail.output.html',
        data: {
          displayName: '{{ recordDetail.id }}',
        },
        resolve: {
          artifacts: function (BuildRecordDAO, recordDetail) {
            return BuildRecordDAO.getArtifacts({
              recordId: recordDetail.id
            });
          }
        }
      });

      $stateProvider.state('record.detail.dependencies', {
          url: '/dependencies',
          controller: 'RecordDependenciesController',
          controllerAs: 'depsCtrl',
          templateUrl: 'record/views/record.detail.dependencies.html',
          data: {
            displayName: '{{ recordDetail.id }}',
          },
          resolve: {
            artifacts: function (BuildRecordDAO, recordDetail) {
              return BuildRecordDAO.getArtifacts({
                recordId: recordDetail.id
              });
            }
          }
        });

      $stateProvider.state('record.list', {
        url: '',
        templateUrl: 'record/views/record.list.html',
        data: {
          displayName: 'Builds'
        },
        controller: 'RecordListController',
        controllerAs: 'ctrl'
      });

    }]);

})();
