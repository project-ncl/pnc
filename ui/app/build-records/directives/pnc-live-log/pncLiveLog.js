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

  angular.module('pnc.build-records').component('pncLiveLog', {
    bindings: {
      buildRecord: '<'
    },
    template: '<pnc-log-canvas get-log-writer-fn="$ctrl.getLogWriter(writer)"></pnc-log-canvas>',
    controller: ['$log', '$scope', '$websocket', Controller]
  });

  function Controller($log, $scope, $websocket) {
    var $ctrl = this,
        socket,
        logWriter,
        HR = '----------------------------------------------------------------------',
        EM = '***',
        messages = [],
        isProcessingActive = false,
        processingInterval = 750;


    function writelogln(line) {
      logWriter.writeln(line);
    }

    function writelogHeading(line) {
      writelogln(HR);
      writelogln(line);
      writelogln(HR);
    }

    function writeLogEm(line) {
      writelogln(EM + ' ' + line + ' ' + EM);
    }

    function processMessages() {
      if (messages.length) {
        isProcessingActive = true;
        writelogln(messages.join('<br>'));
        messages = [];

        if (socket.readyState === 1) {
          setTimeout(function () {
            processMessages();
          }, processingInterval);
        }

      } else {
        // suspend processing until onMessage event is fired
        isProcessingActive = false;
      }
    }

    function connect(url, serviceName) {
      serviceName = serviceName || 'service';
      $log.debug('Attempting to connect to %s at: %s for updates', serviceName, url);
      socket = $websocket(url, { autoApply: false });

      socket.onMessage(function (msg) {
        messages.push(msg.data);

        if (!isProcessingActive) {
          processMessages();
        }
      });

      socket.onOpen(function () {
        $log.info('Connected to %s at: %s', serviceName, socket.url);
        writeLogEm('Connected to ' + serviceName);
        processMessages();
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

    $ctrl.getLogWriter = function (writer) {
      logWriter = writer;
    };

    $ctrl.$onInit = function () {
      $scope.$on('PROCESS_PROGRESS_UPDATE', function (event, payload) {
        processUpdate(payload.taskName, payload.bpmTaskStatus, payload.detailedNotificationsEndpointUrl);
      });
    };

    $ctrl.$onDestroy = function () {
      disconnect(true);
    };
  }
})();
