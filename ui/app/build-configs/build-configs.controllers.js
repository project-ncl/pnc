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

  module.controller('ConfigurationListController', [
    '$log',
    '$state',
    'configurationList',
    function($log, $state, configurationList) {
      this.configurations = configurationList;
    }
  ]);

  module.controller('ConfigurationDetailController', [
    '$log',
    '$state',
    '$filter',
    'pncNotify',
    'ProductDAO',
    'BuildConfigurationSetDAO',
    'configurationDetail',
    'environments',
    'products',
    'linkedProductVersions',
    'dependencies',
    'configurations',
    'configurationSetList',
    'linkedConfigurationSetList',
    function($log, $state, $filter, pncNotify, ProductDAO, BuildConfigurationSetDAO,
      configurationDetail, environments, products,
      linkedProductVersions, dependencies, configurations, configurationSetList, linkedConfigurationSetList) {

      var that = this;

      that.configuration = configurationDetail;
      that.environment = configurationDetail.environment;
      that.environments = environments;
      that.configurations = configurations;
      that.configurationSetList = configurationSetList;
      that.products = products;

      that.productVersions = {
        selected: linkedProductVersions
      };

      // Selection of dependencies
      that.dependencies = {
        selected: dependencies || []
      };

      // Selection of environments
      that.environmentSelection = {
        selected: [that.environment]
      };

      // Selection of ConfigurationSets
      that.buildgroupconfigs = {
        selected: _.clone(linkedConfigurationSetList)
      };

      // Update a build configuration after editting
      that.update = function() {
        $log.debug('Updating configuration: %O', that.configuration);

        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        that.configuration.productVersionId =
          getFirstId(that.productVersions.selected);
        that.configuration.dependencyIds = that.dependencies.selected.map(function (d) { return d.id; });

        that.configuration.environment.id = that.environment.id;

        var added = _.difference(that.buildgroupconfigs.selected, linkedConfigurationSetList);
        var removed = _.difference(linkedConfigurationSetList, that.buildgroupconfigs.selected);

        that.configuration.$update().then(function() {

          // Saving the BuildConfig link into the BuildGroupConfig for the added ones
          _.each(added, function(buildgroupconfig) {
            buildgroupconfig.buildConfigurationIds.push(that.configuration.id);
            BuildConfigurationSetDAO.update(buildgroupconfig);
          });

          // Saving the BuildConfig link into the BuildGroupConfig for the added ones
          _.each(removed, function(buildgroupconfig) {
            var odds = _.reject(buildgroupconfig.buildConfigurationIds, function(thisBuildConfigId){ return thisBuildConfigId === that.configuration.id; });
            buildgroupconfig.buildConfigurationIds = odds;
            BuildConfigurationSetDAO.update(buildgroupconfig);
          });

          $state.go('projects.detail.build-configs.detail', {
            configurationId: that.configuration.id,
            projectId: that.configuration.project.id
          }, {
            reload: true
          });
        });
      };

      // Cloning a build configuration
      that.clone = function() {
        that.configuration.$clone().then(function(result) {
          $state.go('projects.detail.build-configs.detail', {
            configurationId: result.id,
            projectId: result.project.id
          }, {
            reload: true
          });
        });
      };

      // Deleting a build configuration
      that.delete = function() {
        that.configuration.$delete().then(function() {
          $state.go('projects.detail', {
            projectId: that.configuration.project.id
          }, {
            reload: true,
            inherit: false,
            notify: true
          });
        });
      };

      that.cancel = function(form) {
        if (form) {
          $state.go('projects.detail.build-configs.detail', {
            configurationId: that.configuration.id,
            projectId: that.configuration.project.id
          }, {
            reload: true
          });
        }
      };

    }
  ]);

  module.controller('ConfigurationSidebarController', [
    '$log',
    '$stateParams',
    'configurationDetail',
    function($log, $stateParams, configurationDetail) {
      this.buildConfigurationId = $stateParams.configurationId;
      this.buildConfiguration = configurationDetail;
      this.filterBy = {
        buildConfigurationId: $stateParams.configurationId
      };

    }
  ]);

  function getFirstId(array) {
    if (!array || array.length === 0) {
      return null;
    }
    return array[0].id;
  }
})();
