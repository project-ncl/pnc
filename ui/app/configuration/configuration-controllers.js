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

  module.controller('ConfigurationCreateController', [
    '$state',
    '$log',
    '$filter',
    'BuildConfigurationDAO',
    'ProductDAO',
    'Notifications',
    'environments',
    'projects',
    'products',
    'configurations',
    'configurationSetList',
    function($state, $log, $filter, BuildConfigurationDAO, ProductDAO, Notifications, environments,
      projects, products, configurations, configurationSetList) {

      var that = this;

      that.data = new BuildConfigurationDAO();
      that.environments = environments;
      that.projects = projects;
      that.configurations = configurations;
      that.configurationSetList = configurationSetList;

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

          $state.go('configuration.detail.show', {
            configurationId: result.id
          });
        });
      };

      // Filtering and selection of linked ProductVersions.
      that.products = {
        all: products,
        selected: null
      };

      that.allProductsMaps = {};
      that.allProductNamesMaps = {};

      _.each(that.products.all, function(prod) {
        that.allProductsMaps[ prod.id ] = prod;
      });

      that.productVersions = {
        selected: [],
        all: [],

        update: function() {

          if (that.products.selected) {
            ProductDAO.getVersions({
              productId: that.products.selected.id
            }).then(function(data) {
              that.productVersions.all = data;

              // TOFIX - Ugly but quick - avibelli
              _.each(data, function(prodVers) {
                that.allProductNamesMaps[ prodVers.id ] = that.allProductsMaps[ prodVers.productId ].name + ' - ';
              });
            });
          }
        },
        getItems: function($viewValue) {
          return $filter('filter')(that.productVersions.all, {
            version: $viewValue
          });
        }
      };

      // Selection of dependencies.
      that.dependencies = {
        selected: []
      };

      // Selection of Build Group Configs.
      that.buildgroupconfigs = {
        selected: []
      };

      // Selection of Projects
      that.projectSelection = {
        selected: []
      };

      // Selection of Environments
      that.environmentSelection = {
        selected: []
      };

      that.reset = function(form) {
        if (form) {
          that.products.selected = null;
          that.productVersions.all = [];
          that.productVersions.selected = [];
          that.dependencies.selected = [];
          that.buildgroupconfigs.selected = [];
          that.projectSelection.selected = [];
          that.environmentSelection.selected = [];
          that.data = new BuildConfigurationDAO();

          form.$setPristine();
          form.$setUntouched();
        }
      };
    }
  ]);



  module.controller('ConfigurationDetailController', [
    '$log',
    '$state',
    '$filter',
    'Notifications',
    'ProductDAO',
    'BuildConfigurationDAO',
    'BuildConfigurationSetDAO',
    'configurationDetail',
    'environments',
    'environmentDetail',
    'linkedProductVersions',
    'dependencies',
    'allProducts',
    'configurations',
    'configurationSetList',
    'linkedConfigurationSetList',
    function($log, $state, $filter, Notifications, ProductDAO, BuildConfigurationDAO, BuildConfigurationSetDAO,
      configurationDetail, environments, environmentDetail,
      linkedProductVersions, dependencies, allProducts, configurations, configurationSetList, linkedConfigurationSetList) {

      var that = this;

      that.configuration = configurationDetail;
      that.environment = _.isUndefined(environmentDetail.content[0]) ? undefined : environmentDetail.content[0];
      that.environments = environments;
      that.allProducts = allProducts;
      that.configurations = configurations;
      that.configurationSetList = configurationSetList;

      // Filtering and selection of linked ProductVersions.
      that.products = {
        all: allProducts,
        selected: null
      };

      that.productVersions = {
        selected: linkedProductVersions,
        all: [],

        update: function() {
          ProductDAO.getVersions({
            productId: that.products.selected.id
          }).then(function(data) {
            that.productVersions.all = data;
          });
        },
        getItems: function($viewValue) {
          return $filter('filter')(that.productVersions.all, {
            version: $viewValue
          });
        }
      };

      that.allProductsMaps = {};
      that.allProductNamesMaps = {};

      _.each(that.allProducts, function(prod) {
        that.allProductsMaps[ prod.id ] = prod;

        // Bootstrap products, depending on whether the BuildConfiguration
        // already has a ProductVersion attached.
        if (!_.isUndefined(linkedProductVersions) && linkedProductVersions.length > 0) {
          if (linkedProductVersions[0].productId === prod.id) {
            that.products.selected = prod;
            that.productVersions.update();
          }
        }
      });

      _.each(linkedProductVersions, function(prodVers) {
        that.allProductNamesMaps[ prodVers.id ] = that.allProductsMaps[ prodVers.productId ].name + ' - ';
      });

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
        that.configuration.productVersionIds =
          gatherIds(that.productVersions.selected);
        that.configuration.dependencyIds = gatherIds(that.dependencies.selected);

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

      that.updateEnvironment = function() {
        that.configuration.environment.id = that.environment.id;
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

})();
