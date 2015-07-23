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
    'projectDetail',
    'projectConfigurationList',
    function(projectDetail, projectConfigurationList) {
      this.project = projectDetail;
      this.projectConfigurationList = projectConfigurationList;
    }
  ]);

  module.controller('ProjectCreateController', [
    '$scope',
    '$state',
    '$log',
    'Project',
    function($scope, $state, $log, Project) {

      this.create = function(project) {

        new Project(angular.copy(project)).$save().then(function(result) {
          $state.go('project.detail', {
            projectId: result.id
          });
        });
      };

      this.reset = function(form) {
        if (form) {
          form.$setPristine();
          form.$setUntouched();
        }
      };
    }
  ]);

})();
