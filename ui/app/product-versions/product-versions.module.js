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

  angular.module('pnc.product-versions', []).config([
    '$stateProvider',
    ($stateProvider) => {
      $stateProvider.state('products.detail.product-versions', {
        url: '/versions',
        abstract: true,
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'products.detail'
        }
      });

      $stateProvider.state('products.detail.product-versions.create', {
        url: '/create',
        component: 'pncCreateProductVersionPage',
        data: {
          displayName: 'Create Product Version',
          title: 'Create Product Version | {{ product.name }}',
          requireAuth: true
        }
      });

      $stateProvider.state('products.detail.product-versions.detail', {
        url: '/{productVersionId}',
        component: 'pncProductVersionDetailPage',
        data: {
          displayName: '{{ productVersion.version }}',
          title: '{{ productVersion.version }}'
        },
        resolve: {
          productVersion: [
            '$stateParams',
            'ProductVersionResource',
            ($stateParams, ProductVersionResource) => {
              return ProductVersionResource.get({ id: $stateParams.productVersionId }).$promise;
            }
          ],
          buildConfigs: [
            '$stateParams',
            'ProductVersionResource',
            ($stateParams, ProductVersionResource) => {
              return ProductVersionResource.queryBuildConfigs({ id: $stateParams.productVersionId }).$promise;
            }
          ],
          groupConfigs: [
            '$stateParams',
            'ProductVersionResource',
            ($stateParams, ProductVersionResource) => {
              return ProductVersionResource.queryGroupConfigs({ id: $stateParams.productVersionId }).$promise;
            }
          ],
          productReleases: [
            '$stateParams',
            'ProductVersionResource',
            ($stateParams, ProductVersionResource) => {
              return ProductVersionResource.queryReleases({
                id: $stateParams.productVersionId,
                pageSize: 10
              }).$promise;
            }
          ],
          productMilestones: [
            '$stateParams',
            'ProductVersionResource',
            ($stateParams, ProductVersionResource) => {
              return ProductVersionResource.queryMilestones({
                id: $stateParams.productVersionId,
                pageSize: 10
              }).$promise;
            }
          ]
        }
      });
    }
  ]);

})();
