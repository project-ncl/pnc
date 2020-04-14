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

  angular.module('pnc.common.components').component('pncBuildStatusIcon', {
    bindings: {
      /**
       * Object: The Build to display the status icon for.
       */
      build: '<',
      /**
       * Boolean: Whether to display additional warnings such as corrupted 
       * Build indicator, defaults to false.
       */
      noWarnings: '@',
    },
    templateUrl: 'common/components/pnc-build-status-icon/pnc-build-status-icon.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.isCorrupted = false;
    $ctrl.isTemporary = false;

    // --------------------

    function isCorrupted(build) {
      var attrs = build.attributes;

      return attrs && (
        attrs.POST_BUILD_REPO_VALIDATION === 'REPO_SYSTEM_ERROR' ||
        attrs.PNC_SYSTEM_ERROR           === 'DISABLED_FIREWALL'
      );
    }

    function isTemporary(build) {
      return build.temporaryBuild;
    }

    $ctrl.$onChanges = function (changes) {
      if (changes.build && changes.build.currentValue) {
        $ctrl.isCorrupted = isCorrupted(changes.build.currentValue);
        $ctrl.isTemporary = isTemporary(changes.build.currentValue);
      }
    };
  }

})();
