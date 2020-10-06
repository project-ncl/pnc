/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
    'pnc.properties'
  ]);

  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

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
            'BuildResource', 'SortHelper',
            (BuildResource, SortHelper) => BuildResource.query(SortHelper.getSortQueryString('buildsList')).$promise
          ]
        }
      });

       /*
       * Shortcut state to allow short links of the form /builds/:id
       *
       * Don't navigate here programatically as it can potentially break the back button and causes a double fetch of
       * the build.
       *
       * This only exists for users to type in a shorter URL!
       */
      $stateProvider.state('builds.detail', {
        url: '/{buildId}',
        resolve: {
          build: [
            'BuildResource', '$stateParams',
            (BuildResource, $stateParams) => BuildResource.get({ id: $stateParams.buildId }).$promise
          ]
        },
        redirectTo: trans => {
          let buildPromise = trans.injector().getAsync('build');
          return buildPromise.then(build => {
            return {
              state: 'projects.detail.build-configs.detail.builds.detail.default',
              params: {
                projectId: build.project.id,
                configurationId: build.buildConfigRevision.id,
                buildId: build.id
              }
            };
          });
        }
      });

      /*
       * NCLSUP-155 Maintain old style build URLs so links in JIRAs are not broken.
       */
      $urlRouterProvider.when('/build-records/:id', '/builds/:id');

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
          brewPushResult: ['BuildResource', '$stateParams', function (BuildResource, $stateParams) {
            return BuildResource.getBrewPushResult({ id: $stateParams.buildId });
          }],
          buildConfigRevision: ['BuildResource', 'build', function (BuildResource, build) {
            return BuildResource.getRevision({ id: build.buildConfigRevision.id, revisionId: build.buildConfigRevision.rev }).$promise;
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.default', {
        url: '',
        component: 'pncBuildDetailDetailsPage',
        data: {
          displayName: 'Job #{{ build.id }}',
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.build-metrics', {
        url: '/build-metrics',
        component: 'pncBuildDetailBuildMetricsPage',
        data: {
          displayName: 'Build Metrics',
          title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Build Metrics'
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.build-log', {
        url: '/build-log',
        component: 'pncBuildDetailBuildLogPage',
        data: {
          displayName: 'Build Log',
          title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Build Log'
        },
        resolve: {
          buildLog: ['BuildResource', 'build', function (BuildResource, build) {
            return BuildResource.getLogBuild({ id: build.id }).$promise;
          }],
          sshCredentials: ['BuildResource', 'build', function (BuildResource, build) {
            return BuildResource.getSshCredentials({
              id: build.id,
              buildUser: build.user
            });
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.artifacts', {
        url: '/artifacts',
        component: 'pncBuildDetailArtifactsPage',
        data: {
          displayName: 'Build Artifacts',
          title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Build Artifacts'
        },
        resolve: {
          artifacts: [
            'BuildResource',
            'build',
            (BuildResource, build) => BuildResource.getBuiltArtifacts({ id: build.id, pageSize: 10 }).$promise
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.dependencies', {
          url: '/dependencies',
          component: 'pncBuildDetailDependenciesPage',
          data: {
            displayName: 'Dependencies',
            title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Dependencies'
          },
          resolve: {
            artifacts: [
              'BuildResource',
              'build',
              (BuildResource, build) => BuildResource.getArtifactsDependencies({ id: build.id, pageSize: 10 }).$promise
            ]
          }
        });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.alignment-log', {
        url: '/alignment-log',
        component: 'pncBuildDetailAlignmentLogPage',
        data: {
          displayName: 'Alignment Log',
          title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Alignment Log'
        },
        resolve: {
          alignmentLog: ['BuildResource', 'build', function (BuildResource, build) {
            return BuildResource.getLogAlign({ id: build.id }).$promise;
          }]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.builds.detail.brew-push', {
        url: '/brew-push',
        component: 'pncBuildDetailBrewPushPage',
        data: {
          displayName: 'Brew Push Results',
          title: '#{{ build.id }} {{ build.buildConfigRevision.name }} | Brew Push'
        },
        resolve: {
          brewPushResult: [
            'BuildResource',
            'build',
            (BuildResource, build) => BuildResource.getBrewPushResult({ id: build.id }).$promise
          ]
        }
      });



    }]);

})();
