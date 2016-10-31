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

  var module = angular.module('pnc.build-groups', [
    'ui.router',
    'xeditable',
    'pnc.common.restclient',
    'angularUtils.directives.uiBreadcrumbs',
    'pnc.common.events',
    'pnc.common.authentication',
    'pnc.common.pnc-client'
  ]);

  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function ($stateProvider, $urlRouterProvider) {

      // NCL-2401 changed the module base URL, this redirect should
      // be removed at some point in the future.
      $urlRouterProvider.when(/^\/configuration-set\/.*/, function ($location) {
        return $location.url().replace('/configuration-set/', '/build-groups/');
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

      $stateProvider.state('build-groups.list', {
        url: '',
        templateUrl: 'build-groups/views/build-groups.list.html',
        data: {
          displayName: 'Build Groups'
        },
        controller: 'ConfigurationSetListController',
        controllerAs: 'setlistCtrl',
        resolve: {
          configurationSetList: function(BuildConfigurationSetDAO) {
            return BuildConfigurationSetDAO.getAll().$promise;
          }
        }
      });

      $stateProvider.state('build-groups.detail', {
        url: '/{configurationSetId:int}',
        templateUrl: 'build-groups/views/build-groups.detail.html',
        data: {
          displayName: '{{ configurationSetDetail.name }}',
        },
        controller: 'ConfigurationSetDetailController',
        controllerAs: 'detailSetCtrl',
        resolve: {
          configurationSetDetail: function(BuildConfigurationSet, $stateParams) {
            return BuildConfigurationSet.get({
              id: $stateParams.configurationSetId }).$promise;
          },
          productVersion: function ($q, ProductVersion, configurationSetDetail) {
            var id = configurationSetDetail.productVersionId;
            if (angular.isUndefined(id) || id === null) {
              return $q.when();
            }
            return ProductVersion.get({ id: configurationSetDetail.productVersionId }).$promise;
          },
          configurations: function(BuildConfigurationSetDAO, $stateParams) {
            return BuildConfigurationSetDAO.getConfigurations({
              configurationSetId: $stateParams.configurationSetId });
          },
          records: function(BuildConfigurationSetDAO, $stateParams) {
            return BuildConfigurationSetDAO.getRecords({
              configurationSetId: $stateParams.configurationSetId});
          },
          previousState: ['$state', '$q', function ($state, $q) {
            var currentStateData = {
              Name: $state.current.name,
              Params: $state.params,
              URL: $state.href($state.current.name, $state.params)
            };
            return $q.when(currentStateData);
          }],
        }
      });

      $stateProvider.state('build-groups.create', {
        url: '/create/:productId/:versionId',
        templateUrl: 'build-groups/views/build-groups.create.html',
        data: {
          displayName: 'Create Build Group',
          requireAuth: true
        },
        controller: 'ConfigurationSetCreateController',
        controllerAs: 'createSetCtrl',
        resolve: {
          products: function(ProductDAO) {
            return ProductDAO.getAll();
          },
        },
      });

      $stateProvider.state('build-groups.add-configuration', {
        url: '/build-groups/{configurationSetId:int}/add-configuration',
        templateUrl: 'build-groups/views/build-groups.add.configuration.html',
        data: {
          displayName: 'Add Build Config',
          requireAuth: true
        },
        controller: 'ConfigurationSetAddConfigurationController',
        controllerAs: 'addConfigurationSetCtrl',
        resolve: {
          configurationSetDetail: function(BuildConfigurationSetDAO, $stateParams) {
            return BuildConfigurationSetDAO.get({
              configurationSetId: $stateParams.configurationSetId }).$promise;
          },
          projects: function(ProjectDAO) {
            return ProjectDAO.query();
          }
        }
      });
    }
  ]);

})();
