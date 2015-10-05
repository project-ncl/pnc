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

  var module = angular.module('pnc.util.confirmClick', ['ui.bootstrap']);

  /**
   * @ngdoc directive
   * @name pnc.util:pncConfirmClick
   * @author Alex Creasy
   * @restrict A
   * @element ANY
   * @param {function} pnc-confirm-click
   * A function to be executed if the user confirms they wish to continue.
   * @param {string=} pnc-confirm-message
   * A message to display to the user.
   * @description
   * This directive can be used on any elements that performs destructive
   * actions when clicked. Upon clicking the user will be presented with the
   * supplied message and asked to confirm or cancel the action. If the user
   * confirms the supplied function will be called.
   * @example
   * <button pnc-confirm-click="deleteAllTheThings()" pnc-confirm-message="Are
   * you sure you want to delete all the things?">Delete All The Things</button>
   */
  module.directive('pncConfirmClick', function ($modal) {
    var DEFAULT_MESSAGE = 'Are you sure?';
    var message = attrs.pncConfirmMessage || DEFAULT_MESSAGE;

    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        element.bind('click', function() {
          var message = attrs.pncConfirmMessage || DEFAULT_MESSAGE;

          var modalInstance = $modal.open({
            animation: $scope.animationsEnabled,
            templateUrl: 'util/views/confirm-click.html',
            controller: 'ModalInstanceCtrl',
            resolve: {
              message: function () {
                return message;
              }
            }
          });
          // scope.$apply(attrs.pncConfirmClick);
        });
      }
    };
  });

})();
