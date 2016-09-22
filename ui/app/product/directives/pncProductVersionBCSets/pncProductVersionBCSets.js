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

  var module = angular.module('pnc.record');

  /**
   * @author Jakub Senko
   */
  module.directive('pncProductVersionBCSets', [
    '$log',
    '$state',
    'eventTypes',
    'ProductVersionDAO',
    'BuildConfigurationSetDAO',
    function ($log, $state, eventTypes, ProductVersionDAO, BuildConfigurationSetDAO) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersionBCSets/pnc-product-version-bcsets.html',
        scope: {
          version: '=',
          product: '='
        },
        link: function (scope) {

          scope.page = ProductVersionDAO.getPagedBCSets({versionId: scope.version.id });

          scope.latestBuildRecordSets = {};

          scope.page.onUpdate(function(page) {
            _(page.data).each(function(bcset) {
              if(!_(scope.latestBuildRecordSets).has(bcset.id)) { // avoid unnecessary requests
                BuildConfigurationSetDAO.getLatestBuildConfigSetRecordsForConfigSet({ configurationSetId: bcset.id }).then(function (data) {
                  scope.latestBuildRecordSets[bcset.id] = data;
                });
              }
            });
          });

          var processEvent = function (event, payload) {
            // If the BuildConfigurationdSet is shown in the page
            var bcsetFiltered = _.filter(scope.page.data, function(buildConfSet){ return buildConfSet.id === payload.buildSetConfigurationId; });
            if (_.isArray(bcsetFiltered) && !_.isEmpty(bcsetFiltered)) {
              // If the latestBuildConfigSetRecord is already shown
              if (_.has(scope.latestBuildRecordSets, payload.buildSetConfigurationId) && scope.latestBuildRecordSets[payload.buildSetConfigurationId][0].id === payload.id) {
                // I update the status with no reloads to optimize refresh
                console.log('Updating BuildRecordSet #' + payload.id + ' with status ' + payload.buildStatus + ' and ' + payload.buildSetEndTime);

                scope.latestBuildRecordSets[payload.buildSetConfigurationId][0].status = payload.buildStatus;
                scope.latestBuildRecordSets[payload.buildSetConfigurationId][0].endTime = payload.buildSetEndTime;
              }
              else {
                console.log('Reloading page');

                delete scope.latestBuildRecordSets[payload.buildSetConfigurationId];
                scope.page.reload();
              }
            }
          };

          scope.$on(eventTypes.BUILD_SET_STARTED, processEvent);
          scope.$on(eventTypes.BUILD_SET_FINISHED, processEvent);

          // Executing a build of a configurationSet forcing all the rebuilds
          scope.forceBuildConfigSet = function(configSet) {
            $log.debug('**Initiating FORCED build of SET: %s**', configSet.name);
            BuildConfigurationSetDAO.forceBuild({
              configurationSetId: configSet.id
            }, {});
          };

          // Executing a build of a configurationSet NOT forcing all the rebuilds
          scope.buildConfigSet = function(configSet) {
            $log.debug('**Initiating build of SET: %s**', configSet.name);
            BuildConfigurationSetDAO.build({
              configurationSetId: configSet.id
            }, {});
          };

        },
        controllerAs: 'ctrl',
        controller: [
          '$scope',
          'modalSelectService',
          'ProductVersion',
          function ($scope, modalSelectService, ProductVersion) {
            var ctrl = this;
            console.log('$scope == %O', $scope);
            ctrl.edit = function () {
              var modal = modalSelectService.openForBuildGroup({
                title: 'Add/Remove Build Groups to ' + $scope.product.name + ': ' + $scope.version.version,
                selected: $scope.version.buildConfigurationSets
              });

              modal.result.then(function (result) {
                // $scope.$evalAsync(function () {
                  ProductVersion.updateBuildConfigurationSets({ id: $scope.version.id }, result).$promise.then(function () {
                    $scope.page.loadPageIndex(0);
                    $scope.version = ProductVersion.get({ id: $scope.version.id }).$promise.then(function (version) {
                      $scope.version = version;
                    });
                  });
                // });
              });
            };
        }]
      };
    }
  ]);

})();
