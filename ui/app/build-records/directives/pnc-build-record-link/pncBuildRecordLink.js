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

  angular.module('pnc.build-records').component('pncBuildRecordLink', {
    bindings: {
      buildRecord: '<',
      shortLink: '@'
    },
    transclude: true,
    templateUrl: 'build-records/directives/pnc-build-record-link/pnc-build-record-link.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;

    // -- Controller API --


    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.linkText = getLinkText();
    };

    function getLinkText() {
      if ($ctrl.shortLink === 'true') {
        return '#' + $ctrl.buildRecord.id;
      } else {
        return $ctrl.buildRecord.buildConfigurationAudited.name + '#' + $ctrl.buildRecord.id;
      }
    }
  }

})();
