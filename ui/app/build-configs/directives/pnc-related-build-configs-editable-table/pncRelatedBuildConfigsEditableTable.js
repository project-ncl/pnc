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

  angular.module('pnc.build-configs').component('pncRelatedBuildConfigsEditableTable', {
    bindings: {
      page: '<',
      onRemove: '&',
      onEdit: '&'
    },
    templateUrl: 'build-configs/directives/pnc-related-build-configs-editable-table/pnc-related-build-configs-editable-table.html',
    controller: ['$q', 'modalSelectService', Controller]
  });


  function Controller($q, modalSelectService) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.displayFields = ['name', 'project', 'buildStatus'];
    
    $ctrl.actions = {
      remove: remove
    };

    $ctrl.edit = edit;

    // --------------------


    $ctrl.$onInit = function () {
    };


    function remove(buildConfig) {
      $q.when($ctrl.onRemove()(buildConfig)).then(function () {
        $ctrl.page.refresh();
      });
    }

    function edit() {
      $q.when()
        .then(function () {
          if ($ctrl.page.total === 1) {
            return $ctrl.page.data;
          } else {
            return $ctrl.page.getWithNewSize($ctrl.page.total * $ctrl.page.count).then(function (resp) { return resp.data; });
          }
        })
        .then(function (buildConfigs) {
          return modalSelectService.openForBuildConfigs({
            title: 'Insert / Remove Build Configs',
            buildConfigs: buildConfigs
          }).result;
        })
        .then(function (editedBuildConfigs) {
          $q.when($ctrl.onEdit()(editedBuildConfigs)).then(function () {
            $ctrl.page.refresh();
          });
        });
    }

  }

})();
