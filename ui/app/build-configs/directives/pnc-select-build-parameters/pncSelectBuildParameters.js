(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncSelectBuildParameters', {
    require: {
      ngModel: '?ngModel'
    },
    templateUrl: 'build-configs/directives/pnc-select-build-parameters/pnc-select-build-parameters.html',
    controller: ['$scope', '$log', 'utils', 'BuildConfiguration', Controller]
  });

  function Controller($scope, $log, utils, BuildConfiguration) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.knownKeys = undefined;
    $ctrl.params = {};

    $ctrl.addParam = addParam;
    $ctrl.removeParam = removeParam;
    $ctrl.hasParams = hasParams;

    // --------------------


    $ctrl.$onInit = function () {

      $ctrl.knownKeys = BuildConfiguration.getSupportedGenericParameters().then(function (params) {
        return formatSupportedParams(params);
      });

      $scope.$watch(function () {
        return utils.digestStrings($ctrl.params);
      }, function () {
        $ctrl.ngModel.$setViewValue($ctrl.params);
      });

      $ctrl.ngModel.$render = function () {
        $ctrl.params = angular.isDefined($ctrl.ngModel.$viewValue) ? $ctrl.ngModel.$viewValue : {};
      };
    };

    function addParam(key, value) {
      $ctrl.params[key] = value;
    }

    function removeParam(key) {
      delete $ctrl.params[key];
    }

    function hasParams() {
      return Object.keys($ctrl.params).length > 0;
    }

    function formatSupportedParams(params) {
      var result = [];

      Object.keys(params).forEach(function (key) {
        result.push({
          name: key,
          description: params[key]
        });
      });

      return result;
    }

  }
})();
