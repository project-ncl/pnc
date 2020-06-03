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

  /**
   * A component for displaying log data from bifrost.
   *
   * See Also: https://github.com/project-ncl/bifrost
   */
  angular.module('pnc.common.bifrost').component('pncBifrostLogViewer', {
    bindings: {
      /**
       * The prefixFilters parameter to pass to bifrost
       */
      prefixFilters: '@',
      /**
       * the matchFilters parameter to pass to bifrost
       */
      matchFilters: '@'
    },
    templateUrl: 'common/bifrost/pnc-bifrost-log-viewer.html',
    controller: ['bifrostConfig', Controller]
  });

  function Controller(bifrostConfig) {
    const $ctrl = this;

    // -- Controller API --


    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.bifrostHost = bifrostConfig.getBifrostHost();
    };

  }
})();
