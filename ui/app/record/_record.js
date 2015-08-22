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
            //templateUrl: 'common/templates/single-col-center.tmpl.html'
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
          BuildConfiguration: 'BuildConfiguration',
          build: 'Build',
          recordDetail: function (build, $stateParams) {
            return build.get({
              recordId: $stateParams.recordId
            });
          },
          configurationDetail: function (BuildConfiguration, recordDetail) {
            return BuildConfiguration.get({
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
        resolve: {
          restClient: 'PncRestClient',
          buildLog: function (restClient, recordDetail) {
            return restClient.Record.getLog({
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
        resolve: {
          restClient: 'PncRestClient',
          artifacts: function (restClient, recordDetail) {
            return restClient.Record.getArtifacts({
              recordId: recordDetail.id
            }).$promise;
          }
        }
      });

      $stateProvider.state('record.list', {
        url: '',
        templateUrl: 'record/views/record.list.html',
        data: {
          displayName: 'Records'
        },
        controller: 'RecordListController',
        controllerAs: 'ctrl',
        resolve: {
          restClient: 'PncRestClient'
        }
      });

    }]);

})();
