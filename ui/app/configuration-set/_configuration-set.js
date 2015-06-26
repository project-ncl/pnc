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
    'pnc.remote.restClient',
    'angularUtils.directives.uiBreadcrumbs'
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
        restClient: 'PncRestClient',
        configurationSetList: function(restClient) {
          return restClient.ConfigurationSet.query().$promise;
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
        restClient: 'PncRestClient',
        configurationSetDetail: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.get({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        configurations: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.getConfigurations({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        records: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.getRecords({
            configurationSetId: $stateParams.configurationSetId}).$promise;
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
      url: '/configuration-set/create',
      templateUrl: 'configuration-set/views/configuration-set.create.html',
      data: {
        displayName: 'Create Build Configuration Set'
      },
      controller: 'ConfigurationSetCreateController',
      controllerAs: 'createSetCtrl',
      resolve: {
        restClient: 'PncRestClient',
        products: function(restClient) {
          return restClient.Product.query().$promise;
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
        restClient: 'PncRestClient',
        configurationSetDetail: function(restClient, $stateParams) {
          return restClient.ConfigurationSet.get({
            configurationSetId: $stateParams.configurationSetId }).$promise;
        },
        projects: function(restClient) {
          return restClient.Project.query().$promise;
        },
      },
    });

  }]);

})();
