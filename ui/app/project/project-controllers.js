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

(function() {

  var module = angular.module('pnc.project');

  module.controller('ProjectListController', [
    'projectList',
    function(projectList) {
      this.projects = projectList;
    }
  ]);

  module.controller('ProjectDetailController', [
    '$log',
    '$state',
    'projectDetail',
    function($log, $state, projectDetail) {
      var that = this;
      that.project = projectDetail;

      // Update a project after editing
      that.update = function() {
        $log.debug('Updating project: %O', that.project);
        that.project.$update(
        ).then(
          function() {
            $state.go('project.detail', {
              projectId: that.project.id
            }, {
              reload: true
            });
          }
        );
      };
    }
  ]);

  module.controller('ProjectCreateController', [
    '$scope',
    '$state',
    '$log',
    'ProjectDAO',
    function($scope, $state, $log, ProjectDAO) {

      this.create = function(project) {

        new ProjectDAO(angular.copy(project)).$save().then(function(result) {
          $state.go('project.detail', {
            projectId: result.id
          });
        });
      };

      this.reset = function(form) {
        if (form) {
          form.$setPristine();
          form.$setUntouched();
          $scope.project = new ProjectDAO();
        }
      };
    }
  ]);

  module.controller('ConfigurationListController', [
    '$log',
    '$state',
    'configurationList',
    'ProjectDAO',
    function($log, $state, configurationList, ProjectDAO) {
      var that = this;

      this.configurations = configurationList;
      this.projects = [];

      angular.forEach(this.configurations.data, function(configuration) {
        ProjectDAO.get({
          projectId: configuration.projectId
        }).$promise.then(
          function(result) {
            if (result) {
              that.projects.push(result);
            }
          }
        );
      });
    }
  ]);

  module.controller('CreateBCController', [
    'projectDetail',
    function(projectDetail) {
      // To be passed as parameter to the directive
      this.fixedProject = projectDetail;
    }
  ]);

})();
