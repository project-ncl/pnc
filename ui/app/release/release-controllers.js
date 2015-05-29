'use strict';

(function () {

  var module = angular.module('pnc.release');

  module.controller('ReleaseCreateUpdateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'PncRestClient',
    'Notifications',
    'productDetail',
    'versionDetail',
    'releaseDetail',
    'dateUtilConverter',
    function ($scope, $state, $stateParams, $log, PncRestClient, Notifications,
              productDetail, versionDetail, releaseDetail, dateUtilConverter) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;
      that.versionMilestones = [];
      that.usedVersionMilestoneIds = [];      
      that.supportLevels = [];

      that.isUpdating = false;
      that.data = new PncRestClient.Release();

      if (releaseDetail !== null) {
         that.isUpdating = true;
         that.data = releaseDetail;
         that.productMilestoneId = releaseDetail.productMilestoneId;

         // Remove the prefix
         that.version = that.data.version.substring(versionDetail.version.length+1);

         // Need to convert from timestamp to date for the datepicker
         that.data.releaseDate = dateUtilConverter.convertFromTimestampNoonUTC(that.data.releaseDate);
      }

      // I need to gather the existing Releases, as Milestone can be associated with only one Release at the most
      PncRestClient.Release.getAllForProductVersion({
        versionId: that.productVersion.id
      }, {}).$promise.then(
        function(results) {
          angular.forEach(results, function(result){
            that.usedVersionMilestoneIds.push(result.productMilestoneId);            
          });

          // Only Milestones that are not yet used in this Product Version will be listed
          PncRestClient.Milestone.getAllForProductVersion({
            versionId: that.productVersion.id
          }, {}).$promise.then(
            function(results) {
              angular.forEach(results, function(result){
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

      PncRestClient.Release.getAllSupportLevel({
        versionId: that.productVersion.id
      }, {}).$promise.then(
        function(results) {
          angular.forEach(results, function(result){
            that.supportLevels.push(result);
          });
        }
      );

      that.submit = function () {

        that.data.version = versionDetail.version + '.' + that.version; // add the prefix
        that.data.releaseDate = dateUtilConverter.convertToTimestampNoonUTC(that.data.releaseDate);
        that.data.productVersionId = versionDetail.id;
        that.data.productMilestoneId = parseInt(that.productMilestoneId);

        // Distinguish between release creation and update
        if (!that.isUpdating) {
          that.data.$saveForProductVersion({versionId: versionDetail.id}).then(
            function (result) {
              /* jshint unused:false */
              Notifications.success('Release created');
              $state.go('product.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {reload:true});
            },
            function (response) {
              $log.error('Creation of Release: response: %O', response);
              Notifications.error('Creation of release failed');
            }
          );
        }
        else {
          that.data.$update().then(
            function(result) {
              /* jshint unused:false */
              Notifications.success('Release updated');
              $state.go('product.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {reload:true});
            },
            function(response) {
              $log.error('Update release: %O failed, response: %O',
                       that.data, response);
              Notifications.error('Release update failed');
            }
          );
        }
      };

      dateUtilConverter.initDatePicker($scope);
    }
  ]);

})();
