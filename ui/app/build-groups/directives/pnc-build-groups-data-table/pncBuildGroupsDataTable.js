/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.build-groups').component('pncBuildGroupsDataTable', {
    bindings: {
      /**
       * page object: the page of build groups to display in the table.
       */
      buildGroupsPage: '<page',
      /**
       * array of strings: Names of table columns to display (see template for possible options)
       */
      displayFields: '<',
      /**
       * 
       */
      onEdit: '&',
      /**
       * 
       */
      onRemove: '&'
    },
    templateUrl: 'build-groups/directives/pnc-build-groups-data-table/pnc-build-groups-data-table.html',
    controller: ['$log', '$q', 'paginator', Controller]
  });


  function Controller($log, $q, paginator) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.page = paginator($ctrl.buildGroupsPage);

    $ctrl.actions = {
      remove: remove
    };

    // --------------------


    $ctrl.$onInit = function () {
    };

    
    function remove(buildGroup) {
      $log.debug('Table action: remove Build Group: %O', buildGroup);
      $q.when($ctrl.onRemove()(buildGroup)).then(function () {
        $ctrl.page.refresh();
      });
    }

  }

})();
