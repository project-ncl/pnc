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

      this.products = productList;
    }
  ]);

  module.controller('ProductDetailController', [
    '$log',
    'productDetail',
    'productVersions',
    'PncRestClient',
    function($log, productDetail, productVersions, PncRestClient) {

      var that = this;
      that.product = productDetail;
      that.versions = productVersions;

      // Update a product after editing
      that.update = function() {
        $log.debug('Updating product: %O', that.product);
        that.product.$update();
      };

      // Build wrapper objects
      that.versionMilestones = [];
      that.versionReleases = [];

      // Retrieve all the artifacts of all the build records of the build configurations set
      angular.forEach(that.versions, function(version) {

        PncRestClient.Milestone.getAllForProductVersion({
          versionId: version.id
        }).$promise.then(
          function(results) {
            angular.forEach(results, function(result) {
              that.versionMilestones.push(result);
            });
          }
        );

        PncRestClient.Release.getAllForProductVersion({
          versionId: version.id
        }).$promise.then(
          function(results) {
            angular.forEach(results, function(result) {
              that.versionReleases.push(result);
            });
          }
        );
      });

      that.convertFromTimestamp = function(mSec) {
        var now = new Date();
        return new Date(mSec + (now.getTimezoneOffset() * 60 * 1000) - (12 * 60 * 60 * 1000));
      };

      that.formatDate = function(date) {
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
        var milestoneTooltip = '<strong>' + milestone.version + '</strong>' +
          '<br><br><strong>Phase: </strong> &lt;tbd&gt; <br>' +
          '<strong>Starting date: </strong>' + sDate + '<br>' +
          '<strong>Planned release date: </strong>' + prDate + '<br>' +
          '<strong>Release date: </strong>' + rDate + '<br>';
        return milestoneTooltip;
      };

      that.getReleaseTooltip = function(release) {
        var rDate = '';
        if (release.releaseDate) {
          rDate = that.formatDate(that.convertFromTimestamp(release.releaseDate));
        }
        var milestoneVersion = '';
        angular.forEach(that.versionMilestones, function(versionMilestone) {
          if (versionMilestone.id === release.productMilestoneId) {
            milestoneVersion = versionMilestone.version;
          }
        });
        var releaseTooltip = '<strong>' + release.version + '</strong>' +
          '<br><br><strong>Phase: </strong> &lt;tbd&gt; <br>' +
          '<strong>Release date: </strong>' + rDate + '<br>' +
          '<strong>Released from Milestone: </strong>' + milestoneVersion + '<br>' +
          '<strong>Support Level: </strong>' + release.supportLevel + '<br>';
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
    'PncRestClient',
    function($log, $state, productDetail, versionDetail, buildConfigurationSets,
      buildConfigurations, productReleases, productMilestones, PncRestClient) {

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
        that.version.$update();
      };

      // Update a product version after editing
      that.unreleaseMilestone = function(milestone) {
        $log.debug('Unreleasing milestone: %O', milestone);

        milestone.releaseDate = null;
        milestone.downloadUrl = null;

        milestone.$update({
          versionId: versionDetail.id
        }).then(
          function() {
            $state.go('product.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {
              reload: true
            });
          }
        );
      };

      // Mark Milestone as current in Product Version
      that.markCurrentMilestone = function(milestone) {
        $log.debug('Mark milestone as current: %O', milestone);

        versionDetail.currentProductMilestoneId = milestone.id;

        versionDetail.$update({
            productId: productDetail.id,
            versionId: versionDetail.id
          })
          .then(
            function() {
              $state.go('product.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {
                reload: true
              });
            }
          );
      };

      // Executing a build of a configurationSet
      that.buildConfigSet = function(configSet) {
        $log.debug('**Initiating build of SET: %s**', configSet.name);
        PncRestClient.ConfigurationSet.build({
          configurationSetId: configSet.id
        }, {});
      };

      // Executing a build of a configuration
      that.buildConfig = function(config) {
        $log.debug('**Initiating build of: %O', config.name);

        PncRestClient.Configuration.build({
          configurationId: config.id
        }, {});
      };

      that.getMilestoneVersion = function(milestoneId) {
        var milestoneVersion = '';
        angular.forEach(that.productmilestones, function(versionMilestone) {
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
    function($state, $log, PncRestClient) {

      this.data = new PncRestClient.Product();
      var that = this;

      that.submit = function() {
        that.data.$save().then(function(result) {
          $state.go('product.detail', {
            productId: result.id
          });
        });
      };
    }
  ]);

  module.controller('ProductVersionCreateController', [
    '$state',
    '$log',
    'PncRestClient',
    'productDetail',
    function($state, $log, PncRestClient, productDetail) {

      this.data = new PncRestClient.Version();
      this.product = productDetail;
      var that = this;

      that.submit = function() {
        that.data.productId = that.product.id;
        that.data.version = that.data.version;
        that.data.$save().then(function(result) {
            $state.go('product.detail', {
              productId: result.productId,
            });
          }
        );
      };
    }
  ]);

})();
