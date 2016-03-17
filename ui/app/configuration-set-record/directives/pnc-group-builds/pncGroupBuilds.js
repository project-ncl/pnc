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

  var module = angular.module('pnc.configuration-set-record');

  /**
   * @ngdoc directive
   * @name pnc.configuration:pncGroupBuilds
   * @restrict E
   * @description
   * Displays a paginated panel showing running and completed group builds.
   * @example
      <pnc-group-builds></pnc-group-builds>
   * @author Alex Creasy
   */
  module.directive('pncGroupBuilds', [
    function () {
      function PncGroupBuildsCtrl($log, $scope, BuildConfigurationSetRecordDAO, eventTypes) {
        $scope.page = BuildConfigurationSetRecordDAO.getPaged();

        $scope.$on(eventTypes.BUILD_SET_STARTED, $scope.page.reload());
        $scope.$on(eventTypes.BUILD_SET_FINISHED, $scope.page.reload());
      }

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-group-builds/pnc-group-builds.html',
        scope: {},
        controller: PncGroupBuildsCtrl
      };
    }
  ]);

})();
