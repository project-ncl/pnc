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

  angular.module('pnc.common.pnc-client.message-bus').factory('messageBus', [
    '$websocket',
    '$injector',
    '$log',
    function ($websocket, $injector, $log) {

      var SUBSCRIBE_DATA_TEMPLATE = Object.freeze({
        action: 'SUBSCRIBE'
      });

      var UNSUBSCRIBE_DATA_TEMPLATE = Object.freeze({
        action: 'UNSUBSCRIBE'
      });

      var listeners = [],
          webSocket;

      function connect(url) {
        webSocket = $websocket(url, null, { reconnectIfNotNormalClose: true });

        webSocket.onOpen(function () {
          $log.info('Connected to PNC messageBus at: %s', webSocket.url);
        });

        webSocket.onClose(function () {
          $log.info('Disconnected from PNC messageBus at: %s', webSocket.url);
        });

        webSocket.onError(function () {
          $log.error('Connection error on PNC messageBus at: %s', webSocket.url);
        });

        webSocket.onMessage(function (message) {
          var parsedMsg = JSON.parse(message.data);
          listeners.forEach(function (listener) {
            listener(parsedMsg);
          });
        });
      }

      function disconnect(force) {
        webSocket.close(!!force);
      }

      function subscribe(params) {
        webSocket.send({
          messageType: 'PROCESS_UPDATES',
          data: angular.merge({}, SUBSCRIBE_DATA_TEMPLATE, params)
        });

        // Returns a function to unsubscribe for convenience.
        return function () {
          unsubscribe(params);
        };
      }

      function unsubscribe(params) {
        webSocket.send({
          messageType: 'PROCESS_UPDATES',
          data: angular.merge({}, UNSUBSCRIBE_DATA_TEMPLATE, params)
        });
      }

      function registerListener(listener) {
        listeners.push(angular.isString(listener) ?
            $injector.get(listener) :  $injector.invoke(listener));
      }

      return {
        connect: connect,
        disconnect: disconnect,
        subscribe: subscribe,
        unsubscribe: unsubscribe,
        registerListener: registerListener
      };
    }
  ]);

})();
