/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  /**
   * The dumb component representing Build Status displaying information like build status icon, 
   * start / end time or started by user for given Build Record or Build Group Record.
   * 
   * @example 
   * <pnc-build-status build-record="buildRecord" is-loaded="isLoaded"></pnc-build-status>
   */
  angular.module('pnc.common.components').component('pncBuildStatus', {
    bindings: {
      /**
       * Object: The BuildRecord to display the status for.
       */
      buildRecord: '<?',
      /**
       * Object: The BuildGroupRecord to display the status for.
       */
      buildGroupRecord: '<?',
      /**
       * Object: Truthy or falsy object indicating whether data request is finished or not.
       */
      isLoaded: '<',
      /**
       * Object: Truthy of falsy object indicating whether click should be propagated or not. 
       * Sometimes propagated clicks can cause side effects that should be stopped.
       */
      stopPropagation: '<'
    },
    templateUrl: 'common/components/pnc-build-status/pnc-build-status.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;

    $ctrl.$onInit = function() {
      $ctrl.item = $ctrl.buildRecord ? $ctrl.buildRecord : $ctrl.buildGroupRecord;
    };

    $ctrl.$onChanges = function(changedBindings) {
      if (changedBindings.buildRecord) {
        $ctrl.item = $ctrl.buildRecord;
      } else if (changedBindings.buildGroupRecord) {
        $ctrl.item = $ctrl.buildGroupRecord;
      }
    };
    
  }

})();
