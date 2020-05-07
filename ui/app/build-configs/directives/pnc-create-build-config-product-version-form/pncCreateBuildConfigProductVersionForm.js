(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncCreateBuildConfigProductVersionForm', {
    templateUrl: 'build-configs/directives/pnc-create-build-config-product-version-form/pnc-create-build-config-product-version-form.html',
    require: {
      ngModel: 'ngModel'
    },
    controller: ['ProductResource', Controller]
  });

  function Controller(ProductResource) {
    var $ctrl = this;

    // -- Controller API --

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.ngModel.$render = function () {
        $ctrl.data = $ctrl.ngModel.$modelValue;
      };
    };

    $ctrl.$doCheck = function () {
      if ($ctrl.ngModel.$viewValue !== $ctrl.data) {
        $ctrl.ngModel.$setViewValue($ctrl.data);
      }
    };
    /*Check version data and remove it if product is changed or not selected*/
    $ctrl.checkVersionData = function () {
      if (!$ctrl.data.product || ($ctrl.data.version && $ctrl.data.product.id !== $ctrl.data.version.product.id)) {
        $ctrl.data.version = null;
      }
    };
  }
})();
