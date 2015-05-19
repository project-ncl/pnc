'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  module.controller('MilestoneCreateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'PncRestClient',
    'Notifications',
    'productDetail',
    'versionDetail',
    function ($scope, $state, $stateParams, $log, PncRestClient, Notifications,
              productDetail, versionDetail) {

      var that = this;

      this.product = productDetail;
      this.productVersion = versionDetail;

      this.setCurrentMilestone = false;

      this.data = new PncRestClient.Milestone();

      $scope.validation = {
        version: {
          any: false,
          required: false,
          format: false
        },
        startingDate: {
          any: false,
          required: false
        },
        plannedReleaseDate: {
          any: false,
          required: false
        }
      };

      var validate = function () {
        $scope.validation.version.required = !that.data.version || !that.data.version.length;
        $scope.validation.version.format = !/^[0-9]/.test(that.data.version);
        $scope.validation.version.any = $scope.validation.version.required || $scope.validation.version.format;

        $scope.validation.startingDate.required = !that.data.startingDate;
        $scope.validation.startingDate.any = $scope.validation.startingDate.required;

        $scope.validation.plannedReleaseDate.required = !that.data.plannedReleaseDate;
        $scope.validation.plannedReleaseDate.afterStartingDate = that.data.plannedReleaseDate < that.data.startingDate;
        $scope.validation.plannedReleaseDate.any = $scope.validation.plannedReleaseDate.required || $scope.validation.plannedReleaseDate.afterStartingDate;

        return !$scope.validation.version.any && !$scope.validation.startingDate.any && !$scope.validation.plannedReleaseDate.any;
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

        that.data.version = versionDetail.version + '.' + that.data.version; // add the prefix
        that.data.startingDate = convertToTimestamp(that.data.startingDate);
        that.data.plannedReleaseDate = convertToTimestamp(that.data.plannedReleaseDate);
        that.data.productVersionId = versionDetail.id;

        that.data.$saveForProductVersion({versionId: versionDetail.id}).then(
          function (result) {
            /* jshint unused:false */
            Notifications.success('Milestone created');
            $state.go('product.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {reload:true});
          },
          function (response) {
            $log.error('Create product failed: response: %O', response);
            Notifications.error('Action Failed.');
          }
        );
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
