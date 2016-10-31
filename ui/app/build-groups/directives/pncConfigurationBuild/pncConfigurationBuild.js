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
(function () {
  'use strict';

  var module = angular.module('pnc.build-groups');

  /**
   * @ngdoc directive
   * @name pnc.configuration-set:pncConfigurationBuild
   * @restrict E
   * @param {number=} pnc-configuration-id
   * The id of the BuildConfiguration to display build for.
   * @description
   * Displays a item with the last build of the given BuildConfiguration.
   * @example
      <pnc-configuration-build pnc-configuration-id="4"></pnc-configuration-build>
   * @author Martin Kelnar
   */
  module.directive('pncConfigurationBuild', [
    function () {

      function PncConfigurationBuildCtrl($log, $scope, BuildsDAO, eventTypes) {

        /*
         * Paged resources is used to be consistent with pnc.configuration:pncBuildsPanel directive
         * and be able to easily extend functionality to display more than one record in the future.
         */
        $scope.page = BuildsDAO.getLastByConfiguration({
          id: $scope.pncConfigurationId
        });

        function update(event, payload) {
          $log.debug('pncConfigurationBuild::update >> event = %O, payload = %O', event, payload);
          if (payload.buildConfigurationId === $scope.pncConfigurationId) {
            $scope.page.reload();
          }
        }

        $scope.$on(eventTypes.BUILD_STARTED, update);
        $scope.$on(eventTypes.BUILD_FINISHED, update);
      }

      return {
        restrict: 'E',
        templateUrl: 'build-groups/directives/pncConfigurationBuild/pnc-configuration-build.html',
        scope: {
          pncConfigurationId: '='
        },
        controller: PncConfigurationBuildCtrl
      };
    }
  ]);

})();
