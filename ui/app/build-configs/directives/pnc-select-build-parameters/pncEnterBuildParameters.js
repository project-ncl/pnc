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
