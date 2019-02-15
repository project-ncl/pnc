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

  var module = angular.module('pnc.build-configs', [
    'ui.router',
    'ui.bootstrap',
    'xeditable',
    'pnc.common.restclient',
    'pnc.common.directives',
    'pnc.build-records',
    'infinite-scroll',
    'pnc.common.authentication'
  ]);

  // Throttling scroll events
  // Scroll events can be triggered very frequently, which can hurt performance and make scrolling appear jerky.
  // To mitigate this, infiniteScroll can be configured to process scroll events a maximum of once every x milliseconds.
  angular.module('infinite-scroll').value('THROTTLE_MILLISECONDS', 350);

  module.config([
    '$stateProvider',
    function($stateProvider) {

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
        url: '/{configurationId:int}',
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
          'sidebar': {
            templateUrl: 'build-configs/views/build-configs.detail-sidebar.html',
            controller: 'ConfigurationSidebarController',
            controllerAs: 'sidebarCtrl'
          }
        },
        resolve: {
          configurationDetail: [
            '$stateParams',
            'BuildConfiguration',
            function ($stateParams, BuildConfiguration) {
              return BuildConfiguration.get({ id: $stateParams.configurationId }).$promise;
            }
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
          dependencies: [
            'configurationDetail',
            function (configurationDetail) {
              return configurationDetail.$getDependencies();
            }
          ]
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

      $stateProvider.state('projects.detail.build-configs.detail.products', {
        url: '/products',
        component: 'pncBuildConfigProductsTab',
        data: {
          displayName: 'Products'
        },
        bindings: {
          buildConfig: 'configurationDetail'
        },
        resolve: {
          productVersions: [
            'configurationDetail',
            'ProductVersion',
            function (configurationDetail, ProductVersion) {
              return ProductVersion.queryContainsBuildConfiguration({}, { id: configurationDetail.id }).$promise;
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
        url: '/{revisionId:int}',
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



      /*
       * Shortcut states
       */

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
        templateUrl: 'build-configs/views/build-configs.list.html',
        data: {
          displayName: 'Build Configs',
          title: 'Build Configs'
        },
        controller: 'ConfigurationListController',
        controllerAs: 'listCtrl',
        resolve: {
          configurationList: ['BuildConfigurationDAO', function(BuildConfigurationDAO) {
            return BuildConfigurationDAO.getAll().$promise;
          }]
        }
      });

      $stateProvider.state('build-configs.detail', {
        url: '/{configurationId:int}',
        resolve: {
          configurationDetail: ['BuildConfigurationDAO', '$stateParams', function(BuildConfigurationDAO, $stateParams) {
            return BuildConfigurationDAO.get({
              configurationId: $stateParams.configurationId }).$promise;
          }]
        },
        onEnter: [
          '$state',
          '$timeout',
          'configurationDetail',
          function ($state, $timeout, configurationDetail) {
            $timeout(function () { // Works around bug in ui.router https://github.com/angular-ui/ui-router/issues/1434
              $state.go('projects.detail.build-configs.detail', {
                projectId: configurationDetail.project.id,
                configurationId: configurationDetail.id
              });
            });
          }
        ]
      });

      $stateProvider.state('build-configs.create', {
        url: '/create',
        templateUrl: 'build-configs/views/build-configs.create.html',
        data: {
          displayName: 'Create Build Config',
          title: 'Create Build Config',
          requireAuth: true
        }
      });
    }
  ]);

})();
