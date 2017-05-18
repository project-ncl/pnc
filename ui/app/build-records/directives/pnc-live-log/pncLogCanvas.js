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
    template: '<div></div>',
    controller: ['$element', Controller]
  });

  function Controller($element) {
    var $ctrl = this,
        div,
        parent,
        autoscroll,
        startPosition;

    function write(text) {
      div.append(text);
      if (autoscroll) {
        parent.scrollTop = parent.scrollHeight;
        // Reset the start position so we can detect which direction the user
        // has scrolled in next time.
        startPosition = parent.scrollTop;
      }
    }

    function writeln(text) {
      write(text + '<br>');
    }

    $ctrl.$postLink = function () {
      // Find the child div specified in `template`
      div = $element.find('div');
      parent = $element[0];

      autoscroll = true;
      startPosition = parent.scrollTop;

      // Catch scroll events so we can enable / disable scrolling based
      // on whether the user is scrolled to the bottom.
      parent.onscroll = function () {
        var endPosition = parent.scrollTop;
        var height = $element.innerHeight();
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
