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
    function($state, $log, $filter, BuildConfigurationDAO, ProductDAO, Notifications, environments,
      projects, products, configurations) {

      var that = this;

      this.data = new BuildConfigurationDAO(); // TODO is this correct?
      this.environments = environments;
      this.projects = projects;


      this.submit = function() {
        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        that.data.productVersionIds = gatherIds(that.productVersions.selected);
        that.data.dependencyIds = gatherIds(that.dependencies.selected);

        that.data.$save().then(function(result) {
          $state.go('configuration.detail.show', {
            configurationId: result.id
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

          if (that.products.selected) {
            ProductDAO.getVersions({
              productId: that.products.selected.id
            }).then(function(data) {
              that.productVersions.all = data;

              // TOFIX - Ugly but quick - avibelli
              data.forEach(function ( prodVers ) {
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
      this.dependencies = {
        selected: [],

        getItems: function($viewValue) {
          return $filter('filter')(configurations, {
            name: $viewValue
          });
        }
      };

      that.reset = function(form) {
        if (form) {
          form.$setPristine();
          form.$setUntouched();
          that.products.selected = null;
          that.productVersions.all = [];
          that.productVersions.selected = [];
          that.dependencies.selected = [];
          that.data = new BuildConfigurationDAO();
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
    'configurationDetail',
    'environments',
    'environmentDetail',
    'linkedProductVersions',
    'dependencies',
    'allProducts',
    'configurations',
    function($log, $state, $filter, Notifications, ProductDAO, BuildConfigurationDAO,
      configurationDetail, environments, environmentDetail,
      linkedProductVersions, dependencies, allProducts, configurations) {

      var that = this;

      that.configuration = configurationDetail;
      that.environments = environments;
      that.allProducts = allProducts;

      // We need to set environment from existing environments collections to be able to preselect
      // dropdown element when editing
      that.environment = findEnvironment(that.configuration.environment.id, that.environments);

      // Filtering and selection of linked ProductVersions.
      that.products = {
        all: allProducts,
        selected: null
      };

      // Could not make it work in a nicer way (i.e. via cachedGetter) - avibelli
      that.allProductsMaps = {};
      that.allProducts.forEach(function ( prod ) {
          that.allProductsMaps[ prod.id ] = prod;
      });

      // TOFIX - Ugly but quick - avibelli
      that.allProductNamesMaps = {};
      linkedProductVersions.forEach(function ( prodVers ) {
          that.allProductNamesMaps[ prodVers.id ] = that.allProductsMaps[ prodVers.productId ].name + ' - ';
      });

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

      // Bootstrap products, depending on whether the BuildConfiguration
      // already has a ProductVersion attached.
      if (linkedProductVersions && linkedProductVersions.length > 0) {

        ProductDAO.get({
          productId: linkedProductVersions[0].productId
        }).$promise.then(function(result) {
          that.products.selected = result;
          that.productVersions.update();
        });
      }

      // Selection of dependencies
      that.dependencies = {
        selected: dependencies,

        getItems: function($viewValue) {
          return $filter('filter')(configurations, {
            name: $viewValue
          });
        }
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

        that.configuration.$update().then(function() {
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

  function findEnvironment(id, environments) {
    for (var i = 0; i < environments.length; i++) {
      if (id === environments[i].id) {
        return environments[i];
      }
    }
  }

  function gatherIds(array) {
    var result = [];
    for (var i = 0; i < array.length; i++) {
      result.push(array[i].id);
    }
    return result;
  }

})();
