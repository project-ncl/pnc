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

  var module = angular.module('pnc.build-configs');

  /**
   * @ngdoc directive
   * @name pnc.configuration:pncBuildsPanel
   * @restrict E
   * @param {number=} pnc-configuration-id
   * The id of the BuildConfiguration to display builds for.
   * @description
   * Displays a panel with the build history of the given BuildConfiguration.
   * @example
      <pnc-builds-panel pnc-configuration-id="4"></pnc-builds-panel>
   * @author Alex Creasy
   */
  module.directive('pncBuildsPanel', [
    function () {

      function PncBuildsPanelCtrl($log, $scope, BuildsDAO, eventTypes) {
        $scope.page = BuildsDAO.getByConfiguration({
          id: $scope.pncConfigurationId
        });

        function update(event, payload) {
          $log.debug('PncBuildsPanelCtrl::update >> event = %O, payload = %O', event, payload);
          if (payload.buildConfigurationId === $scope.pncConfigurationId) {
            $scope.page.reload();
          }
        }

        $scope.$on(eventTypes.BUILD_STARTED, update);
        $scope.$on(eventTypes.BUILD_FINISHED, update);
      }

      return {
        restrict: 'E',
        templateUrl: 'build-configs/directives/pnc-builds-panel/pnc-builds-panel.html',
        scope: {
          pncConfigurationId: '='
        },
        controller: PncBuildsPanelCtrl
      };
    }
  ]);

})();
