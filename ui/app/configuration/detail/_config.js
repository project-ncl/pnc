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

  var module = angular.module('pnc.configuration');

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('configuration.detail', {
      url: '/{configurationId:int}',
      data: {
         displayName: '{{ configurationDetail.name }}'
      },
      views: {
        'content@': {
          templateUrl: 'common/templates/two-col-right-sidebar.tmpl.html'
        },
        '@configuration.detail': {
          templateUrl: 'configuration/detail/configuration.detail-main.html',
          controller: 'ConfigurationDetailController',
          controllerAs: 'detailCtrl'
        },
        'sidebar@configuration.detail': {
          templateUrl: 'configuration/detail/configuration.detail-sidebar.html',
          controller: 'ConfigurationSidebarController',
          controllerAs: 'sidebarCtrl'
        }
      },
      resolve: {
        configurationDetail: function(restClient, $stateParams) {
          return restClient.Configuration.get({
            configurationId: $stateParams.configurationId }).$promise;
        },
        environmentDetail: function(restClient, $stateParams,
                                    configurationDetail) {

          return restClient.Environment.get({
            environmentId: configurationDetail.environmentId  }).$promise;
        },
        projectDetail: function(restClient, $stateParams,
                                 configurationDetail) {
          return restClient.Project.get({
            projectId: configurationDetail.projectId }).$promise;
        },
        buildRecordList: function(restClient, $stateParams) {
          return restClient.Record.getAllForConfiguration({
            configurationId: $stateParams.configurationId }).$promise;
        },
        runningBuildRecordList: function(restClient) {
          return restClient.Running.query().$promise;
        },
        products: function(restClient) {
          return restClient.Product.query().$promise;
        },
        configurations: function(restClient) {
          return restClient.Configuration.query().$promise;
        },
        productVersions: function(restClient, $stateParams) {
          return restClient.Configuration.getProductVersions({
            configurationId: $stateParams.configurationId }).$promise;
        },
        dependencies: function(restClient, $stateParams) {
          return restClient.Configuration.getDependencies({
            configurationId: $stateParams.configurationId }).$promise;
        }
      }
    });

  }]);

})();
