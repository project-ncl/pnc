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

  var module = angular.module('pnc.configuration');

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
    'BuildConfigurationDAO',
    'BuildConfigurationSetDAO',
    'configurationDetail',
    'environments',
    'products',
    'linkedProductVersions',
    'dependencies',
    'configurations',
    'configurationSetList',
    'linkedConfigurationSetList',
    function($log, $state, $filter, pncNotify, ProductDAO, BuildConfigurationDAO, BuildConfigurationSetDAO,
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
        selected: dependencies
      };

      // Selection of environments
      that.environmentSelection = {
        selected: [that.environment]
      };

      // Selection of ConfigurationSets
      that.buildgroupconfigs = {
        selected: _.clone(linkedConfigurationSetList)
      };

      // Executing a build of a configuration forcing a rebuild
      that.forceBuild = function() {
        $log.debug('Initiating FORCED build of: %O', that.configuration);
        BuildConfigurationDAO.forceBuild({
          configurationId: that.configuration.id
        }, {});
      };

      that.buildAndKeepAliveOnError = function() {
        $log.debug('Initiating FORCED build of :%0 with keeping pod alive on failure enabled', that.configuration);
        BuildConfigurationDAO.buildAndKeepAliveOnError({
          configurationId: that.configuration.id
        }, {});
      };

      // Executing a build of a configuration
      that.build = function() {
        $log.debug('Initiating build of: %O', that.configuration);
        BuildConfigurationDAO.build({
          configurationId: that.configuration.id
        }, {});
      };

      // Update a build configuration after editting
      that.update = function() {
        $log.debug('Updating configuration: %O', that.configuration);

        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        that.configuration.productVersionId =
          getFirstId(that.productVersions.selected);
        that.configuration.dependencyIds = gatherIds(that.dependencies.selected);
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

          $state.go('configuration.detail.show', {
            configurationId: that.configuration.id
          }, {
            reload: true
          });
        });
      };

      // Cloning a build configuration
      that.clone = function() {
        that.configuration.$clone().then(function(result) {
          $state.go('configuration.detail.show', {
            configurationId: result.id
          }, {
            reload: true
          });
        });
      };

      // Deleting a build configuration
      that.delete = function() {
        that.configuration.$delete().then(function() {
          $state.go('configuration.list', {}, {
            reload: true,
            inherit: false,
            notify: true
          });
        });
      };

      that.cancel = function(form) {
        if (form) {
          $state.go('configuration.detail.show', {
            configurationId: that.configuration.id
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
    function($log, $stateParams) {
      this.buildConfigurationId = $stateParams.configurationId;
      this.filterBy = {
        buildConfigurationId: $stateParams.configurationId
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

  function getFirstId(array) {
      if (array.length > 0) {
          return array[0].id;
      }
      return null;
  }
})();
