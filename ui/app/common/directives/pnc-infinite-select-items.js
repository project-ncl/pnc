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
'use strict';

(function() {

  var module = angular.module('pnc.common.directives');

  /**
   * @ngdoc directive
   * @name pnc.common.directives:pncInfiniteSelectSingle
   * @restrict E
   * @param {string@} singleItem
   * Indicates if the list of selected items is single (selecting a new item will replace the existing one, if any). Defaults to 'false'
   * @param {array=} selected-items
   * An array on the in scope controller that will hold the items selected by
   * the user. The array can be pre-populated to show items that are already
   * selected.
   * @param {string@} display-property
   * The name of the property on the item to search against and display. Defaults to 'name'
   * @param {string=} additional-display-items-by-id
   * The name of the additional property to display
   * @param {array=} items
   * The items to display
   * @param {string=} item-id
   * The ui id of the items (for UI validation)
   * @param {string@} placeholder
   * The placeholder to show in the search text. Defaults to 'Scroll & Filter'
   * @param {string@} infinite-select-id
   * The ui id of the infiniteSelect directive (for UI validation)
   * @param {string@} infinite-select-name
   * The ui name of the infiniteSelect directive (for UI validation)
   * @param {string@} infinite-select-required
   * Indicates if the infinite select value is required (for UI validation). Defaults to 'false'
   * @description
   * A directive that allows users to select multiple options from a list of
   * possible types, using the infinite scroll. The user finds possible items by typing into a input box. 
   * The selected items are presented to the user in a list (one-item list if 'singleItem' is true)
   * The user is given the option to remove items from the list by pressing a
   * cross button next to the selected item.
   * @example
   * <pnc-infinite-select-list
        ng-show="configurationForm.$visible"
        selected-items="detailCtrl.buildgroupconfigs.selected"
        infinite-select-required="false"
        infinite-select-id="input-build-group-configs"
        infinite-select-name="bgConfigId"
        placeholder="Scroll & Filter Build Group Configs..."
        items="detailCtrl.configurationSetList">
     </pnc-infinite-select-list>
   * @author Andrea Vibelli
   */
  module.directive('pncInfiniteSelectItems', function() {
    return {
      restrict: 'E',
      scope: {
        singleItem: '@',
    	selectedItems: '=',
        displayProperty: '@',
        additionalDisplayItemsById: '=',
        items: '=',
        itemId: '=',
        placeholder: '@',
        infiniteSelectId: '@',
        infiniteSelectRequired: '@'
      },
      templateUrl: 'common/directives/views/pnc-infinite-select-items.html',
      controller: [
        '$log',
        '$scope',
        function($log, $scope) {

          var findInArray = function(obj, array) {
            for (var i = 0; i < array.length; i++) {
              if (angular.equals(obj.id, array[i].id)) {
                return i;
              }
            }
            return -1;
          };

       	  var PLACEHOLDER = 'Scroll & Filter';
          $scope.placeholder = _.isUndefined($scope.placeholder) ? PLACEHOLDER : $scope.placeholder;
          $scope.infiniteSelectRequired = _.isUndefined($scope.infiniteSelectRequired) ? false : $scope.infiniteSelectRequired;
          $scope.singleItem = convertToBooleanString($scope.singleItem);

       	  var DEFAULT_DISPLAY_PROPERTY = 'name';
          $scope.displayProperty = _.isUndefined($scope.displayProperty) ? DEFAULT_DISPLAY_PROPERTY : $scope.displayProperty;

          // If selectedItems is not empty, populate the $scope.itemId and $scope.searchText, otherwise
          // if 'infiniteSelectRequired' is true, the form would be invalid
          if ($scope.singleItem === 'true' && !_.isUndefined($scope.selectedItems) && !_.isEmpty($scope.selectedItems)) {
            $scope.itemId = $scope.selectedItems[0].id;
            $scope.searchText = $scope.selectedItems[0][$scope.displayProperty];
          }

          $scope.removeItem = function(item) {
            // Remove from selected items list
            var i = findInArray(item, $scope.selectedItems);
            if (i >= 0) {
              $scope.selectedItems.splice(i, 1);
              if ($scope.singleItem === 'true') {
                $scope.itemId = undefined;
                $scope.searchText  = undefined;
              }
            }
          };

          $scope.shouldShowList = function() {
            return ($scope.selectedItems && $scope.selectedItems.length > 0);
          };

          $scope.shouldShowSelection = function() {
            return !($scope.singleItem === 'true' && $scope.shouldShowList());
          };

          $scope.loadOptions = function() {
            if (!$scope.searchText) {
              $scope.items.loadMore();
            }
          };

          /* 
           * Mousedown event handler
           */
          $scope.selectItem = function(item) {
        	if ($scope.singleItem === 'false') {
              $scope.itemId = undefined;
              $scope.searchText  = undefined;
        	}
        	else {
              $scope.itemId = item.id;
              $scope.searchText = item[$scope.displayProperty];
        	}

            if (findInArray(item, $scope.selectedItems) < 0) {
              // If single item, clear the $scope.selectedItems
              if ($scope.singleItem === 'true') {
                $scope.selectedItems.splice(0, $scope.selectedItems.length);
              }
              // Add to selected items list
              $scope.selectedItems.push(item);
            }
          };

          $scope.viewDropdown = function(isDropdown) {
            $scope.isDropdown = isDropdown;
          };

          $scope.search = _.throttle(function() {
            $scope.items.search($scope.searchText);
          }, 1000);

          // When resetting the forms, itemId is reset because it's bound in the form via the 'item-id' property, but 'searchText' is not.
          // This makes sure the 'searchText' is reset also, to avoid refreshing problems
          $scope.$watch('itemId', function(newValue) {
        	if (_.isUndefined(newValue)) {
              $scope.searchText = undefined;
        	}
          });
        }
      ]
    };

  });

  function convertToBooleanString(value) {
    if (_.isUndefined(value) || value !== 'true') {
    	return 'false';
    }
   	return 'true';
  }

})();
