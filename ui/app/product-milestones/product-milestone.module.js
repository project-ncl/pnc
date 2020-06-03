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
        url: '/{productMilestoneId}',
        component: 'pncProductMilestoneDetailPage',
        data: {
          displayName: '{{ productMilestone.version }}',
          title: '{{ productMilestone.version }} | {{ product.name }} | Milestone'
        },
        resolve: {
          productMilestone: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams) =>
            ProductMilestoneResource.get({ id: $stateParams.productMilestoneId }).$promise
          ],
          performedBuilds: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams) =>
            ProductMilestoneResource.queryPerformedBuilds({ id: $stateParams.productMilestoneId }).$promise
          ],
          closeResults: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams) =>
            ProductMilestoneResource.queryCloseResults({ id: $stateParams.productMilestoneId }).$promise
          ],
          latestCloseResult: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams, $log) =>
            ProductMilestoneResource.getLatestCloseResult({ id: $stateParams.productMilestoneId }).$promise.catch((error) => {
              $log.error('Error loading release workflow: ' + JSON.stringify(error));
            })
          ]
        }
      })

      .state('products.detail.product-versions.detail.milestone.detail.log', {
        url: '/log',
        component: 'pncProductMilestoneDetailLogPage',
        data: {
          displayName: 'Workflow Log',
          title: '{{ productMilestone.version }} | {{ product.name }} | Workflow Log'
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
        url: '/{productMilestoneId}/update',
        component: 'pncProductMilestoneCreateUpdatePage',
        data: {
          displayName: 'Update Milestone',
          title: '{{ productMilestone.version }} | {{ product.name }} | Update Milestone',
          requireAuth: true
        },
        resolve: {
          productMilestone: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams) =>
            ProductMilestoneResource.get({id: $stateParams.productMilestoneId}).$promise
          ]
        }
      })

      .state('products.detail.product-versions.detail.milestone.close', {
        url: '/{productMilestoneId}/close',
        component: 'pncProductMilestoneClosePage',
        data: {
          displayName: 'Close Milestone',
          title: '{{ productMilestone.version }} | {{ product.name }} | Close Milestone',
          requireAuth: true
        },
        resolve: {
          productMilestone: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams) =>
            ProductMilestoneResource.get({id: $stateParams.productMilestoneId}).$promise
          ]
        }
      })

      .state('products.detail.product-versions.detail.milestone.detail.close-result', {
        url: '/close-results/{closeResultId}',
        views: {
          'content@': {
            component: 'pncProductMilestoneCloseResultPage'
          }
        },
        data: {
          displayName: 'Close Result',
          title: 'Close Result | {{ productMilestone.version }} | {{ product.name }} '
        },
        resolve: {
          closeResult: ['ProductMilestoneResource', '$stateParams', (ProductMilestoneResource, $stateParams) =>
            ProductMilestoneResource.queryCloseResults({ id: $stateParams.productMilestoneId, q: 'id==' + $stateParams.closeResultId }).$promise
          ]
        }
      });

  }]);

})();
