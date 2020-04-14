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
     * The component representing toolbar (see https://patternfly.github.io/angular-patternfly/#!/api/patternfly.toolbars.directive:pfToolbar)
     * compatible with pnc-client library.
     */
    angular.module('pnc.common.components').component('pncToolbar', {
      bindings: {
        /**
         * Object: The page representing filtering page (see filteringPaginator factory) coming from pnc-client
         */
        filteringPage: '<',
        /**
         * Array: The fields representing individual select box filtering options, this configuration will extend PatternFly Toolbar configuration
         */
        filteringFields: '<',
        /**
         * Array: The fields representing individual select box sortingFields options, this configuration will extend PatternFly Toolbar configuration
         */
        sortingFields: '<',
        /**
         * Array: The configuration of the sorting, this configuration will extend "currentField" and "isAscending" of PatternFly Toolbar configuration
         */
        sortingConfigs: '<',
        /**
         * Object: Optional config object for toolbar level action buttons:
         *
         * Valid properties:
         * .primaryActions - (Array<Object>) List of primary actions to display on the toolbar. Valid object properties:
         *    .name - (String) The name of the action, displayed on the button
         *    .title - (String) Optional title, used for the tooltip
         *    .actionFn - (function(action)) Function to invoke when the action selected
         *    .isDisabled - (Boolean) set to true to disable the action
         * .moreActions - (Array<Object>) List of secondary actions to display on the toolbar action pulldown menu. Valid object properties:
         *    .name - (String) The name of the action, displayed on the button
         *    .title - (String) Optional title, used for the tooltip
         *    .actionFn - (function(action)) Function to invoke when the action selected
         *    .isDisabled - (Boolean) set to true to disable the action
         *    .isSeparator - (Boolean) set to true if this is a placehodler for a separator rather than an action
         * .actionsInclude - (Boolean) set to true if using the actions transclude to add custom action buttons (only available if using Angular 1.5 or later)
         */
        actionsConfig: '<?'
      },
      templateUrl: 'common/components/pnc-toolbar/pnc-toolbar.html',
      controller: ['pfFilterAdaptor', Controller]
    });

    function Controller(pfFilterAdaptor) {
      var $ctrl = this;

      // -- Controller API --
      $ctrl.pfToolbarConfig = null;

      // --------------------

      $ctrl.$onInit = () => {
        $ctrl.adaptor = pfFilterAdaptor($ctrl.filteringPage);

        $ctrl.pfToolbarConfig = {
          isTableView: true,
          filterConfig: {
            fields: $ctrl.filteringFields,
            showTotalCountResults: false,
            appliedFilters: [],
            onFilterChange: $ctrl.adaptor.onFilterChange
          }
        };

        if ($ctrl.sortingFields) {
          $ctrl.pfToolbarConfig.sortConfig = {
            fields: $ctrl.sortingFields,
            onSortChange: $ctrl.adaptor.onSortChange,
            currentField: $ctrl.sortingConfigs.field,
            isAscending: $ctrl.sortingConfigs.asc
          };
        }

        if ($ctrl.actionsConfig) {
          $ctrl.pfToolbarConfig.actionsConfig = $ctrl.actionsConfig;
        }
      };

    }

  })();
