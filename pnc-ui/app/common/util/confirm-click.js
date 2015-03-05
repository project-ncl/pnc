'use strict';

(function() {

  var module = angular.module('pnc.util.confirmClick', []);

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
  module.directive('pncConfirmClick', function () {
    var DEFAULT_MESSAGE = 'Are you sure?';

    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        element.bind('click', function() {
          var message = attrs.pncConfirmMessage || DEFAULT_MESSAGE;
          if (confirm(message)) { // jshint ignore:line
            scope.$apply(attrs.pncConfirmClick);
          }
        });
      }
    };
  });

})();
