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

  var module = angular.module('pnc.milestone');

  module.controller('MilestoneCreateUpdateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'PncRestClient',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'dateUtilConverter',
    function($scope, $state, $stateParams, $log, PncRestClient, productDetail,
      versionDetail, milestoneDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;
      that.setCurrentMilestone = false;
      that.isUpdating = false;

      that.data = new PncRestClient.Milestone();

      if (milestoneDetail !== null) {
        that.isUpdating = true;
        that.data = milestoneDetail;

        // Remove the prefix
        that.version = that.data.version.substring(versionDetail.version.length + 1);

        // Need to convert from timestamp to date for the datepicker
        that.data.startingDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.startingDate);
        that.data.plannedReleaseDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.plannedReleaseDate);
      }

      that.invalidStartingPlannedReleaseDates = function(sDate, prDate) {
        if (sDate === undefined || prDate === undefined) {
          return false;
        }
        return sDate >= prDate;
      };

      that.submit = function() {

        that.data.version = versionDetail.version + '.' + that.version; // add the prefix
        that.data.startingDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.startingDate);
        that.data.plannedReleaseDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.plannedReleaseDate);
        that.data.productVersionId = versionDetail.id;

        // Distinguish between milestone creation and update
        if (!that.isUpdating) {

          that.data.$save().then(
            function() {

              if (that.setCurrentMilestone) {
                // Mark Milestone as current in Product Version
                versionDetail.currentProductMilestoneId = that.data.id;
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
                    },
                    function() {
                      $state.go('product.version', {
                        productId: productDetail.id,
                        versionId: versionDetail.id
                      }, {
                        reload: true
                      });
                    }
                  );
              } else {
                $state.go('product.version', {
                  productId: productDetail.id,
                  versionId: versionDetail.id
                }, {
                  reload: true
                });
              }
            }
          );
        } else {
          that.data.$update().then(
            function() {
              $state.go('product.version', {
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

  module.controller('MilestoneCloseController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'PncRestClient',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'dateUtilConverter',
    function($scope, $state, $stateParams, $log, PncRestClient, productDetail,
      versionDetail, milestoneDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;

      that.data = milestoneDetail;

      that.submit = function() {

        that.data.releaseDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.releaseDate);
        that.data.$update().then(
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

      dateUtilConverter.initDatePicker($scope);
    }
  ]);

})();
