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

  angular.module('pnc.builds').component('pncBuildDetailAlignmentLogPage', {
    bindings: {
      build: '<',
      alignmentLog: '<'
    },
    templateUrl: 'builds/detail/alignment-log/pnc-build-detail-alignment-log-page.html',
    controller: ['REST_BASE_REST_URL', 'BUILD_PATH', Controller]
  });


  function Controller(REST_BASE_REST_URL, BUILD_PATH) {
    const $ctrl = this;

    // -- Controller API --
    $ctrl.logUrl = null;
    $ctrl.logFileName = null;
    $ctrl.sshCredentialsBtn = {
      clicked: false
    };


    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.logUrl = REST_BASE_REST_URL + BUILD_PATH.replace(':id', $ctrl.build.id) + '/logs/align';
      $ctrl.logFileName = $ctrl.build.id + '_' + $ctrl.build.buildConfigRevision.name + '_' + $ctrl.build.status + '_alignment-log.txt';
    };

  }

})();
