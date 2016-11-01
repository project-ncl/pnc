/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.common.select-modals').component('buildConfigMultiSelect', {
    bindings: {
      modalCtrl: '<'
    },
    templateUrl: 'common/select-modals/build-config-multi-select/build-config-multi-select.html',
    controller: Controller
  });

  function Controller() {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.save = save;
    $ctrl.cancel = cancel;
    $ctrl.onRemove = onRemove;
    $ctrl.onAdd = onAdd;

    $ctrl.title = $ctrl.modalCtrl.config.title;
    $ctrl.buildConfigs = angular.copy($ctrl.modalCtrl.config.buildConfigs);

    // --------------------


    function save() {
      $ctrl.modalCtrl.$close($ctrl.buildConfigs);
    }

    function cancel() {
      $ctrl.modalCtrl.$dismiss();
    }

    function getBcIndex(buildConfig) {
      return $ctrl.buildConfigs.findIndex(function (x) { return buildConfig.id === x.id; });
    }

    function onAdd(buildConfig) {
      if (getBcIndex(buildConfig) < 0) {
        $ctrl.buildConfigs.push(buildConfig);
      }
    }

    function onRemove(buildConfig) {
      var index = getBcIndex(buildConfig);

      if (index > -1) {
        $ctrl.buildConfigs.splice(index, 1);
      }
    }
  }

})();
