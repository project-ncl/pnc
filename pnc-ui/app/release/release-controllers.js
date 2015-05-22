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
    function ($scope, $state, $stateParams, $log, PncRestClient, Notifications,
              productDetail, versionDetail, releaseDetail) {

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
         var now = new Date();
         var newTimestamp = that.data.releaseDate + (now.getTimezoneOffset() * 60 * 1000) - (12 * 60 * 60 * 1000);
         now.setTime(newTimestamp);
         that.data.releaseDate = now;
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

      $scope.validation = {
        version: {
          any: false,
          required: false,
          format: false
        },
        releaseDate: {
          any: false,
          required: false
        },
        downloadUrl: {
          any: false,
          required: false
        }
      };

      var validate = function () {
        $scope.validation.version.required = !that.version || !that.version.length;
        $scope.validation.version.format = !/^[0-9]/.test(that.version);
        $scope.validation.version.any = $scope.validation.version.required || $scope.validation.version.format;

        $scope.validation.releaseDate.required = !that.data.releaseDate;
        $scope.validation.releaseDate.any = $scope.validation.releaseDate.required;

        $scope.validation.downloadUrl.required = !that.data.downloadUrl;
        $scope.validation.downloadUrl.any = $scope.validation.downloadUrl.required;
        

        return !$scope.validation.version.any && !$scope.validation.releaseDate.any && !$scope.validation.downloadUrl.any;
      };

      /**
       * Date picker returns date with time set to midnight
       * local time. This function sets the time to noon UTC.
       */
      var convertToTimestamp = function (date) {
        return date.getTime() -
          (date.getTimezoneOffset() * 60 * 1000) + // remove local timezone offset
          (12 * 60 * 60 * 1000); // change from midnight to noon
      };

      that.submit = function () {
        if (!validate()) {
          return;
        }

        that.data.version = versionDetail.version + '.' + that.version; // add the prefix
        that.data.releaseDate = convertToTimestamp(that.data.releaseDate);
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

      $scope.opened = [];

      $scope.today = function () {
        $scope.dt = new Date();
      };
      $scope.today();

      $scope.clear = function () {
        $scope.dt = null;
      };

      $scope.open = function ($event, id) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.opened[id] = true;
      };

      $scope.format = 'yyyy/MM/dd';
    }
  ]);

})();
