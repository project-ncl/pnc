/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.release');

  module.controller('ReleaseCreateUpdateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'ProductReleaseDAO',
    'ProductMilestoneDAO',
    'productDetail',
    'versionDetail',
    'releaseDetail',
    'dateUtilConverter',
    function($scope, $state, $stateParams, $log, ProductReleaseDAO, ProductMilestoneDAO,
      productDetail, versionDetail, releaseDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;
      that.versionMilestones = [];
      that.usedVersionMilestoneIds = [];
      that.supportLevels = [];

      that.isUpdating = false;
      that.data = new ProductReleaseDAO();

      if (releaseDetail !== null) {
        that.isUpdating = true;
        that.data = releaseDetail;
        that.productMilestoneId = releaseDetail.productMilestoneId;

        // Remove the prefix
        that.version = that.data.version.substring(versionDetail.version.length + 1);

        // Need to convert from timestamp to date for the datepicker
        that.data.releaseDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.releaseDate);
      }

      // I need to gather the existing Releases, as Milestone can be associated with only one Release at the most
      ProductReleaseDAO.getAllForProductVersion({
        versionId: that.productVersion.id
      }, {}).then(
        function(results) {
          angular.forEach(results, function(result) {
            that.usedVersionMilestoneIds.push(result.productMilestoneId);
          });

          // Only Milestones that are not yet used in this Product Version will be listed
          ProductMilestoneDAO.getAllForProductVersion({
            versionId: that.productVersion.id
          }, {}).then(
            function(results) {
              angular.forEach(results, function(result) {
                if (that.usedVersionMilestoneIds.indexOf(result.id) === -1) {
                  that.versionMilestones.push(result);
                }
                if (that.productMilestoneId && result.id === that.productMilestoneId) {
                  that.productMilestoneVersion = result.version;
                }
              });
            }
          );
        }
      );

      ProductReleaseDAO.getAllSupportLevel({
        versionId: that.productVersion.id
      }, {}).then(
        function(results) {
          that.supportLevels = results;
        }
      );

      that.submit = function() {

        that.data.version = versionDetail.version + '.' + that.version; // add the prefix
        that.data.releaseDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.releaseDate);
        that.data.productVersionId = versionDetail.id;
        that.data.productMilestoneId = parseInt(that.productMilestoneId);

        // Distinguish between release creation and update
        if (!that.isUpdating) {
          that.data.$save().then(
            function() {
              $state.go('product.detail.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {
                reload: true
              });
            }
          );
        } else {
          that.data.$update().then(
            function() {
              $state.go('product.detail.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {
                reload: true
              });
            }
          );
        }
      };

      dateUtilConverter.initDatePicker($scope);
    }
  ]);

})();
