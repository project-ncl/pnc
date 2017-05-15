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
   * @ngdoc component
   * @name pnc.record:pncLiveLog
   * @restrict EA
   * @interface
   * @description
   * @example
   * @author Alex Creasy
   */
  angular.module('pnc.build-records').component('pncLiveLog', {
    bindings: {
      buildRecord: '<'
    },
    controller: ['$log', '$scope', '$websocket', Controller]
  });

  function Controller($log, $scope, $websocket) {
    var $ctrl = this,
        socket,
        HR = '------------------------------------------------------------------------------------------------------------------------',
        EM = '***';


    // -- Controller API --

    $ctrl.connect = connect;
    $ctrl.disconnect = disconnect;

    // --------------------

    function writelogln(line) {
      $scope.$emit('pnc-log-canvas::add_line', line);
    }

    function writelogHeading(line) {
      writelogln(HR);
      writelogln(line);
      writelogln(HR + '\n\n');
    }

    function writeLogEm(line) {
      writelogln(EM + ' ' + line + ' ' + EM);
    }

    function connect(url, serviceName) {
      serviceName = serviceName || 'service';
      $log.debug('Attempting to connect to %s at: %s for updates', serviceName, url);
      socket = $websocket(url);

      socket.onMessage(function (msg) {
        writelogln(msg.data);
      });

      socket.onOpen(function () {
        $log.info('Connected to %s at: %s', serviceName, socket.url);
        writeLogEm('Connected to ' + serviceName);
      });

      socket.onError(function() {
        $log.error('Connection error to %s at: %s', serviceName, socket.url);
        writeLogEm('Error connecting to ' + serviceName);
      });

      socket.onClose(function() {
        $log.info('Disconnected from %s at: %s', serviceName, socket.url);
        writeLogEm('Connection to ' + serviceName + ' closed');
      });
    }

    function disconnect(force) {
      if (socket) {
        socket.close(!!force);
      }
    }

    function processUpdate(task, status, url) {
      writelogHeading('PROCESS PROGRESS UPDATE: ' + status + ' ' + task);
      if (url) {
        disconnect(true);
        connect(url, task);
      }
    }

    $scope.$on('PROCESS_PROGRESS_UPDATE', function (event, payload) {
      processUpdate(payload.taskName, payload.bpmTaskStatus, payload.detailedNotificationsEndpointUrl);
    });

    $scope.$on('$destroy', function () {
      disconnect(true);
    });
  }
})();
