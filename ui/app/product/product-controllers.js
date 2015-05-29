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

  module.controller('ProductListController', [
    '$log', '$state', 'productList',
    function($log, $state, productList) {
      $log.debug('ProductListController >> this=%O, productList=%O',
                 this, productList);

      this.products = productList;
    }
  ]);

  module.controller('ProductDetailController', [
    '$log',
    'productDetail',
    'productVersions',
    'Notifications',
    'PncRestClient',
    function ($log, productDetail, productVersions, Notifications, PncRestClient) {

      var that = this;
      that.product = productDetail;
      that.versions = productVersions;

      // Update a product after editing
      that.update = function() {
        $log.debug('Updating product: %O', that.product);

        that.product.$update().then(
          function(result) {
            $log.debug('Update Product: %O, result: %O', that.product,
                       result);
            Notifications.success('Product updated');
          },
          function(response) {
            $log.error('Update product: %O failed, response: %O',
                       that.product, response);
            Notifications.error('Product update failed');
          }
        );
      };

      // Build wrapper objects
      that.versionMilestones = [];
      that.versionReleases = [];

      // Retrieve all the artifacts of all the build records of the build configurations set
      angular.forEach(that.versions, function(version){

          PncRestClient.Milestone.getAllForProductVersion({
              versionId: version.id
          }).$promise.then(
            function (results) {
               angular.forEach(results, function(result){
                 that.versionMilestones.push(result);
               });
            }
          );

          PncRestClient.Release.getAllForProductVersion({
              versionId: version.id
          }).$promise.then(
            function (results) {
               angular.forEach(results, function(result){
                 that.versionReleases.push(result);
               });
            }
          );
      });

      that.convertFromTimestamp = function (mSec) {
        var now = new Date();
        return new Date(mSec + (now.getTimezoneOffset() * 60 * 1000) - (12 * 60 * 60 * 1000));
      };

      that.formatDate = function (date) {
        return date.getFullYear() + '/' + date.getMonth() + '/' + date.getDate();
      };

      that.getMilestoneTooltip = function(milestone) {
        var sDate = '';
        var prDate = '';
        var rDate = '';
        if (milestone.startingDate) {
          sDate = that.formatDate(that.convertFromTimestamp(milestone.startingDate));
        }
        if (milestone.plannedReleaseDate) {
          prDate = that.formatDate(that.convertFromTimestamp(milestone.plannedReleaseDate));
        }
        if (milestone.releaseDate) {
          rDate = that.formatDate(that.convertFromTimestamp(milestone.releaseDate));
        }
        var milestoneTooltip = '<strong>'+milestone.version+'</strong>'+
          '<br><br><strong>Phase: </strong> &lt;tbd&gt; <br>'+
          '<strong>Starting date: </strong>'+sDate+'<br>'+
          '<strong>Planned release date: </strong>'+prDate+'<br>'+
          '<strong>Release date: </strong>'+rDate+'<br>';
        return milestoneTooltip;
      };

      that.getReleaseTooltip = function(release) {
        var rDate = '';
        if (release.releaseDate) {
          rDate = that.formatDate(that.convertFromTimestamp(release.releaseDate));
        }
        var milestoneVersion = '';
         angular.forEach(that.versionMilestones, function(versionMilestone){
            if (versionMilestone.id === release.productMilestoneId) {
               milestoneVersion = versionMilestone.version;
            }
        });
        var releaseTooltip = '<strong>'+release.version+'</strong>'+
          '<br><br><strong>Phase: </strong> &lt;tbd&gt; <br>'+
          '<strong>Release date: </strong>'+rDate+'<br>'+
          '<strong>Released from Milestone: </strong>'+milestoneVersion+'<br>'+
          '<strong>Support Level: </strong>'+release.supportLevel+'<br>';
        return releaseTooltip;
      };
    }
  ]);

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
            $state.go('product.version', {
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

  module.controller('ProductCreateController', [
    '$state',
    '$log',
    'PncRestClient',
    'Notifications',
    function($state, $log, PncRestClient, Notifications) {

      this.data = new PncRestClient.Product();
      var that = this;

      that.submit = function() {
        that.data.$save().then(
          function(result) {
            Notifications.success('Product created');
            $state.go('product.detail', {
              productId: result.id
            });
          },
          function(response) {
            $log.error('Create product failed: response: %O', response);
            Notifications.error('Product creation failed');
          }
        );
      };
    }
  ]);

  module.controller('ProductVersionCreateController', [
    '$state',
    '$log',
    'PncRestClient',
    'Notifications',
    'productDetail',
    function($state, $log, PncRestClient, Notifications, productDetail) {

      $log.debug('ProductVersionCreateController >> this=%O, productDetail=%O, ', this, productDetail);

      this.data = new PncRestClient.Version();
      this.product = productDetail;
      var that = this;

      that.submit = function() {
        that.data.$save({productId: that.product.id }).then(
          function(result) {
            Notifications.success('Product Version created');
            $state.go('product.detail', {
              productId: productDetail.id,
              versionId: result.id
            });
          },
          function(response) {
            $log.error('Create product version failed: response: %O', response);
            Notifications.error('Product Version creation failed');
          }
        );
      };
    }
  ]);

})();
