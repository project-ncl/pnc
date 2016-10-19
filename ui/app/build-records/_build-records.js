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
(function () {
  'use strict';

  var module = angular.module('pnc.build-records', [
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

      // NCL-2402 changed the module base URL, this redirect should
      // be removed at some point in the future.
      $urlRouterProvider.when(/^\/record\/?.*/, function ($location) {
        return $location.url().replace('/record', '/build-records');
      });

      $stateProvider.state('build-records', {
        abstract: true,
        url: '/build-records',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'build-records.list',
        }
      });

      $stateProvider.state('build-records.detail', {
        abstract: true,
        url: '/{recordId:int}',
        templateUrl: 'build-records/views/build-records.detail.html',
        data: {
          proxy: 'build-records.detail.default'
        },
        controller: 'RecordDetailController',
        controllerAs: 'recordCtrl',
        resolve: {
          recordDetail: function (BuildRecord, $stateParams) {
            return BuildRecord.get({ id: $stateParams.recordId }).$promise;
          }
        }
      });

      $stateProvider.state('build-records.detail.default', {
        url: '',
        templateUrl: 'build-records/views/build-records.detail.default.html',
        data: {
          displayName: 'Job #{{ recordDetail.id }}',
        }
      });

      $stateProvider.state('build-records.detail.result', {
        url: '/result',
        controller: 'RecordResultController',
        controllerAs: 'resultCtrl',
        templateUrl: 'build-records/views/build-records.detail.result.html',
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

      $stateProvider.state('build-records.detail.artifacts', {
        url: '/artifacts',
        controller: 'RecordArtifactsController',
        controllerAs: 'artifactsCtrl',
        templateUrl: 'build-records/views/build-records.detail.artifacts.html',
        data: {
          displayName: 'Built Artifacts',
        },
        resolve: {
          artifacts: function (recordDetail) {
            return recordDetail.$getBuiltArtifacts();
          }
        }
      });

      $stateProvider.state('build-records.detail.dependencies', {
          url: '/dependencies',
          controller: 'RecordArtifactsController',
          controllerAs: 'artifactsCtrl',
          templateUrl: 'build-records/views/build-records.detail.artifacts.html',
          data: {
            displayName: 'Dependencies',
          },
          resolve: {
            artifacts: function (recordDetail) {
              return recordDetail.$getDependencies();
            }
          }
        });

      $stateProvider.state('build-records.list', {
        url: '',
        templateUrl: 'build-records/views/build-records.list.html',
        data: {
          displayName: 'Builds'
        },
        controller: 'RecordListController',
        controllerAs: 'ctrl'
      });

    }]);

})();
