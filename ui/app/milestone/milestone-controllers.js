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

  module.controller('MilestoneDetailController', [
    '$scope',
    '$state',
    '$stateParams',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'distributedArtifacts',
    'performedBuilds',
    function($scope, $state, $stateParams, productDetail, versionDetail, milestoneDetail,
        distributedArtifacts, performedBuilds) {

      var that = this;
      that.product = productDetail;
      that.productVersion = versionDetail;
      that.milestone = milestoneDetail;
      that.distributedArtifacts = distributedArtifacts;
      that.performedBuilds = performedBuilds;
    }
  ]);

  module.controller('MilestoneCreateUpdateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'ProductMilestoneDAO',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'dateUtilConverter',
    function($scope, $state, $stateParams, $log, ProductMilestoneDAO, productDetail,
      versionDetail, milestoneDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;
      that.isUpdating = false;

      that.data = new ProductMilestoneDAO();

      if (milestoneDetail !== null) {
        that.isUpdating = true;
        that.data = milestoneDetail;

        // Remove the prefix
        that.version = that.data.version.substring(versionDetail.version.length + 1);

        // Need to convert from timestamp to date for the datepicker
        that.data.startingDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.startingDate);
        that.data.plannedEndDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.plannedEndDate);
      }

      that.setCurrentMilestone = that.productVersion.currentProductMilestoneId === that.data.id;

      // milestone can be only marked as current, not unmarked
      that.setCurrentMilestoneDisabled = that.setCurrentMilestone;

      that.invalidStartingPlannedEndDates = function(sDate, prDate) {
        if (sDate === undefined || prDate === undefined) {
          return false;
        }
        return sDate >= prDate;
      };

      that.submit = function() {

        that.data.version = versionDetail.version + '.' + that.version; // add the prefix
        that.data.startingDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.startingDate);
        that.data.plannedEndDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.plannedEndDate);
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
                      $state.go('product.detail.version', {
                        productId: productDetail.id,
                        versionId: versionDetail.id
                      }, {
                        reload: true
                      });
                    },
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
                $state.go('product.detail.version', {
                  productId: productDetail.id,
                  versionId: versionDetail.id
                }, {
                  reload: true
                });
              }
            }
          );
        } else {
          if (that.setCurrentMilestone) {
            that.productVersion.currentProductMilestoneId = that.data.id;
          }
          
          that.productVersion.$update().then(function(){
            that.data.$update().then(function() {
              $state.go('product.detail.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {
                reload: true
              });
            });
          });
        }
      };

      dateUtilConverter.initDatePicker($scope);
    }
  ]);

  module.controller('MilestoneCloseController', [
    '$scope',
    '$state',
    '$stateParams',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'dateUtilConverter',
    function($scope, $state, $stateParams, productDetail,
      versionDetail, milestoneDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;

      that.data = milestoneDetail;

      that.submit = function() {

        that.data.endDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.endDate);
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
      };

      dateUtilConverter.initDatePicker($scope);
    }
  ]);

})();
