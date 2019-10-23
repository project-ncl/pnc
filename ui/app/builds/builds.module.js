/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.builds', [
    'ui.router',
    'angular-websocket',
    'bifrost',
    'pnc.common.events',
    'pnc.common.directives',
    'pnc.common.restclient',
    'pnc.properties'
  ]);

  module.config([
    '$stateProvider',
    function ($stateProvider) {

      $stateProvider.state('builds', {
        abstract: true,
        url: '/builds',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'builds.list',
        }
      });

      $stateProvider.state('builds.list', {
        url: '',
        data: {
          displayName: 'Builds',
          title: 'Builds'
        },
        component: 'pncBuildsListPage',
        resolve: {
          builds: [
            'BuildResource',
            (BuildResource) => BuildResource.query().$promise
          ]
        }
      });

      $stateProvider.state('builds.detail', {
        abstract: true,
        url: '/{buildId}',
        resolve: {
          build: ['BuildResource', '$stateParams', function (BuildResource, $stateParams) {
            return BuildResource.get({ id: $stateParams.buildId }).$promise;
          }]
        }
      });

      $stateProvider.state('builds.detail.default', {
        url: '',
        onEnter: [
          '$state',
          '$timeout',
          'build',
          function ($state, $timeout, build) {
            $timeout(function () { // Works around bug in ui.router https://github.com/angular-ui/ui-router/issues/1434
              $state.go('projects.detail.build-configs.detail.builds.detail.default', {
                projectId: build.project.id,
                configurationId: build.buildConfigRevision.id,
                buildId: build.id
              });
            });
          }
        ]
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds', {
        abstract: true,
        url: '/builds',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail', {
        abstract: true,
        url: '/{buildId}',
        data: {
          proxy: 'projects.detail.build-configs.detail.builds.detail.default',
          title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Build'
        },
        component: 'pncBuildDetailPage',
        resolve: {
          build: ['BuildResource', '$stateParams', function (BuildResource, $stateParams) {
            return BuildResource.get({ id: $stateParams.buildId }).$promise;
          }],
          buildBrewPushResult: ['BuildResource', '$stateParams', function (BuildResource, $stateParams) {
            return BuildResource.getBrewPushResult({ id: $stateParams.buildId });
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.default', {
        url: '',
        templateUrl: 'builds/detail/details/pnc-build-detail-details-page.html',
        data: {
          displayName: 'Job #{{ recordDetail.id }}',
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.result', {
        url: '/result',
        controller: 'RecordResultController',
        controllerAs: 'resultCtrl',
        templateUrl: 'builds/views/builds.detail.result.html',
        data: {
          displayName: 'Build Log',
          title: '#{{ recordDetail.id }} {{ recordDetail.buildConfigurationName }} | Build Log'
        },
        resolve: {
          buildLog: ['BuildRecord', 'recordDetail', function (BuildRecord, recordDetail) {
            return BuildRecord.getLog({ id: recordDetail.id }).$promise;
          }],
          sshCredentials: ['BuildRecord', 'recordDetail', function (BuildRecord, recordDetail) {
            return BuildRecord.getSshCredentials({
              recordId: recordDetail.id
            });
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.artifacts', {
        url: '/artifacts',
        controller: 'RecordArtifactsController',
        controllerAs: 'artifactsCtrl',
        templateUrl: 'builds/views/builds.detail.artifacts.html',
        data: {
          displayName: 'Build Artifacts',
          title: '#{{ recordDetail.id }} {{ recordDetail.buildConfigurationName }} | Build Artifacts'
        },
        resolve: {
          artifacts: ['recordDetail', function (recordDetail) {
            return recordDetail.$getBuiltArtifacts({ pageSize: 10 });
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.dependencies', {
          url: '/dependencies',
          controller: 'RecordArtifactsController',
          controllerAs: 'artifactsCtrl',
          templateUrl: 'builds/views/builds.detail.artifacts.html',
          data: {
            displayName: 'Dependencies',
            title: '#{{ recordDetail.id }} {{ recordDetail.buildConfigurationName }} | Dependencies'
          },
          resolve: {
            artifacts: ['recordDetail', function (recordDetail) {
              return recordDetail.$getDependencies({ pageSize: 10 });
            }]
          }
        });

      /**
       * naming: alignment log (end user), repour result (internal)
       */
      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.repour-result', {
        url: '/alignment-log',
        controller: 'RecordRepourResultController',
        controllerAs: 'repourResultCtrl',
        templateUrl: 'builds/views/builds.detail.repour-result.html',
        data: {
          displayName: 'Alignment Log',
          title: '#{{ recordDetail.id }} {{ recordDetail.buildConfigurationName }} | Alignment Log'
        },
        resolve: {
          repourLog: ['BuildRecord', 'recordDetail', function (BuildRecord, recordDetail) {
            return BuildRecord.getRepourLog({ id: recordDetail.id }).$promise;
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.brew-push', {
        url: '/brew-push',
        component: 'pncBrewPushTab',
        bindings: {
          buildRecord: 'recordDetail'
        },
        data: {
          displayName: 'Brew Push Results',
          title: '#{{ recordDetail.id }} {{ recordDetail.buildConfigurationName }} | Brew Push'
        },
        resolve: {
          buildRecordPushResult: ['BuildRecord', '$stateParams', function (BuildRecord, $stateParams) {
            return BuildRecord.getLatestPushStatus($stateParams.recordId);
          }]
        }
      });



    }]);

})();
