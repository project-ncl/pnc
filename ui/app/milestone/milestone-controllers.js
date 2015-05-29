'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  module.controller('MilestoneCreateUpdateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'PncRestClient',
    'Notifications',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'dateUtilConverter',
    function ($scope, $state, $stateParams, $log, PncRestClient, Notifications,
              productDetail, versionDetail, milestoneDetail, dateUtilConverter) {

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
         that.version = that.data.version.substring(versionDetail.version.length+1);

         // Need to convert from timestamp to date for the datepicker
         that.data.startingDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.startingDate);
         that.data.plannedReleaseDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.plannedReleaseDate);
      }

      that.invalidStartingPlannedReleaseDates = function (sDate, prDate) {
        if (sDate === undefined || prDate === undefined) {
          return false;
        }
        return sDate >= prDate;
      };

      that.submit = function () {

        that.data.version = versionDetail.version + '.' + that.version; // add the prefix
        that.data.startingDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.startingDate);
        that.data.plannedReleaseDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.plannedReleaseDate);
        that.data.productVersionId = versionDetail.id;

        // Distinguish between release creation and update
        if (!that.isUpdating) {

          that.data.$saveForProductVersion({versionId: versionDetail.id}).then(
            function () {
              Notifications.success('Milestone created');

              if (that.setCurrentMilestone) {
                // Mark Milestone as current in Product Version
                versionDetail.currentProductMilestoneId = that.data.id;
                versionDetail.$update({ productId: productDetail.id, versionId: versionDetail.id})
                 .then(
                   function() {
                     $state.go('product.version', {
                       productId: productDetail.id,
                       versionId: versionDetail.id
                     }, {reload: true});
                   },
                   function(response) {
                     $log.error('Update version failed, response: %O', response);
                     $state.go('product.version', {
                       productId: productDetail.id,
                       versionId: versionDetail.id
                     }, {reload: true});
                   }
                 );
              }
              else {
                $state.go('product.version', {
                  productId: productDetail.id,
                  versionId: versionDetail.id
                }, {reload: true});
              }
            },
            function (response) {
              $log.error('Creation of Milestone: response: %O', response);
              Notifications.error('Creation of milestone failed');
            }
          );
        }
        else {
          that.data.$update({versionId: versionDetail.id}).then(
            function() {
              Notifications.success('Milestone updated');
              $state.go('product.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {reload:true});
            },
            function(response) {
              $log.error('Update milestone failed, response: %O', response);
              Notifications.error('Milestone update failed');
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
    'Notifications',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'dateUtilConverter',
    function ($scope, $state, $stateParams, $log, PncRestClient, Notifications,
              productDetail, versionDetail, milestoneDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;

      that.data = milestoneDetail;

      that.submit = function () {

        that.data.releaseDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.releaseDate);
        that.data.$update({versionId: versionDetail.id}).then(
          function() {
            Notifications.success('Milestone released');
            $state.go('product.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {reload:true});
          },
          function(response) {
            $log.error('Release milestone failed, response: %O', response);
            Notifications.error('Milestone release failed');
          }
        );
      };

      dateUtilConverter.initDatePicker($scope);
    }
  ]);

})();
