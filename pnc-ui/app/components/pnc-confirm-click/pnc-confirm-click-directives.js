'use strict';

(function() {

  var DEFAULT_MESSAGE = "Are you sure?";

  var module = angular.module('pnc.components.confirmClick', []);

  module.directive('pncConfirmClick', function () {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        element.bind('click', function() {
          var message = attrs.pncConfirmMessage || DEFAULT_MESSAGE;
          if (confirm(message)) {
            scope.$apply(attrs.pncConfirmClick);
          }
        });
      }
    }
  });

})();
