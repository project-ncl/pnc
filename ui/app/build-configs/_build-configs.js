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

  var module = angular.module('pnc.build-configs', [
    'ui.router',
    'ui.bootstrap',
    'xeditable',
    'pnc.common.restclient',
    'pnc.util.confirmClick',
    'angularUtils.directives.uiBreadcrumbs',
    'pnc.common.directives',
    'pnc.record',
    'infinite-scroll',
    'pnc.common.authentication'
  ]);

  // Throttling scroll events
  // Scroll events can be triggered very frequently, which can hurt performance and make scrolling appear jerky.
  // To mitigate this, infiniteScroll can be configured to process scroll events a maximum of once every x milliseconds.
  angular.module('infinite-scroll').value('THROTTLE_MILLISECONDS', 350);

  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function($stateProvider, $urlRouterProvider) {

      // NCL-2402 changed the module base URL, this redirect should
      // be removed at some point in the future.
      $urlRouterProvider.when(/^\/configuration\/.*/, function ($location) {
        return $location.url().replace('/configuration/', '/build-configs/');
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
        url: '/{configurationId:int}',
        data: {
           displayName: '{{ configurationDetail.name }}',
        },
        templateUrl: 'build-configs/views/build-configs.detail-main.html',
        controller: 'ConfigurationDetailController',
        controllerAs: 'detailCtrl',
        views: {
          '': {
            templateUrl: 'build-configs/views/build-configs.detail-main.html',
            controller: 'ConfigurationDetailController',
            controllerAs: 'detailCtrl'
          },
          'sidebar': {
            templateUrl: 'build-configs/views/build-configs.detail-sidebar.html',
            controller: 'ConfigurationSidebarController',
            controllerAs: 'sidebarCtrl'
          }
        },
        resolve: {
          configurationDetail: function(BuildConfigurationDAO, $stateParams) {
            return BuildConfigurationDAO.get({
              configurationId: $stateParams.configurationId }).$promise;
          },
          linkedProductVersions: function(BuildConfigurationDAO, $stateParams) {
            return BuildConfigurationDAO.getProductVersions({
              configurationId: $stateParams.configurationId }).$promise;
          },
          dependencies: function(BuildConfigurationDAO, $stateParams) {
            return BuildConfigurationDAO.getDependencies({
              configurationId: $stateParams.configurationId }).$promise;
          },
          linkedConfigurationSetList: function(BuildConfigurationDAO, $stateParams) {
            return BuildConfigurationDAO.getConfigurationSets({
              configurationId: $stateParams.configurationId }).$promise;
          },

          environments: function(EnvironmentDAO) {
            return EnvironmentDAO.getAll().$promise;
          },
          products: function(ProductDAO) {
            return ProductDAO.getAll().$promise;
          },
          configurations: function(BuildConfigurationDAO) {
            return BuildConfigurationDAO.getAll().$promise;
          },
          configurationSetList: function(BuildConfigurationSetDAO) {
            return BuildConfigurationSetDAO.getAll().$promise;
          }
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
          displayName: 'Build Configs'
        },
        controller: 'ConfigurationListController',
        controllerAs: 'listCtrl',
        resolve: {
          configurationList: function(BuildConfigurationDAO) {
            return BuildConfigurationDAO.getAll().$promise;
          }
        }
      });

      $stateProvider.state('build-configs.detail', {
        url: '/{configurationId:int}',
        resolve: {
          configurationDetail: function(BuildConfigurationDAO, $stateParams) {
            return BuildConfigurationDAO.get({
              configurationId: $stateParams.configurationId }).$promise;
          }
        },
        onEnter: [
          '$state',
          'configurationDetail',
          function ($state, configurationDetail) {
            $state.go('projects.detail.build-configs.detail', {
              projectId: configurationDetail.project.id,
              configurationId: configurationDetail.id
            });
          }
        ]
      });

      $stateProvider.state('build-configs.create', {
        url: '/create',
        templateUrl: 'build-configs/views/build-configs.create.html',
        data: {
          displayName: 'Create Build Config',
          requireAuth: true
        }
      });
    }
  ]);

})();
