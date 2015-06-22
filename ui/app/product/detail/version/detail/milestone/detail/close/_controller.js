'use strict';

(function () {

  var module = angular.module('pnc.milestone');

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
            $state.go('product.detail.version.detail', {
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
