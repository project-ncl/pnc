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

/**
 * @ngdoc directive
 * @name pnc.record:pncLogCanvas
 * @restrict EA
 * @interface
 * @description
 * @example
 * @author Alex Creasy
 */
angular.module('pnc.record')
  .directive('pncLogCanvas', function() {

    return {
      restrict: 'EA',
      template: '<div></div>',
      link: function(scope, element) {
        // Find the child div specified in `template`
        var div = element.find('div');
        var parent = element[0];

        var autoscroll = true;
        var startPosition = parent.scrollTop;

        // Catch scroll events so we can enable / disable scrolling based
        // on whether the user is scrolled to the bottom.
        parent.onscroll = function () {
          var endPosition = parent.scrollTop;
          var height = element.innerHeight();
          var bottom = parent.scrollHeight;

          if (endPosition >= startPosition) {
            // User is scrolling downwards, if they scroll to the bottom
            // enable autoscroll.
            if (endPosition + height >= bottom) {
              autoscroll = true;
            }
          } else {
            // User is scrolling up so disable autoscroll
            autoscroll = false;
          }
          // Reset the start position so we can detect which direction the user
          // has scrolled in next time.
          startPosition = parent.scrollTop;
        };

        function addToLog(event, payload) {
          div.append(payload + '<br>');
          if (autoscroll) {
            parent.scrollTop = parent.scrollHeight;
            // Reset the start position so we can detect which direction the user
            // has scrolled in next time.
            startPosition = parent.scrollTop;
          }
        }

        scope.$on('pnc-log-canvas::add_line', addToLog);
      }
    };

});
