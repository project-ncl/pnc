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

  angular.module('pnc.builds').component('pncBuildsDataTable', {
    bindings: {
      page: '<',
      displayFields: '<',
    },
    templateUrl: 'builds/directives/pnc-builds-data-table/pnc-builds-data-table.html',
    controller: ['paginator', Controller]
  });


  function Controller(paginator) {
    var $ctrl = this;
    var DEFAULT_FIELDS = ['status', 'id', 'configurationName', 'startTime', 'endTime', 'username'];

    // -- Controller API --
    

    // --------------------


    $ctrl.$onInit = function () {
      // set if bindings are empty
      $ctrl.displayFields = $ctrl.displayFields || DEFAULT_FIELDS;
      $ctrl.paginator = paginator($ctrl.page);
    };

  }

})();
