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

      //  Temporary redirect due to changed URL. This should be removed at
      // some point.
      $urlRouterProvider.when('/record/:recordId/info', '/record/:recordId');

      $stateProvider.state('record.detail', {
        abstract: true,
        url: '/{recordId:int}',
        templateUrl: 'record/views/record.detail.html',
        data: {
          proxy: 'record.detail.default'
        },
        controller: 'RecordDetailController',
        controllerAs: 'recordCtrl',
        resolve: {
          recordDetail: function (BuildRecord, $stateParams) {
            return BuildRecord.get({ id: $stateParams.recordId }).$promise;
          }
        }
      });

      $stateProvider.state('record.detail.default', {
        url: '',
        templateUrl: 'record/views/record.detail.default.html',
        data: {
          displayName: 'Job #{{ recordDetail.id }}',
        }
      });

      $stateProvider.state('record.detail.result', {
        url: '/result',
        controller: 'RecordResultController',
        controllerAs: 'resultCtrl',
        templateUrl: 'record/views/record.detail.result.html',
        data: {
          displayName: 'Log'
        },
        resolve: {
          buildLog: function (BuildRecord, recordDetail) {
            return BuildRecord.getLog({ id: recordDetail.id }).$promise;
          },
          sshCredentials: function (BuildRecord, recordDetail) {
            return BuildRecord.getSshCredentials({
              recordId: recordDetail.id
            }).$promise;
          }
        }
      });

      $stateProvider.state('record.detail.artifacts', {
        url: '/artifacts',
        controller: 'RecordArtifactsController',
        controllerAs: 'artifactsCtrl',
        templateUrl: 'record/views/record.detail.artifacts.html',
        data: {
          displayName: 'Built Artifacts',
        },
        resolve: {
          artifacts: function (recordDetail) {
            return recordDetail.$getBuiltArtifacts();
          }
        }
      });

      $stateProvider.state('record.detail.dependencies', {
          url: '/dependencies',
          controller: 'RecordArtifactsController',
          controllerAs: 'artifactsCtrl',
          templateUrl: 'record/views/record.detail.artifacts.html',
          data: {
            displayName: 'Dependencies',
          },
          resolve: {
            artifacts: function (recordDetail) {
              return recordDetail.$getDependencies();
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
