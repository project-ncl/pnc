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

  var module = angular.module('pnc.configuration-set', [
    'ui.router',
    'xeditable',
    'pnc.common.restclient',
    'angularUtils.directives.uiBreadcrumbs',
    'pnc.common.events'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('configuration-set', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
          //templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      },
      data: {
        proxy: 'configuration-set.list',
      }
    });

    $stateProvider.state('configuration-set.list', {
      url: '/configuration-set',
      templateUrl: 'configuration-set/views/configuration-set.list.html',
      data: {
        displayName: 'BuildConfiguration Sets'
      },
      controller: 'ConfigurationSetListController',
      controllerAs: 'setlistCtrl',
      resolve: {
        configurationSetList: function(PncCache, BuildConfigurationSetDAO) {
          return PncCache.key('pnc.configuration-set.ConfigurationSetListController').key('configurationSetList').getOrSet(function() {
            return BuildConfigurationSetDAO.getAllPaged();
          }).then(function(page) {
            page.reload();
            return page;
          });
        }
      }
    });

    $stateProvider.state('configuration-set.detail', {
      url: '/configuration-set/{configurationSetId:int}',
      templateUrl: 'configuration-set/views/configuration-set.detail.html',
      data: {
        displayName: '{{ configurationSetDetail.name }}',
      },
      controller: 'ConfigurationSetDetailController',
      controllerAs: 'detailSetCtrl',
      resolve: {
        configurationSetDetail: function(BuildConfigurationSetDAO, $stateParams) {
          return BuildConfigurationSetDAO.get({
            configurationSetId: $stateParams.configurationSetId });
        },
        configurations: function(configurationSetDetail) {
          return configurationSetDetail.getBuildConfigurations();
        },
        records: function(configurationSetDetail) {
          return configurationSetDetail.getBuildRecords();
        },
        previousState: ['$state', function ($state) {
          var currentStateData = {
            Name: $state.current.name,
            Params: $state.params,
            URL: $state.href($state.current.name, $state.params)
          };
          return currentStateData;
        }],
      }
    });

    $stateProvider.state('configuration-set.create', {
      url: '/configuration-set/create/:productId/:versionId',
      templateUrl: 'configuration-set/views/configuration-set.create.html',
      data: {
        displayName: 'Create Build Configuration Set'
      },
      controller: 'ConfigurationSetCreateController',
      controllerAs: 'createSetCtrl',
      resolve: {
        products: function(ProductDAO) {
          return ProductDAO.getAll();
        },
      },
    });

    $stateProvider.state('configuration-set.add-configuration', {
      url: '/configuration-set/{configurationSetId:int}/add-configuration',
      templateUrl: 'configuration-set/views/configuration-set.add.configuration.html',
      data: {
        displayName: 'Add Build Configuration'
      },
      controller: 'ConfigurationSetAddConfigurationController',
      controllerAs: 'addConfigurationSetCtrl',
      resolve: {
        configurationSetDetail: function(BuildConfigurationSetDAO, $stateParams) {
          return BuildConfigurationSetDAO.get({
            configurationSetId: $stateParams.configurationSetId });
        },
        projects: function(ProjectDAO) {
          return ProjectDAO.getAll();
        }
      }
    });

  }]);

})();
