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

  angular.module('pnc.group-configs').component('pncGroupConfigsDataTable', {
    bindings: {
      page: '<',
      fetchSelected: '&',
      // displayFields: '<',
      // hideActions: '@',
      // onRemove: '&',
      onEdit: '&'
    },
    templateUrl: 'group-configs/components/pnc-group-configs-data-table/pnc-group-configs-data-table.html',
    controller: ['$q', 'filteringPaginator', 'SortHelper', 'modalSelectService', Controller]
  });


  function Controller($q, filteringPaginator, SortHelper, modalSelectService) {
    const $ctrl = this;

    const PAGE_NAME = 'groupConfigsDataTable';

    // -- Controller API --

    $ctrl.edit = edit;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.filterPage = filteringPaginator($ctrl.page);

      $ctrl.displayFields = ['name', 'buildStatus'];

      $ctrl.filterFields = [{
        id: 'name',
        title: 'Name',
        placeholder: 'Filter by Name',
        filterType: 'text'
      }];

      $ctrl.sortingFields = [{
        id: 'name',
        title: 'Name'
      }];

      $ctrl.sortingConfigs = SortHelper.getSortConfig(PAGE_NAME);

      $ctrl.toolbarActions = generateToolbarActions();

    };

    function edit() {
      $ctrl.fetchSelected().then(selected => {
        modalSelectService.openForBuildGroups({
          title: 'Add or Remove Group Configs from Product Version',
          selected: selected
        }).result.then(res => console.log('Modal Result: %O', res));
      });
    }

    function generateToolbarActions() {
      const actions = [];
      if ($ctrl.onEdit()) {
        actions.push({
          name: 'Edit',
          title: 'Add or remove Group Configs to the list',
          actionFn: edit
        });
      }

      return actions.length > 0 ? { primaryActions: actions } : undefined;
    }
  }

})();
