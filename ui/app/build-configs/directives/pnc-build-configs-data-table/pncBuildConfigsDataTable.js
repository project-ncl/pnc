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

  angular.module('pnc.build-configs').component('pncBuildConfigsDataTable', {
    bindings: {
      page: '<',
      displayFields: '<',
      hideActions: '@',
      onRemove: '&',
      onEdit: '&',
      refreshBuildConfigs: '&'
    },
    templateUrl: 'build-configs/directives/pnc-build-configs-data-table/pnc-build-configs-data-table.html',
    controller: ['$scope', '$q', 'modalSelectService', 'filteringPaginator', 'SortHelper', Controller]
  });


  function Controller($scope, $q, modalSelectService, filteringPaginator, SortHelper) {
    var $ctrl = this;
    const DEFAULT_FIELDS = ['name', 'project', 'buildStatus'];

    const PAGE_NAME = 'buildConfigsList';

    // -- Controller API --

    $ctrl.actions = {
      remove: remove
    };

    $ctrl.edit = edit;

    // --------------------


    $ctrl.$onInit = function () {
      // set if bindings are empty
      $ctrl.displayFields = $ctrl.displayFields || DEFAULT_FIELDS;

      $ctrl.filterPage = filteringPaginator($ctrl.page);

      $ctrl.filterFields = [
        {
          id: 'name',
          title: 'Name',
          placeholder: 'Filter by Name',
          filterType: 'text'
        }
      ];

      $ctrl.sortingFields = [{
        id: 'name',
        title: 'Name'
      },
      {
        id: 'project.name',
        title: 'Project'
      },
      {
        id: 'creationTime',
        title: 'Created'
      },
      {
        id: 'modificationTime',
        title: 'Modified'
      }];

      $ctrl.sortingConfigs = SortHelper.getSortConfig(PAGE_NAME);


      $ctrl.toolbarActions = generateToolbarActions();
    };

    function remove(buildConfig) {
      $q.when($ctrl.onRemove()(buildConfig)).then(() => {
        $ctrl.refreshBuildConfigs()($ctrl.page.data.filter(bc => bc.id !== buildConfig.id));
        $ctrl.filterPage.refresh();
      });
    }

    function edit() {
      $q.when()
        .then(function () {
          if ($ctrl.page.total === 1) {
            return $ctrl.page.data;
          } else {
            return $ctrl.page.getWithNewSize($ctrl.page.total * $ctrl.page.size).then(function (resp) { return resp.data; });
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
            $ctrl.refreshBuildConfigs()(editedBuildConfigs);
            $ctrl.filterPage.refresh();
          });
        });
    }

    function generateToolbarActions() {
      const actions = [];
      if ($ctrl.onEdit()) {
        actions.push({
          name: 'Edit',
          title: 'Add or remove build configs to the list',
          actionFn: edit
        });
      }

      return actions.length > 0 ? { primaryActions: actions } : undefined;
    }
  }

})();
