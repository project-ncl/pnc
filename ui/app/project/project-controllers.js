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
    '$state',
    '$log',
    'BuildConfigurationDAO',
    'ProductDAO',
    'Notifications',
    'environments',
    'products',
    'configurations',
    'configurationSetList',
    'projectDetail',
    function($state, $log, BuildConfigurationDAO, ProductDAO,
             Notifications, environments, products, configurations, configurationSetList, projectDetail) {

      var that = this;

      that.data = new BuildConfigurationDAO();
      that.data.project = projectDetail;
      that.environments = environments;
      that.configurations = configurations;
      that.configurationSetList = configurationSetList;
      that.products = products;

      that.submit = function() {
        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        that.data.productVersionIds = gatherIds(that.productVersions.selected);
        that.data.dependencyIds = gatherIds(that.dependencies.selected);

        that.data.$save().then(function(result) {

          // Saving the BuildConfig link into the BuildGroupConfig 
          _.each(that.buildgroupconfigs.selected, function(buildgroupconfig) {
            buildgroupconfig.buildConfigurationIds.push(result.id);
            buildgroupconfig.$update();
          });

          $state.go('project.detail', {
            projectId: projectDetail.id
          });
        });
      };

      that.productVersions = {
        selected: []
      };

      // Selection of dependencies.
      that.dependencies = {
        selected: []
      };

      // Selection of Build Group Configs.
      that.buildgroupconfigs = {
         selected: []
      };

      // Selection of Environments
      that.environmentSelection = {
        selected: []
      };

      that.reset = function(form) {
        if (form) {
          that.productVersions.selected = [];
          that.dependencies.selected = [];
          that.buildgroupconfigs.selected = [];
          that.environmentSelection.selected = [];
          that.data = new BuildConfigurationDAO();

          form.$setPristine();
          form.$setUntouched();
          }
        };
    }
  ]);

  function gatherIds(array) {
    var result = [];
    for (var i = 0; i < array.length; i++) {
      result.push(array[i].id);
    }
    return result;
  }

})();
