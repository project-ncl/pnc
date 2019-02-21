/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.build-records');

  /**
   * @author Jakub Senko
   */
  module.directive('pncProductVersionBCSets', [
    '$log',
    '$state',
    'eventTypes',
    'ProductVersionDAO',
    function ($log, $state, eventTypes, ProductVersionDAO) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersionBCSets/pnc-product-version-bcsets.html',
        scope: {
          version: '=',
          product: '='
        },
        link: function (scope) {
          scope.page = ProductVersionDAO.getPagedBCSets({versionId: scope.version.id });
        },
        controllerAs: 'ctrl',
        controller: [
          '$scope',
          'modalSelectService',
          'ProductVersion',
          function ($scope, modalSelectService, ProductVersion) {
            var ctrl = this;

            ctrl.edit = function () {
              var modal = modalSelectService.openForBuildGroups({
                title: 'Add/Remove Build Groups to ' + $scope.product.name + ': ' + $scope.version.version,
                selected: $scope.version.buildConfigurationSets
              });

              modal.result.then(function (result) {
                ProductVersion.updateBuildConfigurationSets({ id: $scope.version.id }, result).$promise.then(function () {
                  $scope.page.loadPageIndex(0);
                  $scope.version = ProductVersion.get({ id: $scope.version.id }).$promise.then(function (version) {
                    $scope.version = version;
                  });
                });
              });
            };
        }]
      };
    }
  ]);

})();
