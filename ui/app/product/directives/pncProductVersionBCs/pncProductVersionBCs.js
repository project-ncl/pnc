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

  var module = angular.module('pnc.build-records');

  /**
   * @author Jakub Senko
   */
  module.directive('pncProductVersionBCs', [
    '$log',
    '$state',
    'eventTypes',
    'BuildConfigurationDAO',
    'BuildRecordDAO',
    function ($log, $state, eventTypes, BuildConfigurationDAO, BuildRecordDAO) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersionBCs/pnc-product-version-bcs.html',
        scope: {
          version: '='
        },
        link: function (scope) {

          scope.page = BuildConfigurationDAO.getPagedByProductVersion({
            productId: scope.version.productId, versionId: scope.version.id });

          scope.latestBuildRecords = {};

          scope.page.onUpdate(function(page) {
            _.forEach(page.data, function(bc) {
              if(!_(scope.latestBuildRecords).has(bc.id)) { // avoid unnecessary requests
                BuildRecordDAO.getLatestForConfiguration({ configurationId: bc.id }).then(function (data) {
                  scope.latestBuildRecords[bc.id] = data;
                });
              }
            });
          });

          var processEvent = function (event, payload) {
            // If the BuildConfiguration is shown in the page
            var bcFiltered = _.filter(scope.page.data, function(buildConf){ return buildConf.id === payload.buildConfigurationId; });
            if (_.isArray(bcFiltered) && !_.isEmpty(bcFiltered)) {
              // If the latestBuildConfigRecord is already shown
              if (_.has(scope.latestBuildRecords, payload.buildConfigurationId) && scope.latestBuildRecords[payload.buildConfigurationId][0].id === payload.id) {
                // I update the status with no reloads to optimize refresh
                console.log('Updating BuildRecord #' + payload.id + ' with status ' + payload.buildCoordinationStatus + ' and ' + payload.buildEndTime);

                scope.latestBuildRecords[payload.buildConfigurationId][0].status = payload.buildCoordinationStatus;
                scope.latestBuildRecordSets[payload.buildSetConfigurationId][0].endTime = payload.buildEndTime;
              }
              else {
                console.log('Reloading page');

                delete scope.latestBuildRecords[payload.buildConfigurationId];
                scope.page.reload();
              }
            }
          };

          scope.$on(eventTypes.BUILD_STARTED, processEvent);
          scope.$on(eventTypes.BUILD_FINISHED, processEvent);

          // Executing a build of a configuration forcing all the rebuilds
          scope.forceBuildConfig = function(config) {
            $log.debug('**Initiating FORCE build of: %O', config.name);

            BuildConfigurationDAO.forceBuild({
              configurationId: config.id
            }, {});
          };

          // Executing a build of a configuration
          scope.buildConfig = function(config) {
            $log.debug('**Initiating build of: %O', config.name);

            BuildConfigurationDAO.build({
              configurationId: config.id
            }, {});
          };
        }
      };
    }
  ]);

})();
