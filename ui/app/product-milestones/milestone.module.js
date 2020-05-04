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

  var module = angular.module('pnc.product-milestones', [
    'ui.router',
    'ui.bootstrap',
    'patternfly',
    'pnc.common.restclient',
    'pnc.common.util'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider

      .state('products.detail.product-versions.detail.milestone', {
        abstract: true,
        url: '/milestones',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'products.detail.product-versions.detail'
        }
      })

      .state('products.detail.product-versions.detail.milestone.detail', {
        url: '/{milestoneId}',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.detail.html',
            controller: 'MilestoneDetailController',
            controllerAs: 'milestoneDetailCtrl',
          }
        },
        data: {
          displayName: '{{ milestoneDetail.version }}',
          title: '{{ milestoneDetail.version }} | {{ productDetail.name }} | Milestone'
        },
        resolve: {
          milestoneDetail: ['ProductMilestoneDAO', '$stateParams', function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
                .$promise;
          }],
          distributedArtifacts: ['ProductMilestoneDAO', '$stateParams', function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.getPagedDistributedArtifacts({milestoneId: $stateParams.milestoneId}).$promise;
          }],
          performedBuilds: ['ProductMilestoneDAO', '$stateParams', function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.getPagedPerformedBuilds({milestoneId: $stateParams.milestoneId}).$promise;
          }],
          latestRelease: ['ProductMilestoneDAO', '$stateParams', '$log', function (ProductMilestoneDAO, $stateParams, $log) {
            return ProductMilestoneDAO
                .getLatestRelease({ milestoneId: $stateParams.milestoneId })
                .$promise
                .catch(function (error) {
                  $log.error('Error loading release workflow: ' + JSON.stringify(error));
                  return {};
                });
          }]
        }
      })

      .state('products.detail.product-versions.detail.milestone.detail.log', {
        url: '/log',
        views: {
          'content@': {
             templateUrl: 'milestone/views/milestone.detail.log.html',
             controller: 'MilestoneLogController',
             controllerAs: 'milestoneLogCtrl',
          }
        },
        data: {
          displayName: 'Workflow Log',
          title: '{{ milestoneDetail.version }} | {{ productDetail.name }} | Workflow Log'

        }
      })

      .state('products.detail.product-versions.detail.milestone.create', {
        url: '/create',
        component: 'pncProductMilestoneCreateUpdatePage',
        data: {
          displayName: 'Create Milestone',
          title: '{{ productVersion.version }} | {{ product.name }} | Create Milestone',
          requireAuth: true
        },
        resolve: {
          productMilestone: [function() { return null; }]
        },
      })

      .state('products.detail.product-versions.detail.milestone.update', {
        url: '/{milestoneId}/update',
        component: 'pncProductMilestoneCreateUpdatePage',
        data: {
          displayName: 'Update Milestone',
          title: '{{ milestoneDetail.version }} | {{ productDetail.name }} | Update Milestone',
          requireAuth: true
        },
        resolve: {
          milestoneDetail: ['ProductMilestoneDAO', '$stateParams', function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
              .$promise;
          }]
        }
      })

      .state('products.detail.product-versions.detail.milestone.close', {
        url: '/{milestoneId}/close',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.close.html',
            controller: 'MilestoneCloseController',
            controllerAs: 'milestoneCloseCtrl',
          }
        },
        data: {
          displayName: 'Close Milestone',
          title: '{{ milestoneDetail.version }} | {{ productDetail.name }} | Close Milestone',
          requireAuth: true
        },
        resolve: {
          milestoneDetail: ['ProductMilestoneDAO', '$stateParams', function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
              .$promise;
          }]
        }
      });

  }]);

})();
