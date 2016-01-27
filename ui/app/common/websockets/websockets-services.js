/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

(function () {

  var module = angular.module('pnc.common.websockets');

  /**
   * @ngdoc provider
   * @name webSocketBusProvider
   * @description
   * Use `webSocketBusProvider` to configure the `webSocketBus` service. This
   * provider offers methods for registering WebSocket endpoints with an
   * associated listener service.
   * @example
   * # Creating a basic listener service.
   * ```js
      angular.module('myModule')
        .factory('loggingWebSocketListener',
          function($log) {
            return {

              onMessage: function(message) {
                $log.info('Received Message: ' + JSON.stringify(message));
              },

              onOpen: function() {
                  $log.info('WebSocket connection opened');
              },

              onClose: function() {
                $log.info('WebSocket connection closed');
              },

              onError: function() {
                $log.error('Error on WebSocket');
              }
            };
          }
        );
   * ```
   * # Registering a listener service.
   * ```js
       angular.module('myModule', ['pnc.common.websockets'])
         // Inject the webSocketBusProvider into the module config.
         .config(function(webSocketBusProvider) {
           webSocketBusProvider.newEndpoint(
             'ws://localhost/myEndpoint',
             'loggingWebSocketListener'
           );
         });
   * ```
   * @author Alex Creasy
   */
  module.provider('webSocketBus',
    function () {

      var endpoints = this.endpoints = [];

      function WsEndpoint(url, listenerService) {
        if (!angular.isString(url)) {
          throw new TypeError('Parameter url must be a string form websocket url');
        }

        if (!angular.isString(listenerService)) {
          throw new TypeError('Parameter listenerService must be a string name of an angular service');
        }

        this.url = url;
        this.listenerServiceName = listenerService;
        this.socket = null;
        this.listener = null;
      }

      /**
       * @ngdoc method
       * @name webSocketBusProvider#newEndpoint
       * @param {string} url The address of the WebSocket endpoint to connect
       * to.
       * @param {string} listenerService The name of the angular service to
       * register as the listener for this endpoint.
       * @description
       * Registers a service as a listener for websockets. A listener service
       * should at least expose an `onMessage` method that will be invoked and
       * passed the websocket message as an object, each time a message is
       * receievd on the websocket endpoint. Optional methods that can be
       * exposed are `onError`, `onOpen` and `onClose`.
       */
      this.newEndpoint = function(url, listenerService) {
        endpoints.push(new WsEndpoint(url, listenerService));
      };

      /**
       * @ngdoc service
       * @name webSocketBus
       * @requires $log
       * @requires $injector
       * @requires $websocket
       * @description
       * A bus for listening to multiple WebSocket endpoints. Endpoints are
       * configured using `webSocketBusProvider`. This service itself exposes
       * only one method `close`, used to close all WebSocket connections.
       * @author Jakub Senko
       * @author Alex Creasy
       */
      this.$get = [
        '$log',
        '$injector',
        '$websocket',
        function($log, $injector, $websocket) {
          // Add functionality to WsEndpoint objects now we have access to the
          // dependency injector service.
          WsEndpoint.prototype.open = function() {
            var listener, socket;
            try {
              listener = this.listener = $injector.get(this.listenerServiceName);
              socket = this.socket = $websocket(this.url);
            } catch (e) {
              throw e;
            }

            socket.onMessage(function(message) {
              var parsedData = JSON.parse(message.data);
              listener.onMessage(parsedData);
            });

            if (listener.onOpen) {
              socket.onOpen(function() {
                listener.onOpen(arguments);
              });
            }

            if (listener.onClose) {
              socket.onClose(function() {
                listener.onClose(arguments);
              });
            }

            if (listener.onError) {
              socket.onError(function() {
                listener.onError(arguments);
              });
            }
          };

          WsEndpoint.prototype.close = function(force) {
            this.socket.close(force);
          };

          /*
           * Init the WebSocket service by opening all endpoints.
           */
          endpoints.forEach(function(endpoint) {
            endpoint.open();
          });

          return {
            close: function(force) {
              endpoints.forEach(function(endpoint) {
                endpoint.close(force);
              });
            }
          };
        }
      ];
    }
  );

})();
