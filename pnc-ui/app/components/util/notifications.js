'use strict';

(function() {

  var module = angular.module('pnc.util.notifications', []);

  module.factory('notifier',
    function() {
      var notifier = {};

      /**
       *
       * @param {object} spec
       * @param {string} spec.type The type of notification to post. Valid types
       *        are: 'info', 'success', 'fail', 'warning'. Defaults to 'info'.
       * @param {string} spec.title The title of the notification to display.
       * @param {string} spec.body The message body of the notification to
       *        display.
       */
      notifier.post = function(spec) {

      }


      return notifier;
    }
  );

})();
