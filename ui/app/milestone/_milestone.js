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
    'pnc.product',
    'pnc.common.restclient',
    'pnc.util.date_utils',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider
    .state('product.version.milestone', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      }
    })
    .state('product.version.milestone.create', {
      url: '/milestone/create',
      templateUrl: 'milestone/views/milestone.create-update.html',
      data: {
        displayName: 'Create Milestone'
      },
      controller: 'MilestoneCreateUpdateController',
      controllerAs: 'milestoneCreateUpdateCtrl',
    })
    .state('product.version.milestone.update', {
      url: '/milestone/{milestoneId:int}/update',
      templateUrl: 'milestone/views/milestone.create-update.html',
      data: {
        displayName: 'Update Milestone'
      },
      controller: 'MilestoneCreateUpdateController',
      controllerAs: 'milestoneCreateUpdateCtrl',
      resolve: {
        milestoneDetail: function (ProductMilestoneDAO, $stateParams) {
          return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
            .$promise;
        }
      }
    })
    .state('product.version.milestone.close', {
      url: '/milestone/{milestoneId:int}/close',
      templateUrl: 'milestone/views/milestone.close.html',
      data: {
        displayName: 'Close Milestone'
      },
      controller: 'MilestoneCloseController',
      controllerAs: 'milestoneCloseCtrl',
      resolve: {
        milestoneDetail: function (ProductMilestoneDAO, $stateParams) {
          return ProductMilestoneDAO.get({milestoneId: $stateParams.milestoneId})
            .$promise;
        }
      }
    });

  }]);

})();
