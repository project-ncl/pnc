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

  var module = angular.module('pnc.product');

  module.controller('ProductVersionController', [
    '$log',
    '$state',
    'productDetail',
    'versionDetail',
    'buildConfigurationSets',
    'buildConfigurations',
    'productReleases',
    'productMilestones',
    'Notifications',
    'PncRestClient',
    function ($log, $state, productDetail, versionDetail, buildConfigurationSets, buildConfigurations, productReleases, productMilestones, Notifications, PncRestClient) {
      $log.debug('VersionDetailController >> this=%O, productDetail=%O, ' +
                 'versionDetail=%O, buildConfigurationSets=%0', this, productDetail, versionDetail, buildConfigurationSets);

      var that = this;
      that.product = productDetail;
      that.version = versionDetail;
      that.buildconfigurationsets = buildConfigurationSets;
      that.buildconfigurations = buildConfigurations;
      that.productreleases = productReleases;
      that.productmilestones = productMilestones;

      // Update a product version after editing
      that.update = function() {
        $log.debug('Updating product version: %O', that.version);

        that.version.$update().then(
          function(result) {
            $log.debug('Update Product Version: %O, result: %O', that.version,
                       result);
            Notifications.success('Product Version updated');
          },
          function(response) {
            $log.error('Update product version: %O failed, response: %O',
                       that.version, response);
            Notifications.error('Product Version update failed');
          }
        );
      };

      // Update a product version after editing
      that.unreleaseMilestone = function(milestone) {
        $log.debug('Unreleasing milestone: %O', milestone);

        milestone.releaseDate = null;
        milestone.downloadUrl = null;

        milestone.$update({versionId: versionDetail.id}).then(
          function() {
            Notifications.success('Milestone unreleased');
            $state.go('product.detail.version.detail', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {reload:true});
          },
          function(response) {
            $log.error('Unrelease milestone failed, response: %O', response);
            Notifications.error('Milestone unrelease failed');
          }
        );
      };

      // Mark Milestone as current in Product Version
      that.markCurrentMilestone = function(milestone) {
        $log.debug('Mark milestone as current: %O', milestone);

        versionDetail.currentProductMilestoneId = milestone.id;

        versionDetail.$update({ productId: productDetail.id, versionId: versionDetail.id})
        .then(
          function() {
            Notifications.success('Milestone updated');
            $state.go('product.detail.version.detail', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {reload:true});
          },
          function(response) {
            $log.error('Update milestone failed, response: %O', response);
            Notifications.error('Milestone update failed');
          }
        );
      };

      // Executing a build of a configurationSet
      that.buildConfigSet = function(configSet) {
        $log.debug('**Initiating build of SET: %s**', configSet.name);

        PncRestClient.ConfigurationSet.build({
          configurationSetId: configSet.id }, {}).$promise.then(
            function(result) {
              $log.debug('Initiated Build: %O, result: %O', configSet,
                         result);
              Notifications.success('Initiated build of Configuration Set: ' +
                                    configSet.name);
            },
            function(response) {
              $log.error('Failed to initiated build: %O, response: %O',
                         configSet, response);
              Notifications.error('Could not initiate build of Configuration Set: ' +
                                    configSet.name);
            }
        );
      };

      // Executing a build of a configuration
      that.buildConfig = function(config) {
        $log.debug('**Initiating build of: %O', config.name);

        PncRestClient.Configuration.build({
          configurationId: config.id }, {}).$promise.then(
            function(result) {
              $log.debug('Initiated Build: %O, result: %O', config,
                         result);
              Notifications.success('Initiated build of configuration: ' +
                                    config.name);
            },
            function(response) {
              $log.error('Failed to initiated build: %O, response: %O',
                         config, response);
              Notifications.error('Could not initiate build of configuration: ' +
                                    config.name);
            }
          );
      };

      that.getMilestoneVersion = function(milestoneId) {
         var milestoneVersion = '';
         angular.forEach(that.productmilestones, function(versionMilestone){
            if (versionMilestone.id === milestoneId) {
               milestoneVersion = versionMilestone.version;
            }
         });
         return milestoneVersion;
      };
    }
  ]);

})();
