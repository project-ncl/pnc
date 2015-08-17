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

  var module = angular.module('pnc.release', [
    'ui.router',
    'ui.bootstrap',
    'pnc.product',
    'pnc.common.restclient',
    'pnc.util.date_utils',
    'angularUtils.directives.uiBreadcrumbs'
  ]);


  module.config(['$stateProvider', function ($stateProvider) {
    $stateProvider
    .state('product.version.release', {
      abstract: true,
      url: '/release',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
    })
    .state('product.version.release.create', {
      url: '/create',
      templateUrl: 'release/views/release.create-update.html',
      data: {
        proxy: 'product.version.release.create',
        displayName: 'Create Release'
      },
      controller: 'ReleaseCreateUpdateController',
      controllerAs: 'releaseCreateUpdateCtrl'
    })
    .state('product.version.release.update', {
      url: '/{releaseId:int}/update',
      templateUrl: 'release/views/release.create-update.html',
      data: {
        proxy: 'product.version.release.update',
        displayName: 'Update Release'
      },
      controller: 'ReleaseCreateUpdateController',
      controllerAs: 'releaseCreateUpdateCtrl',
      resolve: {
        releaseDetail: function(ProductReleaseDAO, $stateParams) {
          return ProductReleaseDAO.get({ releaseId: $stateParams.releaseId })
          .$promise;
        },
      },
    });

  }]);

})();
