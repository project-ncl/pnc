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

  var module = angular.module('pnc.milestone', [
    'ui.router',
    'ui.bootstrap',
    'patternfly',
    'pnc.common.restclient',
    'pnc.common.util'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider
      .state('product.detail.version.milestoneDetail', {
        url: '/milestone/{milestoneId}/detail',
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
      .state('product.detail.version.milestoneDetail.log', {
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
      .state('product.detail.version.milestoneCreate', {
        url: '/milestone/create',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.create-update.html',
            controller: 'MilestoneCreateUpdateController',
            controllerAs: 'milestoneCreateUpdateCtrl',
          }
        },
        data: {
          displayName: 'Create Milestone',
          title: '{{ versionDetail.version }} | {{ productDetail.name }} | Create Milestone',
          requireAuth: true
        },
        resolve: {
          milestoneDetail: [function() { return null; }]
        },
      })
      .state('product.detail.version.milestoneUpdate', {
        url: '/milestone/{milestoneId}/update',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.create-update.html',
            controller: 'MilestoneCreateUpdateController',
            controllerAs: 'milestoneCreateUpdateCtrl',
          }
        },
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
      .state('product.detail.version.milestoneClose', {
        url: '/milestone/{milestoneId}/close',
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
