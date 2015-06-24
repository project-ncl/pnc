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

  var module = angular.module('pnc.product');

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('product.detail.version.detail', {
      url: '/{versionId:int}',
      templateUrl: 'product/detail/version/detail/product.version.detail.html',
      data: {
         displayName: 'Version {{ versionDetail.version }}'
      },
      controller: 'ProductVersionController',
      controllerAs: 'versionCtrl',
      resolve: {
        versionDetail: function(restClient, $stateParams) {
          return restClient.Version.get({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        },
        buildConfigurationSets: function(restClient, $stateParams) {
          return restClient.Version.getAllBuildConfigurationSets({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        },
        buildConfigurations: function(restClient, $stateParams) {
          return restClient.Configuration.getAllForProductVersion({
            productId: $stateParams.productId,
            versionId: $stateParams.versionId }).$promise;
        },
        productReleases: function(restClient, $stateParams) {
          return restClient.Release.getAllForProductVersion({
            versionId: $stateParams.versionId }).$promise;
        },
        productMilestones: function(restClient, $stateParams) {
          return restClient.Milestone.getAllForProductVersion({
            versionId: $stateParams.versionId }).$promise;
        }
      }
    });

  }]);

})();
