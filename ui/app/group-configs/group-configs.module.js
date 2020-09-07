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

  angular.module('pnc.group-configs', [
    'ui.router',
    'xeditable',
    'pnc.common.events',
    'pnc.common.authentication',
    'pnc.common.pnc-client'
  ])

  .config([
    '$stateProvider',
    '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

      // NCL-4200 renamed build-groups to group-configs, this forwarder should be removed at some point in the future
      $urlRouterProvider.when(/^\/build-groups\/.*/, ['$location', function ($location) {
        return $location.url().replace('/build-groups/', '/group-configs/');
      }]);

      $stateProvider.state('group-configs', {
        abstract: true,
        url: '/group-configs',
        redirectTo: 'group-configs.list',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'group-configs.list'
        }
      });

      $stateProvider.state('group-configs.list', {
        url: '',
        component: 'pncGroupConfigsListPage',
        resolve: {
          groupConfigsPage: [
            'GroupConfigResource', 'SortHelper',
            (GroupConfigResource, SortHelper) => GroupConfigResource.query(SortHelper.getSortQueryString('groupConfigsList')).$promise
          ]
        },
        data: {
          displayName: 'Group Configs',
          title: 'Group Configs'
        }
      });

      $stateProvider.state('group-configs.detail', {
        url: '/{groupConfigId:int}',
        component: 'pncGroupConfigDetailPage',
        resolve: {
          groupConfig: [
            '$stateParams',
            'GroupConfigResource',
            ($stateParams, GroupConfigResource) => GroupConfigResource.get({ id: $stateParams.groupConfigId }).$promise
          ],
          productVersion: [
            'groupConfig',
            'ProductVersionResource',
            (groupConfig, ProductVersionResource) => {
              if (!groupConfig.productVersion) {
                return null;
              }
              return ProductVersionResource.get({ id: groupConfig.productVersion.id }).$promise;
            }
          ],
          previousState: [
            '$state',
            '$q',
            ($state, $q) => {
              const currentStateData = {
                Name: $state.current.name,
                Params: $state.params,
                URL: $state.href($state.current.name, $state.params)
              };
              return $q.when(currentStateData);
            }
          ],
        },
        data: {
          displayName: '{{ groupConfig.name }}',
          title: '{{ groupConfig.name }} | Group Configs'
        },
        redirectTo: 'group-configs.detail.build-configs'
      });

      $stateProvider.state('group-configs.detail.build-configs', {
        url: '',
        component: 'pncGroupConfigBuildConfigsTab',
        resolve: {
          buildConfigs: [
            'groupConfig',
            (groupConfig) => {
              return groupConfig.$queryBuildConfigs();
            }
          ]
        },
        data: {
          displayName: false
        }
      });

      $stateProvider.state('group-configs.detail.build-history', {
        url: '/build-history',
        component: 'pncGroupConfigBuildHistoryTab',
        resolve: {
          groupBuilds: [
            'groupConfig',
            groupConfig => groupConfig.$queryGroupBuilds()
          ]
        },
        data: {
          displayName: false,
        }
      });

      $stateProvider.state('group-configs.create', {
        url: '/create',
        component: 'pncGroupConfigCreatePage',
        data: {
          requireAuth: true,
          displayName: 'Create Group Config',
          title: 'Create | Group Configs'
        }
      });
    }
  ]);

})();
