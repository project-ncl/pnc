/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncSelectBuildConfigs', {
    templateUrl: 'build-configs/directives/pnc-select-build-configs/pnc-select-build-configs.html',
    bindings: {
      onAdd: '&',
      onRemove: '&',
      onChange: '&'
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
      if ($ctrl.ngModel) {
        $ctrl.ngModel.$render = function () {
          $ctrl.buildConfigs = $ctrl.ngModel.$viewValue;
        };
      }
    };

    $ctrl.$doCheck = function () {
      var hashCode = utils.hashCode($ctrl.buildConfigs);

      if (hashCode !== lastHashCode && $ctrl.buildConfigs.length) {
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
