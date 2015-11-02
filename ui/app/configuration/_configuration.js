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

(function() {

  var module = angular.module('pnc.configuration', [
    'ui.router',
    'ui.bootstrap',
    'xeditable',
    'pnc.common.restclient',
    'pnc.util.confirmClick',
    'angularUtils.directives.uiBreadcrumbs',
    'pnc.common.directives',
    'pnc.record'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('configuration', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'configuration.list'
      }
    });

    $stateProvider.state('configuration.list', {
      url: '/configuration',
      templateUrl: 'configuration/views/configuration.list.html',
      data: {
        displayName: 'Build Configurations'
      },
      controller: 'ConfigurationListController',
      controllerAs: 'listCtrl',
      resolve: {
        configurationList: function(BuildConfigurationDAO) {
          return BuildConfigurationDAO.getAll();
        }
      }
    });

    $stateProvider.state('configuration.create', {
      url: '/configuration/create',
      templateUrl: 'configuration/views/configuration.create.html',
      data: {
        displayName: 'Create Build Configuration'
      },
      controller: 'ConfigurationCreateController',
      controllerAs: 'createCtrl',
      resolve: {
        environments: function(EnvironmentDAO) {
          return EnvironmentDAO.query();
        },
        projects: function(ProjectDAO) {
          return ProjectDAO.query();
        },
        products: function(ProductDAO) {
          return ProductDAO.query();
        },
        configurations: function(BuildConfigurationDAO) {
          return BuildConfigurationDAO.query();
        }
      },
    });

    // Sets up a view with a sidebar
    $stateProvider.state('configuration.detail', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/two-col-right-sidebar.tmpl.html'
        }
      },
      data: {
        proxy: 'configuration.detail.show'
      }
    });

    // Populate main and sidebar views.
    $stateProvider.state('configuration.detail.show', {
      url: '/configuration/{configurationId:int}',
      data: {
         displayName: '{{ configurationDetail.name }}',
      },
      views: {
        '': {
          templateUrl: 'configuration/views/configuration.detail-main.html',
          controller: 'ConfigurationDetailController',
          controllerAs: 'detailCtrl'
        },
        'sidebar': {
          templateUrl: 'configuration/views/configuration.detail-sidebar.html',
          controller: 'ConfigurationSidebarController',
          controllerAs: 'sidebarCtrl'
        },
      },
      resolve: {
        configurationDetail: function(BuildConfigurationDAO, $stateParams) {
          return BuildConfigurationDAO.get({
            configurationId: $stateParams.configurationId }).$promise;
        },
        environmentDetail: function(EnvironmentDAO, $stateParams,
                                    configurationDetail) {
          return EnvironmentDAO.get({
            environmentId: configurationDetail.environmentId  }).$promise;
        },
        projectDetail: function(ProjectDAO, $stateParams,
                                 configurationDetail) {
          return ProjectDAO.get({
            projectId: configurationDetail.projectId }).$promise;
        },
        buildRecordList: function(BuildRecordDAO, $stateParams) {
          return BuildRecordDAO.getByConfiguration({
            configurationId: $stateParams.configurationId });
        },
        runningBuildRecordList: function(RunningBuildRecordDAO) {
          return RunningBuildRecordDAO.query();
        },
        allProducts: function(ProductDAO) {
          return ProductDAO.query();
        },
        configurations: function(BuildConfigurationDAO) {
          return BuildConfigurationDAO.query();
        },
        linkedProductVersions: function(BuildConfigurationDAO, $stateParams) {
          return BuildConfigurationDAO.getProductVersions({
            configurationId: $stateParams.configurationId });
        },
        dependencies: function(BuildConfigurationDAO, $stateParams) {
          return BuildConfigurationDAO.getDependencies({
            configurationId: $stateParams.configurationId });
        }
      }
    });

  }]);

})();
