/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.build-configs');

  module.directive('pncConfigurationCreatePanel', [
    'eventTypes',
    'bccEventHandler',
    function (eventTypes, bccEventHandler) {

      function PncConfigurationCreateController($log, $state, $filter, $scope, pncNotify,
        EnvironmentDAO, ProjectDAO, ProductDAO, BuildConfigurationDAO, BuildConfigurationSetDAO, BpmDAO) {

        // Selection of Product Versions.
        $scope.productVersions = {
          selected: []
        };

        // Selection of dependencies.
        $scope.dependencies = {
          selected: []
        };

        // Selection of Build Group Configs.
        $scope.buildgroupconfigs = {
          selected: []
        };

        // Selection of Projects
        $scope.projectSelection = {
          selected: []
        };

        // Selection of Environments
        $scope.environmentSelection = {
          selected: []
        };

        $scope.data = {};
        $scope.environments = EnvironmentDAO.getAll();
        $scope.products = ProductDAO.getAll();
        $scope.configurations = BuildConfigurationDAO.getAll();
        $scope.configurationSetList = BuildConfigurationSetDAO.getAll();

        $scope.genericParameters = {};

        if (_.isUndefined($scope.fixedProject)) {
          $scope.projects = ProjectDAO.getAll();
        } else {
          $scope.projectSelection.selected[0] = JSON.parse($scope.fixedProject);
          $scope.data.project = $scope.projectSelection.selected[0];
        }

        $scope.submit = function() {

          // The REST API takes integer Ids so we need to extract them from
          // our collection of objects first and attach them to our data object
          // for sending back to the server.
          $scope.data.productVersionId = getFirstId($scope.productVersions.selected);
          $scope.data.dependencyIds = gatherIds($scope.dependencies.selected);
          $scope.data.buildConfigurationSetIds = gatherIds($scope.buildgroupconfigs.selected);

          $scope.data.scmExternal = {
            url:      $scope.data.externalScmUrl,
            revision: $scope.data.externalScmRevision
          };
          $scope.data.scmInternal = {
            url:      $scope.data.scmUrl,
            revision: $scope.data.scmRevision
          };

          $scope.data.genericParameters = $scope.genericParameters;

          BpmDAO.startBuildConfigurationCreation($scope.data).then(
            function(data) {
              $state.go('projects.detail', { projectId: $scope.data.project.id });

              pncNotify.info('Build Configuration "' + $scope.data.name + '" is being created. ' +
                'Please wait, this may take up to a few minutes. Notifications will inform you about the progress.');
              var registered = bccEventHandler.register(data.data, $scope.data.name);
              return registered;
            }).catch(
              function() {
                $log.error('Start build configuration creation failed.');
              }
            );
        };

        $scope.reset = function(form) {
          if (form) {
            $scope.productVersions.selected = [];
            $scope.dependencies.selected = [];
            $scope.buildgroupconfigs.selected = [];
            $scope.environmentSelection.selected = [];
            $scope.data = {};
            $scope.genericParametersControl.reset();

            if (_.isUndefined($scope.fixedProject)) {
              $scope.projectSelection.selected = [];
            }

            form.$setPristine();
            form.$setUntouched();
          }
        };

        $scope.isCreateEnabled = function(form) {
          return (form.$invalid || !$scope.data.environment.id || !$scope.data.project.id);
        };

        $scope.isProjectSelectable = function() {
          return (_.isUndefined($scope.fixedProject));
        };

        function gatherIds(array) {
          var result = [];
          for (var i = 0; i < array.length; i++) {
            result.push(array[i].id);
          }
          return result;
        }

        function getFirstId(array) {
            if (array.length > 0) {
                return array[0].id;
            }
            return null;
        }
      }

      return {
        restrict: 'E',
        templateUrl: 'build-configs/directives/pnc-configuration-create-panel/pnc-configuration-create-panel.html',
        scope: {
          fixedProject: '@'
        },
        controller: PncConfigurationCreateController
      };

    }
  ]);
})();
