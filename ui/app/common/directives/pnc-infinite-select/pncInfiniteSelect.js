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

  var module = angular.module('pnc.common.directives');

  /**
   * @param items
   * List of items which will be displayed
   *
   * @param itemId
   * Place where selected item id will be stored
   *
   * @description
   * General component which can be used when infinite selectbox
   * is needed.
   */
  module.directive('pncInfiniteSelect', function () {
    return {
      restrict: 'E',
      scope: {
        items: '=',
        itemId: '=',
        searchText: '@',
        placeholder: '@',
        infiniteSelectId: '@',
        infiniteSelectName: '@',
        infiniteSelectRequired: '@'
      },
      templateUrl: 'common/directives/pnc-infinite-select/pnc-infinite-select.html',
      controller: [
        '$log',
        '$scope',
        function($log, $scope) {

          var PLACEHOLDER = 'Scroll & Filter';
          $scope.placeholder = _.isUndefined($scope.placeholder) ? PLACEHOLDER : $scope.placeholder;
          $scope.infiniteSelectRequired = _.isUndefined($scope.infiniteSelectRequired) ? false : $scope.infiniteSelectRequired;

          $scope.loadOptions = function() {
            if (!$scope.searchText) {
              $scope.items.loadMore();
            }
          };

          /*
           * Mousedown event handler
           */
          $scope.selectItem = function(item) {
            $scope.itemId = item.id;
            $scope.searchText  = item.name;
          };

          $scope.viewDropdown = function(isDropdown) {
            $scope.isDropdown = isDropdown;
          };

          $scope.search = _.throttle(function() {
            $scope.items.search($scope.searchText);
          }, 1500);

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
