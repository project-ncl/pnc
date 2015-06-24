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

  module.controller('ConfigurationSetDetailController', [
    '$log',
    '$state',
    'Notifications',
    'PncRestClient',
    'configurationSetDetail',
    'configurations',
    'records',
    'previousState',
    function($log, $state, Notifications, PncRestClient, configurationSetDetail,
             configurations, records, previousState) {
      var self = this;

      $log.debug('ConfigurationSetDetailController >> this=%O', self);
      self.set = configurationSetDetail;
      self.configurations = configurations;
      self.lastBuildRecords = [];

      // Retrieve all the last builds (based on ID, not date) of all the build configurations
      angular.forEach(configurations, function(configuration){

          PncRestClient.Record.getLatestForConfiguration({
              configurationId: configuration.id
          }).$promise.then(
            function (result) {
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
      angular.forEach(records, function(record){

          PncRestClient.Record.getArtifacts({
              recordId: record.id
          }).$promise.then(
            function (results) {

               var buildRecordArtifactWO = {};
               var artifacts = [];

               // For each artifact found, add it to a temp list
               angular.forEach(results, function(result){
                 artifacts.push(result);
               });

               // Add the artifacts temp list to the WO
               buildRecordArtifactWO.artifacts = artifacts;

               // Add the build record to the WO
               buildRecordArtifactWO.buildRecord = record;

               angular.forEach(configurations, function(configuration){
                 if (configuration.id === record.buildConfigurationId) {
                   // Add the build configuration to the WO
                   buildRecordArtifactWO.buildConfiguration = configuration;
                 }
               });

               self.buildRecordArtifactsWO.push(buildRecordArtifactWO);
            }
          );
      });

      self.build = function() {
        $log.debug('**Initiating build of SET: %s**', self.set.name);

        PncRestClient.ConfigurationSet.build({
          configurationSetId: self.set.id }, {}).$promise.then(
            function(result) {
              $log.debug('Initiated Build: %O, result: %O', self.set,
                         result);
              Notifications.success('Initiated build of Configuration Set: ' +
                                    self.set.name);
            },
            function(response) {
              $log.error('Failed to initiated build: %O, response: %O',
                         self.set, response);
              Notifications.error('Could not initiate build of Configuration Set: ' +
                                    self.set.name);
            }
        );
      };

      // Update a build configuration set after editing
      self.update = function() {
        $log.debug('Updating configuration-set: %O', this.set);

        this.set.$update().then(
          function(result) {
            $log.debug('Update Config: %O, result: %O', self.set,
                       result);
            Notifications.success('Configuration Set updated');
          },
          function(response) {
            $log.error('Update set: %O failed, response: %O',
                       self.set, response);
            Notifications.error('Configuration Set update failed');
          }
        );
      };

      self.getProductVersions = function(productId) {
        $log.debug('**Getting productVersions of Product: %0**', productId);

        if (productId) {
          PncRestClient.Version.query({
            productId: productId
          }).$promise.then(
            function(result) {
              self.productVersions = result;
              if (result) {
                self.data.productVersionId = result[0].id;
              }
            }
          );
        }
        else {
          self.productVersions = [];
        }
      };

      self.remove = function(configurationId) {
        $log.debug('**Removing configurationId: %0 from Build Configuration Set: %0**', configurationId, self.set);

        PncRestClient.ConfigurationSet.removeConfiguration({
            configurationSetId: self.set.id,
            configurationId: configurationId
        }).$promise.then(
          // Success
          function (result) {
            $log.debug('Removal of Configuration from Configuration Set: %O success result: %O',
             self.set, result);
            Notifications.success('Configuration removed from Configuration Set');
            var params = { configurationSetId: self.set.id };
            $state.go('configuration-set.detail', params, { reload: true, inherit: false, notify: true });
          },
          // Failure
          function (response) {
            $log.error('Removal of Configuration from Configuration Set: %O failed, response: %O',
             self.set, response);
            Notifications.error('Configuration removal from Configuration Set failed');
          }
        );
      };

      // Deleting a build configuration set
      self.delete = function() {
        self.set.$delete().then(
          // Success
          function (result) {
            $log.debug('Delete Configuration Set: %O success result: %O',
                       self.set, result);
            Notifications.success('Configuration Set deleted');
            // Attempt to fo to previous state
            $state.go(previousState.Name, previousState.Params);
          },
          // Failure
          function (response) {
            $log.error('Delete configuration set: %O failed, response: %O',
                       self.set, response);
            Notifications.error('Configuration Set deletion failed');
          }
        );
      };

    }
  ]);

})();
