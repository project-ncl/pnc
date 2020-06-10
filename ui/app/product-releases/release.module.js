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

  var module = angular.module('pnc.product-releases', [
    'ui.router',
    'ui.bootstrap',
    'patternfly',
    'pnc.common.util'
  ]);


  module.config(['$stateProvider', function ($stateProvider) {
    $stateProvider

    .state('products.detail.product-versions.detail.release', {
      abstract: true,
      url: '/releases',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'products.detail.product-versions.detail'
      }
    })

    .state('products.detail.product-versions.detail.release.create', {
      url: '/create',
      component: 'pncProductReleaseCreateUpdatePage',
      data: {
        displayName: 'Create Release',
        title: '{{ productVersion.version }} | {{ product.name }} | Create Release',
        requireAuth: true
      },
      resolve: {
        productRelease: [function() { return null; }]
      },
    })

    .state('products.detail.product-versions.detail.release.update', {
      url: '/{releaseId}/update',
      component: 'pncProductReleaseCreateUpdatePage',
      data: {
        displayName: 'Update Release',
        title: '{{ productVersion.version }} | {{ product.name }} | Update Release',
        requireAuth: true
      },
      resolve: {
        productRelease: ['ProductReleaseResource', '$stateParams', (ProductReleaseResource, $stateParams) => ProductReleaseResource.get({
          id: $stateParams.releaseId
        }).$promise]
      },
    });

  }]);

})();
