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

  var module = angular.module('pnc.import');


  module.config([
    '$stateProvider',
    function ($stateProvider) {

      $stateProvider.state('import.product', {
        url: '/product',
        views: {
          'content@': {
            templateUrl: 'import/product/views/import.product.html',
            controller: 'ProductImportCtrl',
            controllerAs: 'ctrl'
          }
        },
        data: {
          //proxy: 'Create Release'
        },
        resolve: {
          //releaseDetail: function() { return null; }
        }
      });

    }
  ]);


  module.run([
    '$rootScope',
    '$modal',
    '$state',
    function ($rootScope, $modal, $state) {
      /* jshint unused: false */
      $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) {
        if(fromState.name === 'import.product' && $rootScope.importProductState === 'bc' && !$rootScope.productImportResetConfirmed) {
          event.preventDefault();
          $modal.open({
            templateUrl: 'common/util/views/confirm-click.html',
            controller: [
              '$scope',
              function ($scope) {
                $scope.message = 'Do you want to leave the page? Current import process data will be lost.' +
                  ' You could open the other page in a new tab.';
                $scope.confirm = function () {
                  $scope.$close();
                  $rootScope.productImportResetConfirmed = true;
                  $state.go(toState,toParams);
                };
                $scope.cancel = function () {
                  $scope.$dismiss();
                };
              }
            ]
          });
        }
        $rootScope.productImportResetConfirmed = false;
      });
    }
  ]);
})();
