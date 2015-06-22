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

  var module = angular.module('pnc.configuration-set');

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('configuration-set.detail', {
      url: '/{configurationSetId:int}',
      templateUrl: 'configuration-set/detail/configuration-set.detail.html',
      data: {
        displayName: '{{ configurationSetDetail.name }}'
      },
      controller: 'ConfigurationSetDetailController',
      controllerAs: 'detailSetCtrl',
      resolve: {
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
          return {
            Name: $state.current.name,
            Params: $state.params,
            URL: $state.href($state.current.name, $state.params)
          };
        }]
      }
    });

  }]);

})();
