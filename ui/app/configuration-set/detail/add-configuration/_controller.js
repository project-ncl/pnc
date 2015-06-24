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

  module.controller('ConfigurationSetAddConfigurationController', [
    '$log',
    '$state',
    'configurationSetDetail',
    'projects',
    'PncRestClient',
    'Notifications',
    function($log, $state, configurationSetDetail, projects, PncRestClient, Notifications) {
      $log.debug('ConfigurationSetAddConfigurationController >> this=%O, products=%O', this, projects);

      var self = this;
      self.configurationSetDetail = configurationSetDetail;
      self.shouldFilter = false;
      self.projects = projects;
      self.configurations = [];
      self.selectedConfiguration = {};

      self.getBuildConfigurations = function(projectId) {
        $log.debug('**Getting build configurations of Project: %0**', projectId);

        if (projectId) {
          PncRestClient.Configuration.getAllForProject({
            projectId: projectId
          }).$promise.then(
            function(result) {
              self.configurations = result;
              if (result) {
                self.data.configurationId = result[0].id;
              }
            }
          );
        }
        else {
          self.configurations = [];
        }
      };

      self.submit = function() {
        // Retrieve all the last builds (based on ID, not date) of all the build configurations
        angular.forEach(self.configurations, function(configuration){

          if (configuration.id === parseInt(self.data.configurationId)) {
            self.selectedConfiguration = configuration;
            return;
          }
        });

        if (self.selectedConfiguration) {
          PncRestClient.ConfigurationSet.addConfiguration({
            configurationSetId: self.configurationSetDetail.id
          }, self.selectedConfiguration).$promise.then(
            function(result) {
              $log.debug('Configuration added to Configuration Set: %s', result);
              Notifications.success('Configuration added to Configuration Set');
              var params = { configurationSetId: self.configurationSetDetail.id };
              $state.go('configuration-set.detail', params, { reload: true, inherit: false,
                notify: true });
            },
            function(response) {
              $log.error('Build Configuration adding failed: response: %O', response);
              Notifications.error('Configuration addition to Configuration Set failed');
            }
          );
        }
      };
    }
  ]);

})();
