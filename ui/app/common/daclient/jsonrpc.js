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

  var module = angular.module('pnc.common.daclient');

  /** 
   * This provider can be enhanced with interceptors:
   *
   * module.config(['jsonrpcProvider', function(jsonrpcProvider) {
   *   jsonrpcProvider.interceptors.push(function(){
   *     return {
   *       requestStarted: function(requestFinishedPromise, cfpLoadingBar) { ..custom logic }
   *     }
   *   });
   * }]);
   */
  module.provider('jsonrpc', function() {

    this.interceptors = [];

    this.$get = [
      '$log',
      '$q',
      '$websocket',
      'cfpLoadingBar',
      function($log, $q, $websocket, cfpLoadingBar) {

        var that = this;

        var JSON_RPC_VERSION = '2.0';
        var ID_PREFIX = 'request_';

        function wsClient(url) {
          var instance = {};

          /*
           * Instance private fields
           */

          var socket;
          var deferrals = {}; // Map of id => deferred

          /*
           * Instance private methods
           */
          function sendOnSocket(msg) {
            return instance.connect().then(function() {
              return socket.send(msg);
            });
          }

          function makeRpcRequest(request) {
            var deferred = $q.defer();

            request.id = _.uniqueId(ID_PREFIX);

            // As rpc invocations are asynchronous and request -> response may
            // interleave with other requests we store the id => deferred entry
            // in the map so we can resolve / reject  the correct promise
            // when we receive the rpc response.
            deferrals[request.id] = deferred;

            sendOnSocket(request).catch(function () {
              deferred.reject({
                code: 0,
                message: 'Error connecting to server'
              });
            });

            $log.debug('Making RPC request: ' + JSON.stringify(request, null, 2));

            that.interceptors.forEach(function(interceptor) {
              interceptor().requestStarted(deferred.promise, cfpLoadingBar);
            });

            return deferred.promise;
          }

          function handleRpcResponse(response) {
            $log.debug('Received RPC response: ' + JSON.stringify(response, null, 2));

            if (!deferrals.hasOwnProperty(response.id)) {
              // This code path should be unreachable!
              throw new Error('Illegal State: Received JSON-RPC response with unknown id: ' + response.id);
            }

            var deferred = deferrals[response.id];

            if (response.hasOwnProperty('error')) {
              deferred.reject(response.error);
            } else {
              deferred.resolve(response.result);
            }
          }

          /*
           * Instance public methods
           */

          /**
           *
           */
          instance.connect = function () {

            if (instance.isConnected()) {
              // Return a pre-resolved promise if it's already connected.
              return $q.when();
            }

            var deferred = $q.defer();

            $log.debug('Attempting to connect to WebSocket at: ' + url);
            socket = $websocket(url);

            socket.onMessage(function(msg) {
              var data = JSON.parse(msg.data);
              handleRpcResponse(data);
            });

            socket.onOpen(function() {
              $log.debug('Connected to WebSocket at: ' + socket.socket.url);
              deferred.resolve();
            });

            socket.onError(function() {
              $log.error('Error with WebSocket connection to: ' + socket.socket.url);
              deferred.reject();
            });

            socket.onClose(function() {
              $log.debug('Disconnected from WebSocket at: ' + socket.socket.url);
            });

            // var sendFn = socket.send;
            // socket.send = function(msg) {
            //   $log.debug('Making RPC request: ' + JSON.stringify(msg, null, 2));
            //   sendFn.call(socket, arguments);
            // };

            return deferred.promise;
          };

          /**
           *
           */
          instance.disconnect = function (force) {
            socket.close(force);
          };

          /**
           *
           */
          instance.isConnected = function () {
            if (_.isUndefined(socket)) {
              return false;
            }
            return socket.readyState === 1;
          };

          /**
           *
           */
          instance.invoke = function (method, params) {
            var request = {
              jsonrpc: JSON_RPC_VERSION,
              method: method,
              params: params
            };

            return makeRpcRequest(request);
          };

          return instance;
        }

        return {
          wsClient: wsClient
        };

    }];
        
  });

})();
