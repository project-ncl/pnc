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

  angular.module('pnc.common.select-modals').component('addBuildConfigWidget', {
    bindings: {
      project: '<',
      onAdd: '&'
    },
    templateUrl: 'common/select-modals/build-config-multi-select/add-build-config-widget.html',
    controller: ['Project', Controller]
  });


  function Controller(Project) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.select = select;
    $ctrl.add = add;

    $ctrl.buildConfigs = [];

    $ctrl.config = {
     selectItems: false,
     multiSelect: false,
     dblClick: false,
     selectionMatchProp: 'id',
     showSelectBox: false,
    };
    $ctrl.actionButtons = [
      {
        name: 'Add',
        title: 'Add this Build Config',
        include: 'button-add-right', // <-- Template for the action button -- defined within this component's template.
        actionFn: function (action, object) {
          $ctrl.add(object);
        }
      }
    ];

    // --------------------


    function fetchBuildConfigs(projectId) {
      Project.queryBuildConfigurations({ id: projectId }).$promise.then(function (page) {
        $ctrl.buildConfigs = page.data || [];
      });
    }

    function select(item) {
      fetchBuildConfigs(item.id);
    }

    function add(buildConfig) {
      $ctrl.onAdd({ buildConfig: buildConfig});
    }

  }

})();
