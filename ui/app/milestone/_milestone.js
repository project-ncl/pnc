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

(function () {

  var module = angular.module('pnc.milestone', [
    'ui.router',
    'ui.bootstrap',
    'patternfly',
    'pnc.product',
    'pnc.common.restclient',
    'pnc.util.date_utils',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider
      .state('product.detail.version.milestoneDetail', {
        url: '/milestone/{milestoneId:int}/detail',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.detail.html',
            controller: 'MilestoneDetailController',
            controllerAs: 'milestoneDetailCtrl',
          }
        },
        data: {
          displayName: '{{ milestoneDetail.version }}'
        },
        resolve: {
          milestoneDetail: function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
                .$promise;
          },
          distributedArtifacts: function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.getPagedDistributedArtifacts({milestoneId: $stateParams.milestoneId});
          },
          performedBuilds: function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.getPagedPerformedBuilds({milestoneId: $stateParams.milestoneId});
          },
          latestRelease: function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO
                .getLatestRelease({ milestoneId: $stateParams.milestoneId })
                .$promise;
          }
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
          displayName: 'Workflow Log'
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
          displayName: 'Create Milestone'
        },
        resolve: {
          milestoneDetail: function() { return null; }
        },
      })
      .state('product.detail.version.milestoneUpdate', {
        url: '/milestone/{milestoneId:int}/update',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.create-update.html',
            controller: 'MilestoneCreateUpdateController',
            controllerAs: 'milestoneCreateUpdateCtrl',
          }
        },
        data: {
          displayName: 'Update Milestone'
        },
        resolve: {
          milestoneDetail: function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
              .$promise;
          }
        }
      })
      .state('product.detail.version.milestoneClose', {
        url: '/milestone/{milestoneId:int}/close',
        views: {
          'content@': {
            templateUrl: 'milestone/views/milestone.close.html',
            controller: 'MilestoneCloseController',
            controllerAs: 'milestoneCloseCtrl',
          }
        },
        data: {
          displayName: 'Close Milestone'
        },
        resolve: {
          milestoneDetail: function (ProductMilestoneDAO, $stateParams) {
            return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
              .$promise;
          }
        }
      });

  }]);

})();
