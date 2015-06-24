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


  module.controller('ConfigurationDetailController', [
    '$log',
    '$state',
    '$filter',
    'Notifications',
    'PncRestClient',
    'configurationDetail',
    'environmentDetail',
    'projectDetail',
    'productVersions',
    'dependencies',
    'products',
    'configurations',
    function($log, $state, $filter, Notifications, PncRestClient,
             configurationDetail, environmentDetail, projectDetail,
             linkedProductVersions, dependencies, products, configurations) {
      $log.debug('ConfigurationDetailController >> arguments=%O', arguments);

      this.configuration = configurationDetail;
      this.environment = environmentDetail;
      this.project = projectDetail;

      var that = this;

      // Filtering and selection of linked ProductVersions.
      this.products = {
        all: [],
        selected: null
      };

      this.productVersions = {
        selected: linkedProductVersions,
        all: [],

        update: function() {
          $log.debug('productVersions >> update()');
          that.productVersions.all = PncRestClient.Product.getVersions({
            productId: that.products.selected.id
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
        PncRestClient.Product.get({
          productId: linkedProductVersions[0].productId
        }).$promise.then(function(result) {
          $log.debug('result, %O', result);
          that.products.selected = result;
          that.products.all = [that.products.selected];
          that.productVersions.update();
        });
      } else {
        that.products.all = products;
      }


     // Selection of dependencies.
      this.dependencies = {
        selected: dependencies,

        getItems: function($viewValue) {
          return $filter('filter')(configurations, {
            name: $viewValue
          });
        }
      };

      // Executing a build of a configuration
      this.build = function() {
        $log.debug('Initiating build of: %O', this.configuration);


        PncRestClient.Configuration.build({
          configurationId: that.configuration.id }, {}).$promise.then(
            function(result) {
              $log.debug('Initiated Build: %O, result: %O', that.configuration,
                         result);
              Notifications.success('Initiated build of configuration: ' +
                                    that.configuration.name);
            },
            function(response) {
              $log.error('Failed to initiated build: %O, response: %O',
                         that.configuration, response);
              Notifications.error('Could not initiate build of configuration: ' +
                                    that.configuration.name);
            }
          );
      };

      // Update a build configuration after editting
      this.update = function() {
        $log.debug('Updating configuration: %O', this.configuration);

        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        this.configuration.productVersionIds = gatherIds(this.productVersions.selected);
        this.configuration.dependencyIds = gatherIds(this.dependencies.selected);

        this.configuration.$update().then(
          function(result) {
            $log.debug('Update Config: %O, result: %O', that.configuration,
                       result);
            Notifications.success('Configuration updated');
          },
          function(response) {
            $log.error('Update configuration: %O failed, response: %O',
                       that.configuration, response);
            Notifications.error('Configuration update failed');
          }
        );
      };

      // Cloning a build configuration
      this.clone = function() {
        this.configuration.$clone().then(function(result) {
          $log.debug('Clone Configuration: %O Successful Result: %O',
               that.configuration, result);

          $state.go('configuration.detail', { configurationId: result.id });
          Notifications.success('Configuration cloned');
        },
        function(response) {
          $log.error('Clone configuration: %O failed, response: %O',
                     that.configuration, response);
          Notifications.error('Configuration clone failed');
        });
      };

      // Deleting a build configuration
      this.delete = function() {
        this.configuration.$delete().then(
          // Success
          function (result) {
            $log.debug('Delete Config: %O success result: %O',
                       that.configuration, result);
            Notifications.success('Configuration deleted');
            $state.go('configuration.list', {}, { reload: true, inherit: false,
                      notify: true });
          },
          // Failure
          function (response) {
            $log.error('Delete configuration: %O failed, response: %O',
                       that.configuration, response);
            Notifications.error('Configuration deletion failed');
          }
        );
      };
    }
  ]);

  module.controller('ConfigurationSidebarController', [
    '$log',
    '$scope',
    '$stateParams',
    'PncRestClient',
    'buildRecordList',
    'runningBuildRecordList',
    'BuildProgressService',
    'BuildRecordNotifications',
    'Notifications',
    function ($log, $scope, $stateParams, PncRestClient, buildRecordList, runningBuildRecordList,
              BuildProgressService, BuildRecordNotifications, Notifications) {
      $log.debug('ConfigurationSidebarController >> arguments=%O', arguments);

      BuildProgressService.track($scope, 'runningBuildRecordList', function () {
        return PncRestClient.Running.query().$promise;
      }, [
        BuildProgressService.BUILD_RECORD_FILTER.WITH_BUILD_CONFIGURATION($stateParams.configurationId),
        BuildProgressService.BUILD_RECORD_FILTER.IS_IN_PROGRESS()
      ], BuildProgressService.BUILD_RECORD_UPDATER);


      BuildProgressService.track($scope, 'buildRecords', function () {
        return PncRestClient.Record.getAllForConfiguration({
          configurationId: $stateParams.configurationId
        }).$promise;
      }, [
        BuildProgressService.BUILD_RECORD_FILTER.WITH_BUILD_CONFIGURATION($stateParams.configurationId),
        BuildProgressService.BUILD_RECORD_FILTER.IS_FINISHED()
      ], BuildProgressService.BUILD_RECORD_UPDATER);


      BuildRecordNotifications.listen(function (record) {
        switch (record.status) {
          case 'SUCCESS':
            Notifications.success('Build #' + record.id + ' finished successfully.');
            break;
          case 'FAILED':
          case 'UNSTABLE':
          case 'ABORTED':
          case 'CANCELLED':
          case 'SYSTEM_ERROR':
          case 'UNKNOWN':
            Notifications.error('Build #' + record.id + ' finished with problems (' + record.status + ').');
        }
      });
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
