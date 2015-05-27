'use strict';

(function () {

  var module = angular.module('pnc.milestone');

  /**
   * Because both create and edit use the same form,
   * they have shared code, which is here.
   */
  module.service('BaseMilestoneController', [
    function () {
      this.extend = function (that, PncRestClient, productDetail, versionDetail, MilestoneFormValidator, DatePicker) {

        that.product = productDetail;
        that.productVersion = versionDetail;
        that.setCurrentMilestone = false;

        that.validator = new MilestoneFormValidator();

        that.data = {};
        that.data.startingDate = new DatePicker();
        that.data.plannedReleaseDate = new DatePicker();

        that.convert = function (data) {
          var res = new PncRestClient.Milestone();
          res.version = versionDetail.version + '.' + data.version;
          res.startingDate = data.startingDate.getTimestamp();
          res.plannedReleaseDate = data.plannedReleaseDate.getTimestamp();
          res.productVersionId = versionDetail.id;
          res.id = data.id;
          return res;
        };
      };
    }
  ]);


  module.controller('MilestoneCreateController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'BaseMilestoneController',
    'PncRestClient',
    'Notifications',
    'MilestoneFormValidator',
    'DatePicker',
    'productDetail',
    'versionDetail',
    function ($scope, $state, $stateParams, $log, BaseMilestoneController, PncRestClient,
              Notifications, MilestoneFormValidator,
              DatePicker, productDetail, versionDetail) {

      var that = this;
      BaseMilestoneController.extend(that, PncRestClient, productDetail, versionDetail,
        MilestoneFormValidator, DatePicker);

      that.submit = function () {

        if (!that.validator.isValid(that.data)) {
          return;
        }

        var res = that.convert(that.data);
        res.$saveForProductVersion({versionId: versionDetail.id}).then(
          function (result) {
            /* jshint unused:false */
            Notifications.success('Milestone created');
            $state.go('product.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {reload: true});
          },
          function (response) {
            $log.error('Create product failed: response: %O', response);
            Notifications.error('Action Failed.');
          }
        );
      };
    }
  ]);


  module.controller('MilestoneEditController', [
    '$scope',
    '$state',
    '$stateParams',
    '$log',
    'BaseMilestoneController',
    'PncRestClient',
    'Notifications',
    'MilestoneFormValidator',
    'DatePicker',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    function ($scope, $state, $stateParams, $log, BaseMilestoneController, PncRestClient,
              Notifications, MilestoneFormValidator,
              DatePicker, productDetail, versionDetail, milestoneDetail) {

      var that = this;
      that.milestoneDetail = milestoneDetail;

      BaseMilestoneController.extend(that, PncRestClient, productDetail, versionDetail,
        MilestoneFormValidator, DatePicker);

      that.convertBack = function (data) {
        if (!milestoneDetail.version.startsWith(versionDetail.version + '.')) {
          return null;
        }
        data.version = milestoneDetail.version.substring(versionDetail.version.length + 1);
        data.startingDate.fromTimestamp(milestoneDetail.startingDate);
        data.plannedReleaseDate.fromTimestamp(milestoneDetail.plannedReleaseDate);
        data.id = milestoneDetail.id;
      };

      that.convertBack(that.data);

      that.submit = function () {

        if (!that.validator.isValid(that.data)) {
          return;
        }

        var res = that.convert(that.data);
        res.$update({versionId: versionDetail.id}).then(
          function (result) {
            /* jshint unused:false */
            Notifications.success('Milestone created');
            $state.go('product.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {reload: true});
          },
          function (response) {
            $log.error('Creation of milestone failed: response: %O', response);
            Notifications.error('Creation of milestone failed');
          }
        );
      };
    }
  ]);


  module.factory('MilestoneFormValidator', [function () {

    var Validator = function () {

      /**
       * This structure contains validation violations,
       * i.e. true if something is invalid.
       * This can be accessed in templates via scope.
       */
      this.invalid = {
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

      this.isValid = function (data) {
        this.invalid.version.required = !data.version || !data.version.length;
        this.invalid.version.format = !/^[0-9]/.test(data.version);
        this.invalid.version.any = this.invalid.version.required || this.invalid.version.format;

        this.invalid.startingDate.required = !data.startingDate.date;
        this.invalid.startingDate.any = this.invalid.startingDate.required;

        this.invalid.plannedReleaseDate.required = !data.plannedReleaseDate.date;
        this.invalid.plannedReleaseDate.afterStartingDate = data.plannedReleaseDate.date < data.startingDate.date;
        this.invalid.plannedReleaseDate.any = this.invalid.plannedReleaseDate.required || this.invalid.plannedReleaseDate.afterStartingDate;

        return !this.invalid.version.any && !this.invalid.startingDate.any && !this.invalid.plannedReleaseDate.any;
      };
    };

    return Validator;
  }]);


  module.factory('DatePicker', [function () {

    var DatePicker = function () {
      var that = this;
      this.opened = false;

      this.format = 'yyyy/MM/dd';

      this.today = function () {
        this.date = new Date();
      };


      this.clear = function () {
        this.date = null;
      };

      this.open = function ($event) {
        $event.preventDefault();
        $event.stopPropagation();
        this.opened = true;
      };


      /**
       * Date picker returns date with time set to midnight local time.
       * This function returns timestamp for noon UTC.
       */
      this.getTimestamp = function () {

        var MINUTE = 60 * 1000;
        var HOUR = 60 * MINUTE;

        if (!that.date) {
          return null;
        }

        return that.date.getTime() -
          (that.date.getTimezoneOffset() * MINUTE) +
          (12 * HOUR);
      };

      this.fromTimestamp = function (timestamp) {
        that.date = new Date(timestamp);
      };
    };
    return DatePicker;

  }]);
})();
