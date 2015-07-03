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

  var module = angular.module('pnc.common.events');

  /**
   * @ngdoc directive
   * @name pnc.common.eventbus:pncListen
   * @restrict A
   * @param {string} pnc-listen
   * The event types to listen for seperated by a `|` character. Valid events
   * are found in {@link pnc.common.eventBus:eventTypes `eventTypes`}
   * @param {function} pnc-callback
   * A callback function that will be executed when an event that is being
   * listened for occurs. The callback will be invoked with 2 parameters, the
   * first is the event object itself, the second the payload object that was
   * broadcast with the event.
   * @description
   * Listens for specific events and executes a callback function
   * when any of the specified events occur.
   * @example
   * ```html
      <div pnc-listen="BUILD_COMPLETED|BUILD_FAILED" pnc-callback="(event, payload)">
      </div>
   *```
   * @author Alex Creasy
   */
  module.directive('pncListen', function() {

    return {
      restrict: 'A',
      scope: {
        pncCallback: '&'
      },
      link: function(scope, element, attrs) {
        var listenEvents = attrs.pncListen.split('|');

        listenEvents.forEach(function(eventType) {
          scope.$on(eventType.trim(), function(event, payload) {
            scope.pncCallback({event: event, payload: payload });
          });
        });
      }
    };

  });

})();
