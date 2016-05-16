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
   * @name pnc.common.directives:pncMultipleInput
   * @restrict E
   * @param {array=} selected-items
   * An array on the in scope controller that will hold the items selected by
   * the user. The array can be pre-populated to show items that are already
   * selected.
   * @param {string@} placeholder
   * The placeholder to show in the input field.
   * @param {string@} select-id
   * The ui id of the select directive (for UI validation)
   * @param {string@} select-name
   * The ui name of the select directive (for UI validation)
   * @description
   * A directive that allows users to add multiple values from a input field.
   * @example
   * <pnc-multiple-input 
       selected-items="customCtrl.customProperty" 
       placeholder="customPlaceholder" select-id="customId" select-name="customName">
     </pnc-multiple-input>
   * 
   */
  module.directive('pncMultipleInput', function() {
    return {
      restrict: 'E',
      scope: {
        selectedItems: '=',
        placeholder: '@',
        selectId: '@',
        selectName: '@',
      },
      templateUrl: 'common/directives/views/pnc-multiple-input.html',
      controller: [
        '$log',
        '$scope',
        function($log, $scope) {

          var findInArray = function(obj, array) {
            for (var i = 0; i < array.length; i++) {
              if (obj === array[i]) {
                return i;
              }
            }
            return -1;
          };

          $scope.removeItem = function(item) {
            // Remove from selected items list
            var i = findInArray(item, $scope.selectedItems);
            if (i >= 0) {
              $scope.selectedItems.splice(i, 1);
            }
          };

          $scope.shouldShowList = function() {
            return ($scope.selectedItems && $scope.selectedItems.length > 0);
          };

          $scope.selectItem = function(item) {
            if (item && !_.include($scope.selectedItems, item)) {
              $scope.selectedItems.push(item);
              $scope.inputItem = '';
            }
          };

        }
      ]
    };

  });

})();
