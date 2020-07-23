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

  angular.module('pnc.common.components').component('pncCorruptedBuildLabel', {
    bindings: {

      /**
       * Object: a Build object to display it's corrupted status.
       */
      build: '<'
    },
    templateUrl: 'common/components/pnc-corrupted-build-label/pnc-corrupted-build-label.html',
    controller: [Controller]
  });


  function Controller() {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.isCorrupted = isCorrupted;

    // --------------------

    function isCorrupted() {
      var attrs = $ctrl.build.attributes;

      return attrs && (
        attrs.POST_BUILD_REPO_VALIDATION === 'REPO_SYSTEM_ERROR' ||
        attrs.PNC_SYSTEM_ERROR           === 'DISABLED_FIREWALL' ||
        attrs.BUILD_PROCESS_ISSUE                                ||
        attrs.BLACKLIST_REASON                                   ||
        attrs.DELETE_REASON
      );
    }
  }

})();
