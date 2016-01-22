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
   * @name pnc.common.directives:pncSelect
   * @restrict E
   * @param {array} selected-items
   * An array on the in scope controller that will hold the items selected by
   * the user. The array can be pre-populated to show items that are already
   * selected.
   * @param {function} query
   * A function that should return an array of possible items for the user to
   * select, filtered by what the user has currently entered, this is passed to
   * the function as the only parameter.
   * @param {string=} display-property
   * The name of the property on the item to search against and display.
   * @description
   * A directive that allows users to select multiple options from a list of
   * possible types. The user finds possible items by typing into a type-ahead
   * style input box. The selected items are presented to the user in a list.
   * The user is given the option to remove items from the list by pressing a
   * cross button next to the selected item.
   * @example
   * <pnc-select display-property="name" selected-items="ctrl.selected" query="ctrl.getItems($viewValue)">
   * </pnc-select>
   * @author Alex Creasy
   */
  module.directive('pncInfiniteSelectList', function() {

    return {
      scope: {
        selectedItems: '=',
        displayProperty: '@',
        additionalDisplayItemsById: '=',
        items: '=',
        itemId: '=',
        placeholder: '@',
        infiniteSelectId: '@',
        infiniteSelectName: '@',
        infiniteSelectRequired: '@'
      },
      templateUrl: 'common/directives/views/pnc-infinite-select-list.html',
      controller: [
        '$log',
        '$scope',
        function($log, $scope) {

       	  var PLACEHOLDER = 'Scroll & Filter';
          $scope.placeholder = _.isUndefined($scope.placeholder) ? PLACEHOLDER : $scope.placeholder;
          $scope.infiniteSelectRequired = _.isUndefined($scope.infiniteSelectRequired) ? false : $scope.infiniteSelectRequired;

       	  var DEFAULT_DISPLAY_PROPERTY = 'name';
          $scope.displayProperty = _.isUndefined($scope.displayProperty) ? DEFAULT_DISPLAY_PROPERTY : $scope.displayProperty;

          var findInArray = function(obj, array) {
            for (var i = 0; i < array.length; i++) {
              if (angular.equals(obj, array[i])) {
                return i;
              }
            }
            return -1;
          };

          $scope.removeItem = function(item) {
            var i = findInArray(item, $scope.selectedItems);
            if (i >= 0) {
              $scope.selectedItems.splice(i, 1);
            }
          };

          $scope.shouldShow = function() {
            return ($scope.selectedItems && $scope.selectedItems.length > 0);
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
            $scope.itemId = undefined;
            $scope.searchText  = undefined;
            
            if (findInArray(item, $scope.selectedItems) < 0) {
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

})();
