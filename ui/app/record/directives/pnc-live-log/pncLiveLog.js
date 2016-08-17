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
(function() {
  'use strict';

  /**
   * @ngdoc directive
   * @name pnc.record:pncLiveLog
   * @restrict EA
   * @interface
   * @description
   * @example
   * @author Alex Creasy
   */
  angular.module('pnc.record')
    .directive('pncLiveLog', function() {
      return {
        restrict: 'EA',
        scope: {
          pncBuildRecord: '='
        },
        controller: function($log, $scope, $websocket, $interval) {
          var socket;

          // Pnc REST Api only gives us an http link to the
          // builder's terminal, this function reformats the
          // URI to connect to the WebSocket
          function createWsUri(liveLogUri) {
            return 'ws' + liveLogUri.substring(4) + 'socket/text/ro';
          }

          function writelogln(line) {
            $scope.$emit('pnc-log-canvas::add_line', line);
          }

          function connect(uri) {
            $log.debug('Attempting to connect to build agent at: ' + uri);
            socket = $websocket(uri);

            socket.onMessage(function(msg) {
              $log.debug('Received logline: `%s`', msg.data);
              writelogln(msg.data);
            });

            socket.onOpen(function() {
              $log.debug('Connected to build agent at: ' + socket.socket.url);
              writelogln('*** Connected to build agent ***');
            });

            socket.onError(function() {
              writelogln('*** Error connecting to build agent ***');
            });

            socket.onClose(function() {
              $log.debug('Disconnected from build agent at: ' + socket.socket.url);
              writelogln('*** Connection to build agent closed ***');
            });
          }

          /*
           * A build agent is not assigned until a number of setup tasks have
           * been completed by the PNC backend. Because of this the liveLogsUri
           * of the BuilRecord will be null for a window of time. The below code
           * handles this by refreshing the BuildRecord every 10 seconds (for a
           * maximum of 10 attempts) until the liveLogsUri contains the address
           * of the build agent then attempts to connect on the WebSocket.
           */
           function notifyWaiting() {
             $log.debug('BuildRecord property liveLogsUri is not present, retrying in 10 seconds');
             writelogln('*** Waiting for build agent ***');
           }
          var interval;

          if (!_.isEmpty($scope.pncBuildRecord.liveLogsUri)) {
            connect(createWsUri($scope.pncBuildRecord.liveLogsUri));
          } else {

            notifyWaiting();

            interval = $interval(function() {
              $scope.pncBuildRecord.$get().then(function(buildRecord) {

                if (!_.isEmpty(buildRecord.liveLogsUri)) {
                  connect(createWsUri(buildRecord.liveLogsUri));
                  $interval.cancel(interval);
                } else {
                  notifyWaiting();
                }

              });
            }, 10000, 10, false);
          }


          // Clean up the connection when user navigates away.
          $scope.$on('$destroy', function() {
            // Force close the websocket.
            if (socket) {
              socket.close(true);
            }
            $interval.cancel(interval);
          });
        }
      };
    });
})();
