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
(function () {
  'use strict';

  var module = angular.module('pnc.build-groups');

  module.controller('ConfigurationSetListController', [
    '$log',
    '$state',
    'configurationSetList',
    function($log, $state, configurationSetList) {
      this.buildconfigurationsets = configurationSetList;
    }
  ]);

  module.controller('ConfigurationSetCreateController', [
    '$log',
    '$state',
    'products',
    'BuildConfigurationSetDAO',
    function($log, $state, products, BuildConfigurationSetDAO) {
      var self = this;
      self.data = new BuildConfigurationSetDAO();
      self.products = products;

      self.productVersions = {
        selected: []
      };

      self.submit = function() {
        self.data.$save().then(
          function() {
            if (!_.isEmpty(self.productVersions.selected)) {
              var params = {
                productId: parseInt(self.productVersions.selected[0].productId),
                versionId: parseInt(self.productVersions.selected[0].id)
              };
              $state.go('product.detail.version', params, {
                reload: true,
                inherit: false,
                notify: true
              });
            } else {
              $state.go('build-groups.list');
            }
          }
        );
      };

      self.reset = function(configurationSetForm) {
        if (configurationSetForm) {
          self.productVersions.selected = [];
          self.data = new BuildConfigurationSetDAO();
          configurationSetForm.$setPristine();
          configurationSetForm.$setUntouched();
        }
      };
    }
  ]);

  module.controller('ConfigurationSetAddConfigurationController', [
    '$log',
    '$state',
    'configurationSetDetail',
    'projects',
    'BuildConfigurationDAO',
    'BuildConfigurationSetDAO',
    function($log, $state, configurationSetDetail, projects,
        BuildConfigurationDAO, BuildConfigurationSetDAO) {

      var self = this;
      self.configurationSetDetail = configurationSetDetail;
      self.shouldFilter = false;
      self.projects = projects;
      self.configurations = [];
      self.selectedConfiguration = {};

      self.getBuildConfigurations = function(projectId) {
        $log.debug('**Getting build configurations of Project: %0**', projectId);

        if (projectId) {
          BuildConfigurationDAO.getAllForProject({
            projectId: projectId
          }).then(
            function(result) {
              self.configurations = result;
              if (result) {
                self.data.configurationId = result[0].id;
              }
            }
          );
        } else {
          self.configurations = [];
        }
      };

      self.reset = function() {
        self.data = null;
        self.getBuildConfigurations(null);
      };

      self.submit = function() {
        // Retrieve all the last builds (based on ID, not date) of all the build configurations
        angular.forEach(self.configurations, function(configuration) {

          if (configuration.id === parseInt(self.data.configurationId)) {
            self.selectedConfiguration = configuration;
            return;
          }
        });

        if (self.selectedConfiguration) {
          BuildConfigurationSetDAO.addConfiguration({
            configurationSetId: self.configurationSetDetail.id
          }, self.selectedConfiguration).$promise.then(
            function() {
              var params = {
                configurationSetId: self.configurationSetDetail.id
              };
              $state.go('build-groups.detail', params, {
                reload: true,
                inherit: false,
                notify: true
              });
            }
          );
        }
      };
    }
  ]);

  module.controller('ConfigurationSetDetailController', [
    '$log',
    '$state',
    'BuildRecordDAO',
    'BuildConfigurationSetDAO',
    'ProductVersionDAO',
    'ProductVersion',
    'configurationSetDetail',
    'configurations',
    'records',
    'productVersion',
    'previousState',
    'modalSelectService',
    function($log, $state, BuildRecordDAO, BuildConfigurationSetDAO, ProductVersionDAO, ProductVersion,
        configurationSetDetail, configurations, records, productVersion, previousState, modalSelectService) {

      var self = this;
      self.set = configurationSetDetail;
      self.configurations = configurations;
      self.records = records;
      self.productVersion = productVersion;

      // Build a wrapper object that contains all is needed (to avoid 'ng-repeat' in the pages)
      self.buildRecordArtifactsWO = [];

      // Retrieve all the artifacts of all the build records of the build configurations set
      angular.forEach(records, function(record) {

        BuildRecordDAO.getArtifacts({
          recordId: record.id
        }).then(
          function(results) {

            var buildRecordArtifactWO = {};
            var artifacts = [];

            // For each artifact found, add it to a temp list
            angular.forEach(results, function(result) {
              artifacts.push(result);
            });

            // Add the artifacts temp list to the WO
            buildRecordArtifactWO.artifacts = artifacts;

            // Add the build record to the WO
            buildRecordArtifactWO.buildRecord = record;

            angular.forEach(configurations, function(configuration) {
              if (configuration.id === record.buildConfigurationId) {
                // Add the build configuration to the WO
                buildRecordArtifactWO.buildConfiguration = configuration;
              }
            });

            self.buildRecordArtifactsWO.push(buildRecordArtifactWO);
          }
        );
      });

      self.forceBuild = function() {
        $log.info('Initiating forced build of group: %', self.set.name);

        BuildConfigurationSetDAO.forceBuild({
          configurationSetId: self.set.id
        }, {});
      };

      self.build = function() {
        $log.info('Initiating build of group: %s', self.set.name);

        BuildConfigurationSetDAO.build({
          configurationSetId: self.set.id
        }, {});
      };

      // Update a build configuration set after editing
      self.update = function() {
        $log.debug('Updating BuildConfigurationSet: %s', JSON.stringify(self.set));
        self.set.$update(
        ).then(
          function() {
            $state.go('build-groups.detail', {
              configurationSetId: self.set.id
            }, {
              reload: true
            });
          }
        );
      };

      self.getProductVersions = function(productId) {
        if (productId) {
          ProductVersionDAO.getAllForProduct({
            productId: productId
          }).$promise.then(
            function(result) {
              self.productVersions = result;
              if (result) {
                self.data.productVersionId = result[0].id;
              }
            }
          );
        } else {
          self.productVersions = [];
        }
      };

      // Deleting a build configuration set
      self.delete = function() {
        self.set.$delete().then(function() {
          // Attempt to fo to previous state
          $state.go(previousState.Name, previousState.Params);
        });
      };

      self.getFullProductVersionName = function () {
        if (_.isEmpty(self.productVersion)) {
          return 'None';
        }

        return self.productVersion.productName + ': ' + self.productVersion.version;
      };

      self.linkWithProductVersion = function () {
        var modal = modalSelectService.openForProductVersion({
          title: 'Link ' + self.set.name + ' with a product version',
          selected: self.set.productVersion
        });

        modal.result.then(function (result) {
          self.set.productVersionId = result.id;
          self.set.$update().then(function () {
            self.productVersion = ProductVersion.get({ id: result.id });
          });
        });
      };

      self.unlinkFromProductVersion = function () {
        self.set.productVersionId = null;
        self.set.$update().then(function () {
          self.productVersion = null;
        });
      };
    }
  ]);

})();
