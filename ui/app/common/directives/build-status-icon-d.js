'use strict';

(function () {

  var module = angular.module('pnc.common.directives');

  /**
   * @ngdoc directive
   * @restrict E
   * @example <build-status-icon status="status"></build-status-icon>
   * @author Jakub Senko
   */
  module.directive('buildStatusIcon', function () {
    return {
      scope: {
        status: '='
      },
      templateUrl: 'common/directives/views/build-status-icon-d.html'
    };
  });

})();
