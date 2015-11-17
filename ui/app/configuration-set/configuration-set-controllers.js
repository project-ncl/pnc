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

  var module = angular.module('pnc.configuration-set');

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
    'ProductVersionDAO',
    function($log, $state, products, BuildConfigurationSetDAO, ProductVersionDAO) {
      var self = this;
      self.data = new BuildConfigurationSetDAO();
      self.products = products;
      self.productVersions = [];

      if (parseInt($state.params.productId) !== -1) {
        self.selectedProductId = parseInt($state.params.productId);

        ProductVersionDAO.getAllForProduct({
            productId: self.selectedProductId
          }).then(
            function(result) {
              self.productVersions = result;
              self.data.productVersionId = parseInt($state.params.versionId);
            }
          );
      }

      self.getProductVersions = function(productId) {
        $log.debug('**Getting productVersions of Product: %0**', productId);

        if (productId) {
          ProductVersionDAO.getAllForProduct({
            productId: productId
          }).then(
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

      self.submit = function() {
        self.data.$save().then(
          function() {
            if (self.data.productVersionId) {
              var params = {
                productId: parseInt(self.selectedProductId),
                versionId: self.data.productVersionId
              };
              $state.go('product.detail.version', params, {
                reload: true,
                inherit: false,
                notify: true
              });
            } else {
              $state.go('configuration-set.list');
            }
          }
        );
      };

      self.reset = function(configurationSetForm) {
        if (configurationSetForm) {
          configurationSetForm.$setPristine();
          configurationSetForm.$setUntouched();
          self.data = new BuildConfigurationSetDAO();
          self.selectedProductId = '';
          self.getProductVersions(null);
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
              $state.go('configuration-set.detail', params, {
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
    'configurationSetDetail',
    'configurations',
    'records',
    'previousState',
    function($log, $state, BuildRecordDAO, BuildConfigurationSetDAO, ProductVersionDAO,
             configurationSetDetail, configurations, records, previousState) {
      var self = this;

      $log.debug('ConfigurationSetDetailController >> this=%O', self);
      self.set = configurationSetDetail;
      self.configurations = configurations;
      self.lastBuildRecords = [];

      // Retrieve all the last builds (based on ID, not date) of all the build configurations
      angular.forEach(configurations, function(configuration) {

        BuildRecordDAO.getLatestForConfiguration({
          configurationId: configuration.id
        }).then(
          function(result) {
            if (result[0]) {
              self.lastBuildRecords.push(result[0]);
            }
          }
        );
      });

      self.records = records;

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
        $log.debug('**Initiating FORCED build of SET: %s**', self.set.name);

        BuildConfigurationSetDAO.forceBuild({
          configurationSetId: self.set.id
        }, {});
      };

      self.build = function() {
        $log.debug('**Initiating build of SET: %s**', self.set.name);

        BuildConfigurationSetDAO.build({
          configurationSetId: self.set.id
        }, {});
      };

      // Update a build configuration set after editing
      self.update = function() {
        $log.debug('Updating configuration-set: %O', self.set);
        self.set.$update(
        ).then(
          function() {
            $state.go('configuration-set.detail', {
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
    }
  ]);

})();
