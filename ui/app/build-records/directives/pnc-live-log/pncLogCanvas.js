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
(function () {
  'use strict';

  angular.module('pnc.build-records').component('pncLogCanvas', {
    bindings: {
      /**
       * Must provide a function that takes one paramater. This function will
       * be invoked at component initialisation passing it a single logWriter
       * object, allowing a parent component to write to the log canvas.
       * The logWriter object has two
       * methods: write and writeln. Both take a string and print this to the
       * canvas. Writeln appends a <br> tag to the string before writing.
       */
      getLogWriterFn: '&'
    },
    template: '<div class="pnc-log-canvas-parent log-console well well-sm"><div class="pnc-log-canvas-content"></div></div>',
    controller: ['$element', Controller]
  });

  function Controller($element) {
    var $ctrl = this,
        content,
        parent,
        autoscroll;

    function write(text) {
      content.append(text);
      if (autoscroll) {
        parent.scrollTop = parent.scrollHeight;
      }
    }

    function writeln(text) {
      write(text + '<br>');
    }

    $ctrl.$postLink = function () {
      // Find the child div specified in `template`
      content = $element.find('.pnc-log-canvas-content');
      parent = $element.find('.pnc-log-canvas-parent')[0];

      autoscroll = true;

      // Catch scroll events so we can enable / disable scrolling based
      // on whether the user is scrolled to the bottom.
      parent.onscroll = function () {
        // user does not have to reach the bottom exactly, if he is close enough,
        // autoscroll will be also activated
        var PADDING = 10;

        if (parent.scrollTop + parent.offsetHeight + PADDING >= parent.scrollHeight) {
          // User is scrolling downwards, if they scroll to the bottom
          // enable autoscroll.
          autoscroll = true;
        } else {
          // User is scrolling up so disable autoscroll
          autoscroll = false;
        }
      };
    };

    $ctrl.$onInit = function () {

      // Passes this component's API up to a component.
      // This is a bit of an odd way of doing it, but
      $ctrl.getLogWriterFn({
        writer: {
          write: write,
          writeln: writeln
        }
      });
    };
  }

})();
