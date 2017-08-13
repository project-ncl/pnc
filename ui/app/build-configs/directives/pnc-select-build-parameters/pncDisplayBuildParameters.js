(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncDisplayBuildParameters', {
    bindings: {
      params: '<',
      onEdit: '&',
      onRemove: '&'
    },
    templateUrl: 'build-configs/directives/pnc-select-build-parameters/pnc-display-build-parameters.html',
    controller: Controller
  });

  function Controller() {
    var $ctrl = this,
        editMap = {};

    // -- Controller API --

    // --------------------

    function copyParam(key) {
      $ctrl.currentParams[key] = angular.copy($ctrl.params[key]);
    }

    function copyParams() {
      $ctrl.currentParams = angular.copy($ctrl.params);
    }

    $ctrl.$onInit = function () {
      copyParams();
    };

    $ctrl.$doCheck = function () {
      if (angular.isUndefined($ctrl.currentParams)) {
        return;
      }

      Object.keys($ctrl.params).forEach(function (key) {
        if (!$ctrl.currentParams.hasOwnProperty(key)) {
          copyParam(key);
        }
      });
    };

    $ctrl.cancel = function (key) {
      copyParam(key);
      $ctrl.setEditOff(key);
    };

    $ctrl.update = function (key, value) {
      $ctrl.onEdit({ key: key, value: value });
      $ctrl.setEditOff(key);
    };

    $ctrl.remove = function (key) {
      $ctrl.onRemove({ key: key });
      delete $ctrl.currentParams[key];
      delete editMap[key];
    };

    $ctrl.setEditOn = function (key) {
      editMap[key] = true;
    };

    $ctrl.setEditOff = function (key) {
      editMap[key] = false;
    };

    $ctrl.toggleEdit = function (key) {
      editMap[key] = !!!editMap[key];
    };

    $ctrl.isEditOn = function (key) {
      return !!editMap[key];
    };

  }
})();
