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

})();
