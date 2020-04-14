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

  angular.module('pnc.common.select-modals').component('addBuildConfigWidget', {
    bindings: {
      project: '<',
      onAdd: '&'
    },
    templateUrl: 'common/select-modals/build-config-multi-select/add-build-config-widget.html',
    controller: ['ProjectResource', '$scope', Controller]
  });


  function Controller(ProjectResource, $scope) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.select = select;
    $ctrl.add = add;
    $ctrl.addSelected = addSelected;
    $ctrl.selectAll = selectAll;
    $ctrl.selectAllState = true;

    $ctrl.items = [];
    $ctrl.checkedItems = [];

    $ctrl.config = {
     selectItems: false,
     multiSelect: false,
     dblClick: false,
     selectionMatchProp: 'buildConfig.id',
     showSelectBox: true,
     onCheckBoxChange: handleCheckBoxChange
    };
    $ctrl.actionButtons = [
      {
        name: 'Add',
        title: 'Add this Build Config',
        include: 'button-add-right', // <-- Template for the action button -- defined within this component's template.
        actionFn: function (action, item) {
          $ctrl.add(item);
        }
      }
    ];

    // --------------------


    function fetchBuildConfigs(projectId) {
      ProjectResource.queryBuildConfigurations({ id: projectId }).$promise.then(function (page) {
        $ctrl.items = [];
        $ctrl.selectAllState = true;
        // keep buildConfig data separately from other item attributes as pf-list-view modifies item attributes (for example 'selected')
        for (var i = 0; i < page.data.length; i++) {
          $ctrl.items.push({
            buildConfig: page.data[i]
          });
        }
      });
    }

    function select(item) {
      fetchBuildConfigs(item.id);
    }

    function add(item) {
      $ctrl.onAdd(item);
    }

    function handleCheckBoxChange(item) {
      if (item.selected) {
        $ctrl.checkedItems.push(item);
      } else {
        var index = $ctrl.checkedItems.findIndex(function (x) { 
          return item.buildConfig.id === x.buildConfig.id; 
        });
        if (index > -1) {
          $ctrl.checkedItems.splice(index, 1);
        }
      }
    }

    function addSelected() {
      for (var i = 0; i < $ctrl.checkedItems.length; i++) {
        $ctrl.onAdd($ctrl.checkedItems[i]);
      }
    }

    /**
     * Select All is not natively supported by PatternFly.
     */
    function selectAll(select) {
      var $checkboxes = $('#build-configs-' + $scope.$id + ' .list-view-pf-checkbox input');

      $checkboxes.each(function(){
        var $that = $(this);
        if ((select && !$that.is(':checked')) || (!select && $that.is(':checked'))) {
          // keep UI responsive when there is a lot of items to select
          setTimeout(function() { 
            // simulate click so that PatternFly click handlers are executed
            $that.click(); 
          }, 0);
        }
      });

      $ctrl.selectAllState = !$ctrl.selectAllState;
    }

  }

})();
