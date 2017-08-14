(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncSelectBuildConfigs', {
    templateUrl: 'build-configs/directives/pnc-select-build-configs/pnc-select-build-configs.html',
    bindings: {
      onAdd: '&',
      onRemove: '&',
      onChange: '&',
      initialValues: '<'
    },
    require: {
      ngModel: '?ngModel'
    },
    controller: ['utils', Controller]
  });

  function Controller(utils) {
    var $ctrl = this,

        lastHashCode,

        listConfig = {
         selectItems: false,
         multiSelect: false,
         dblClick: false,
         selectionMatchProp: 'id',
         showSelectBox: false,
       },

       listActionButtons = [
         {
           name: 'Remove',
           title: 'Remove this dependency',
           actionFn: function (action, object) {
             $ctrl.remove(object);
           }
         }
       ];


    // -- Controller API --

    $ctrl.buildConfigs = [];
    $ctrl.listConfig = listConfig;
    $ctrl.listActionButtons = listActionButtons;
    $ctrl.add = add;
    $ctrl.remove = remove;

    // --------------------


    $ctrl.$onInit = function () {
      if (angular.isDefined($ctrl.initialValues)) {
        $ctrl.buildConfigs = angular.copy($ctrl.initialValues);
      }

      if ($ctrl.ngModel) {
        $ctrl.ngModel.$render = function () {
          $ctrl.buildConfigs = $ctrl.ngModel.$viewValue;
        };
      }
    };

    $ctrl.$doCheck = function () {
      var hashCode = utils.hashCode($ctrl.buildConfigs);

      if (hashCode !== lastHashCode) {
        onChange();
        lastHashCode = hashCode;
      }
    };


    function add(buildConfig) {
      if (indexOf(buildConfig) > -1) {
        return;
      }

      $ctrl.buildConfigs.push(buildConfig);
      $ctrl.buildConfig = undefined;
      if ($ctrl.onAdd) {
        $ctrl.onAdd({ buildConfig: buildConfig });
      }
    }

    function remove(buildConfig) {
      var index = indexOf(buildConfig);

      if (index === -1) {
        return;
      }

      $ctrl.buildConfigs.splice(index, 1);
      if ($ctrl.onRemove) {
        $ctrl.onRemove({ buildConfig: buildConfig });
      }
    }

    function onChange() {
      if ($ctrl.ngModel) {
        $ctrl.ngModel.$setViewValue($ctrl.buildConfigs);
      }
      if ($ctrl.onChange) {
        $ctrl.onChange({ buildConfigs: $ctrl.buildConfigs});
      }
    }

    function indexOf(buildConfig) {
      return $ctrl.buildConfigs.findIndex(function (bc) {
        return bc.id === buildConfig.id;
      });
    }
  }

})();
