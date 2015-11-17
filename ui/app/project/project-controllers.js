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
    'projectConfigurationList',
    function($log, $state, projectDetail, projectConfigurationList) {
      var that = this;
      that.project = projectDetail;
      that.projectConfigurationList = projectConfigurationList;

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
    '$stateParams',
    '$log',
    '$filter',
    'BuildConfigurationDAO',
    'ProductDAO',
    'Notifications',
    'environments',
    'products',
    function($state, $stateParams, $log, $filter, BuildConfigurationDAO, ProductDAO,
             Notifications, environments, products) {

      var that = this;

      this.data = new BuildConfigurationDAO({ projectId: $stateParams.projectId }); // TODO is this correct?
      this.environments = environments;


      this.submit = function() {
        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        that.data.productVersionIds = gatherIds(that.productVersions.selected);
        that.data.dependencyIds = gatherIds(that.dependencies.selected);

        that.data.$save().then(function() {
          $state.go('project.detail', {
            projectId: $stateParams.projectId
          });
        });
      };

      // Filtering and selection of linked ProductVersions.
      this.products = {
        all: products,
        selected: null
      };

      // Could not make it work in a nicer way (i.e. via cachedGetter) - avibelli
      this.allProductsMaps = {};
      this.allProductNamesMaps = {};
      this.products.all.forEach(function ( prod ) {
          that.allProductsMaps[ prod.id ] = prod;
      });

      this.productVersions = {
        selected: [],
        all: [],

        update: function() {
          ProductDAO.getVersions({
            productId: that.products.selected.id
          }).then(function(data) {
            that.productVersions.all = data;

            // TOFIX - Ugly but quick - avibelli
            data.forEach(function ( prodVers ) {
                that.allProductNamesMaps[ prodVers.id ] = that.allProductsMaps[ prodVers.productId ].name + ' - ';
            });
          });
        },
        getItems: function($viewValue) {
          return $filter('filter')(that.productVersions.all, {
            version: $viewValue
          });
        }
      };

      // Selection of dependencies.
      this.dependencies = {
        selected: [],

        getItems: function($viewValue) {
          return BuildConfigurationDAO.querySearch({ name: $viewValue }).$promise.then(function(result) {
            return result.content;
          });
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
