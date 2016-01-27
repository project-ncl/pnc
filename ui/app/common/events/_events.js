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

(function () {

  var module = angular.module('pnc.common.events', [
    'pnc.common.websockets'
  ]);

  module.config([
    'webSocketBusProvider',
    function(webSocketBusProvider) {
      var host = window.location.host; // 'localhost:8080'
      webSocketBusProvider.newEndpoint(
        'ws://' + host + '/pnc-rest/ws/build-records/notifications',
        'eventBroadcastingWebSocketListener'
      );
    }
  ]);

  module.run([
    '$log',
    '$rootScope',
    'webSocketBus',
    'eventTypes',
    'BuildRecordDAO',
    'authService',
    'Notifications',
    function($log, $rootScope, webSocketBus, eventTypes, BuildRecordDAO,
             authService, Notifications) {
      var scope = $rootScope.$new();

      //TODO: When backend functionality is available these notifications
      // should only be fired if the userId of the payload matches the
      // current logged in user.

      scope.$on(eventTypes.BUILD_STARTED, function(event, payload) {
        //$log.debug('BUILD_STARTED_EVENT: payload=%O, authService.getPncUser=%O, payload.userId=%O', payload, authService.getPncUser(), payload.userId);
        $log.debug('BUILD_STARTED_EVENT: authService.getPncUser=%O, payload=%O', authService.getPncUser(), JSON.stringify(payload));

        if (authService.getPncUser().id === payload.userId) {
          Notifications.info('Build ' + payload.buildConfigurationName + '#' + payload.id + ' in progress');
        }
      });

      // Notify user when builds finish
      // (see events-services.js for the conversion
      // between server and client buildCoordinationStatus)
      scope.$on(eventTypes.BUILD_FINISHED, function(event, payload) {
        $log.debug('BUILD_FINISHED: payload=%O', JSON.stringify(payload));

        if (authService.getPncUser().id === payload.userId) {
          if (payload.buildCoordinationStatus === 'REJECTED') {
            Notifications.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' rejected.');
          } else if (payload.buildCoordinationStatus === 'REJECTED_ALREADY_BUILT') {
            Notifications.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' was rejected because already built.');
          } else if (payload.buildCoordinationStatus === 'SYSTEM_ERROR') {
            Notifications.error('A system error prevented the Build ' + payload.buildConfigurationName + '#' + payload.id + ' from starting.');
          } else {
            BuildRecordDAO.get({recordId: payload.id}).$promise.then(
              function (result) {
                if (result.status === 'SUCCESS') {
                  Notifications.success('Build ' + payload.buildConfigurationName + '#' + payload.id + ' completed');
                } else {
                  Notifications.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' failed');
                }
              }
            );
          }
        }
      });

    }
  ]);

})();
