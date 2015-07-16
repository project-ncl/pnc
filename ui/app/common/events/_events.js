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

      webSocketBusProvider.newEndpoint(
        'ws://' + window.location.host + '/pnc-rest/ws/build-records/notifications',
        'eventBroadcastingWebSocketListener'
      );
    }
  ]);

  module.run([
    '$rootScope',
    'webSocketBus',
    'eventTypes',
    'PncRestClient',
    'Notifications',
    function($rootScope, webSocketBus, eventTypes, PncRestClient, Notifications) {
      var scope = $rootScope.$new();

      //TODO: When backend functionality is available these notifications
      // should only be fired if the userId of the payload matches the
      // current logged in user.

      scope.$on(eventTypes.BUILD_STARTED, function(event, payload) {
        Notifications.info('Build #' + payload.id + ' in progress');
      });

      // Notify user when builds finish.
      scope.$on(eventTypes.BUILD_FINISHED, function(event, payload) {

        PncRestClient.Record.get({ recordId: payload.id }).$promise.then(
          function(result) {
            if (result.status === 'SUCCESS') {
              Notifications.success('Build #' + payload.id + ' completed');
            } else {
              Notifications.warn('Build #' + payload.id + ' failed');
            }
          }
        );

      });

    }
  ]);

})();
