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

  var module = angular.module('pnc.build-configs', [
    'ui.router',
    'ui.bootstrap',
    'xeditable',
    'pnc.common.directives',
    'pnc.builds',
    'infinite-scroll',
    'pnc.common.authentication'
  ]);

  module.config([
    '$stateProvider',
    function($stateProvider) {

      $stateProvider.state('build-configs', {
        abstract: true,
        url: '/build-configs',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'build-configs.list'
        }
      });

      $stateProvider.state('build-configs.list', {
        url: '',
        component: 'pncBuildConfigsListPage',
        data: {
          displayName: 'Build Configs',
          title: 'Build Configs'
        },
        resolve: {
          buildConfigs: ['BuildConfigResource', 'SortHelper', (BuildConfigResource, SortHelper) => {
            return BuildConfigResource.query(SortHelper.getSortQueryString('buildConfigsList')).$promise;
          }]
        }
      });


      $stateProvider.state('projects.detail.build-configs', {
        abstract: true,
        url: '/build-configs',
        views: {
          'content@': {
            templateUrl: 'common/templates/two-col-right-sidebar.tmpl.html'
          }
        },
        data: {
          proxy: 'projects.detail.build-configs.detail'
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail', {
        url: '/{configurationId}',
        redirectTo: 'projects.detail.build-configs.detail.default',
        data: {
           displayName: '{{ configurationDetail.name }}',
           title: '{{ configurationDetail.name }} | Build Config'
        },
        views: {
          '': {
            component: 'pncBuildConfigDetailMain',
            bindings: {
              buildConfig: 'configurationDetail'
            }
          },
          sidebar: {
            component: 'pncBuildConfigDetailSidebar',
            bindings: {
              buildConfig: 'configurationDetail'
            }
          }
        },
        resolve: {
          configurationDetail: [
            '$stateParams',
            'BuildConfigResource',
            ($stateParams, BuildConfigResource) => BuildConfigResource.get({ id: $stateParams.configurationId }).$promise
          ],
          builds: [
            '$stateParams',
            'BuildConfigResource',
            ($stateParams, BuildConfigResource) => BuildConfigResource.getBuilds({ id: $stateParams.configurationId, pageSize: 10, sort: '=desc=submitTime' }).$promise
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.default', {
        url: '',
        component: 'pncBuildConfigDetailsTab',
        data: {
          displayName: false
        },
        bindings: {
          buildConfig: 'configurationDetail'
        },
        resolve: {
          productVersion: [
            'configurationDetail',
            'ProductVersionResource',
            (configurationDetail, ProductVersionResource) => {return configurationDetail.productVersion ? ProductVersionResource.get({ id: configurationDetail.productVersion.id }).$promise : null}
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.dependencies', {
        url: '/dependencies',
        component: 'pncBuildConfigDependenciesTab',
        data: {
          displayName: 'Dependencies'
        },
        bindings: {
          buildConfig: 'configurationDetail'
        },
        resolve: {
          dependencies: ['configurationDetail', buildConfig => buildConfig.$getDependencies()]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.dependants', {
        url: '/dependendants',
        component: 'pncBuildConfigDependantsTab',
        data: {
          displayName: 'Dependants'
        },
        bindings: {
          buildConfig: 'configurationDetail'
        },
        resolve: {
          dependants: [
            'configurationDetail',
            function (configurationDetail) {
              return configurationDetail.$getDependants();
            }
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.build-groups', {
        url: '/build-groups',
        component: 'pncBuildConfigBuildGroupsTab',
        data: {
          displayName: 'Build Groups'
        },
        bindings: {
          buildConfig: 'configurationDetail'
        },
        resolve: {
          buildGroups: [
            'configurationDetail',
            'BuildConfigurationSet',
            function (configurationDetail, BuildConfigurationSet) {
              return BuildConfigurationSet.queryContainsBuildConfiguration({}, { id: configurationDetail.id }).$promise;
            }
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.revisions', {
        url: '/revisions',
        redirectTo: function (trans) {
          //'projects.detail.build-configs.detail.revisions.detail',
          return trans.injector().getAsync('revisions').then(function (revisions) {
            var revision = revisions.data[0];
            return {
              state: 'projects.detail.build-configs.detail.revisions.detail',
              params: {
                projectId: revision.project.id,
                configurationId: revision.id,
                revisionId: revision.rev
              }
            };
          });
        },
        data: {
          displayName: 'Revisions'
        },
        component: 'pncBuildConfigRevisionsTab',
        bindings: {
          buildConfig: 'configurationDetail'
        },
        resolve: {
          revisions: [
            'configurationDetail',
            function (configurationDetail) {
              return configurationDetail.$getRevisions();
            }
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.revisions.detail', {
        url: '/{revisionId}',
        views: {
          'master': {
            component: 'pncRevisionsVerticalNav',
            bindings: {
              buildConfig: 'configurationDetail',
              revisions: 'revisions'
            }
          },
          'detail': {
            component: 'pncRevisionsDetails'
          }
        },
        data: {
          displayName: '{{ revision.lastModificationTime | date : \'medium\' }}'
        },
        resolve: {
          revision : [
            'configurationDetail',
            '$stateParams',
            function (configurationDetail, $stateParams) {
              return configurationDetail.$getRevision({ revisionId: $stateParams.revisionId });
            }
          ]
        }
      });

      $stateProvider.state('projects.detail.build-configs.detail.build-metrics', {
        url: '/build-metrics',
        component: 'pncBuildConfigBuildMetricsTab',
        data: {
          displayName: 'Build Metrics'
        },
        resolve: {
          builds: [
            'BuildResource',
            '$stateParams',
            function (BuildResource, $stateParams) {
              return BuildResource.getByConfiguration({
                id: $stateParams.configurationId,
                pageSize: 200
              }).$promise;
            }
          ]
        }
      });

    }
  ]);

})();
