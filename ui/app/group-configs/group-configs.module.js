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
      // $urlRouterProvider.when(/^\/build-groups\/.*/, ['$location', function ($location) {
      //   return $location.url().replace('/build-groups/', '/group-configs/');
      // }]);

      $stateProvider.state('group-configs', {
        url: '/group-configs',
        redirectTo: 'group-configs.list',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          displaName: false
        }
      });

      $stateProvider.state('group-configs.list', {
        url: '',
        component: 'pncGroupConfigsListPage',
        resolve: {
          groupConfigs: [
            'GroupConfig',
            GroupConfig => GroupConfig.query().$promise
          ]
        },
        data: {
          displayName: 'Group Configs',
          title: 'Group Configs'
        }
      });


      $stateProvider.state('build-groups', {
        abstract: true,
        url: '/build-groups',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'build-groups.list',
        }
      });

      $stateProvider.state('build-groups.detail', {
        abstract: true,
        url: '/{configurationSetId}',
        templateUrl: 'build-groups/views/build-groups.detail.html',
        data: {
          displayName: '{{ configurationSetDetail.name }}',
          title: '{{ configurationSetDetail.name }} | Build Group'
        },
        controller: 'ConfigurationSetDetailController',
        controllerAs: 'detailSetCtrl',
        resolve: {
          configurationSetDetail: ['BuildConfigurationSet', '$stateParams', function(BuildConfigurationSet, $stateParams) {
            return BuildConfigurationSet.get({
              id: $stateParams.configurationSetId }).$promise;
          }],
          productVersion: ['$q', 'ProductVersion', 'configurationSetDetail', function ($q, ProductVersion, configurationSetDetail) {
            return $q.when(configurationSetDetail.productVersionId).then(function (id) {
              if (id) {
                return ProductVersion.get({ id: id }).$promise;
              }
            });
          }],
          configurations: ['BuildConfigurationSetDAO', '$stateParams', function(BuildConfigurationSetDAO, $stateParams) {
            return BuildConfigurationSetDAO.getConfigurations({
              configurationSetId: $stateParams.configurationSetId });
          }],
          previousState: ['$state', '$q', function ($state, $q) {
            var currentStateData = {
              Name: $state.current.name,
              Params: $state.params,
              URL: $state.href($state.current.name, $state.params)
            };
            return $q.when(currentStateData);
          }],
          buildConfigsPage: ['$stateParams', 'BuildConfigurationSetDAO', function ($stateParams, BuildConfigurationSetDAO) {
            return BuildConfigurationSetDAO.getPagedConfigurations({
              configurationSetId: $stateParams.configurationSetId }).$promise;

          }]
        }
      });

      $stateProvider.state('build-groups.detail.build-configs', {
        url: '',
        component: 'pncBuildGroupBuildConfigs',
        bindings: {
          buildGroup: 'configurationSetDetail',
          page: 'buildConfigsPage'
        }
      });

      $stateProvider.state('build-groups.detail.build-history', {
        url: '/build-history',
        component: 'pncBuildGroupBuildHistory',
        resolve: {
          buildGroupRecords: ['$stateParams', 'BuildConfigurationSet', function ($stateParams, BuildConfigurationSet) {
            var buildGroupRecords = BuildConfigurationSet.queryBuildConfigSetRecords({ id: $stateParams.configurationSetId });
            return buildGroupRecords.$promise.then(function () { return buildGroupRecords; });
          }]
        },
        bindings: {
          buildGroup: 'configurationSetDetail'
        }
      });

      $stateProvider.state('build-groups.create', {
        url: '/create/:productId/:versionId',
        templateUrl: 'build-groups/views/build-groups.create.html',
        data: {
          displayName: 'Create Build Group',
          title: 'Create Build Group',
          requireAuth: true
        },
        controller: 'ConfigurationSetCreateController',
        controllerAs: 'createSetCtrl',
        resolve: {
          products: ['ProductDAO', function(ProductDAO) {
            return ProductDAO.getAll();
          }],
        },
      });

      $stateProvider.state('build-groups.add-configuration', {
        url: '/build-groups/{configurationSetId}/add-configuration',
        templateUrl: 'build-groups/views/build-groups.add.configuration.html',
        data: {
          displayName: 'Add Build Config',
          title: 'Add Build Config',
          requireAuth: true
        },
        controller: 'ConfigurationSetAddConfigurationController',
        controllerAs: 'addConfigurationSetCtrl',
        resolve: {
          configurationSetDetail: ['BuildConfigurationSetDAO', '$stateParams', function(BuildConfigurationSetDAO, $stateParams) {
            return BuildConfigurationSetDAO.get({
              configurationSetId: $stateParams.configurationSetId }).$promise;
          }],
          projects: ['ProjectDAO', function(ProjectDAO) {
            return ProjectDAO.query();
          }]
        }
      });
    }
  ]);

})();
