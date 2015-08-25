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
    .state('product.detail.version.releaseCreate', {
      url: '/release/create',
      views: {
        'content@': {
          templateUrl: 'release/views/release.create-update.html',
          controller: 'ReleaseCreateUpdateController',
          controllerAs: 'releaseCreateUpdateCtrl',
        }
      },
      data: {
        displayName: 'Create Release'
      },
    })
    .state('product.detail.version.releaseUpdate', {
      url: '/release/{releaseId:int}/update',
      views: {
        'content@': {
          templateUrl: 'release/views/release.create-update.html',
          controller: 'ReleaseCreateUpdateController',
          controllerAs: 'releaseCreateUpdateCtrl',
        }
      },
      data: {
        displayName: 'Update Release'
      },
      resolve: {
        releaseDetail: function(ProductReleaseDAO, $stateParams) {
          return ProductReleaseDAO.get({ releaseId: $stateParams.releaseId })
          .$promise;
        },
      },
    });

  }]);

})();
