(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncEnterBuildParameters', {
    bindings: {
      onAdd: '&',
      onRemove: '&',
      knownKeys: '<'
    },
    templateUrl: 'build-configs/directives/pnc-select-build-parameters/pnc-enter-build-parameters.html',
    controller: ['$q', '$filter', 'utils', Controller]
  });

  function Controller($q, $filter, utils) {
    var $ctrl = this;


    // -- Controller API --

    $ctrl.add = add;
    $ctrl.clear = clear;
    $ctrl.searchKnownKeys = searchKnownKeys;

    // --------------------


    function add() {
      $ctrl.onAdd({ key: $ctrl.key, value: $ctrl.value });
      $ctrl.clear();
    }

    function clear() {
      $ctrl.key = undefined;
      $ctrl.value = undefined;
    }

    function searchKnownKeys($viewValue) {
      var normalized = $q.when($ctrl.knownKeys);

      if (utils.isEmpty($viewValue)){
        return normalized;
      }

      return normalized.then(function (params) {
        return $filter('filter')(params, { name: $viewValue });
      });

    }

  }
})();
