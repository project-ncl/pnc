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

  var module = angular.module('pnc.common.events');

  /**
   * @ngdoc constant
   * @name pnc.common.eventBus:eventTypes
   * @description
   * An enumeration of events
   *
   */
  module.constant('eventTypes', Object.freeze({

    USER_AUTHENTICATED: 'USER_AUTHENTICATED',

    BUILD_STARTED: 'BUILD_STARTED',

    BUILD_FINISHED: 'BUILD_FINISHED',

    BUILD_SET_STARTED: 'BUILD_SET_STARTED',

    BUILD_SET_FINISHED: 'BUILD_SET_FINISHED'
  }));

  module.factory('eventBroadcastingWebSocketListener', [
    '$log',
    '$rootScope',
    'pncEventAdaptor',
    function ($log, $rootScope, pncEventAdaptor) {

      return {

        onMessage: function (message) {
          $log.debug('Received WebSocket event: %O', message);
          var event = pncEventAdaptor.convert(message);
          $rootScope.$broadcast(event.eventType, event.payload);
        },

        onOpen: function () {
          $log.info('WebSocket opened successfully');
        },

        onClose: function () {
          $log.info('WebSocket closed');
        },

        onError: function () {
          $log.error('WebSocket Error: ', arguments);
        }

      };
    }
  ]);

  /**
   * Converts internal PNC generated events into a more useful set
   * of events for the frontend.
   */
  module.factory('pncEventAdaptor', [
    'eventTypes',
    function (eventTypes) {
      return {
        convert: function (event) {

          var adaptors = [];

          adaptors.push({
            supports: function (event) {
              return event.eventType === 'BUILD_STATUS_CHANGED';
            },
            convert: function (event) {
              var result = {
                payload: event.payload
              };

              switch (event.payload.buildStatus) {
                case 'REPO_SETTING_UP':
                  result.eventType = eventTypes.BUILD_STARTED;
                  break;
                case 'DONE':
                case 'REJECTED':
                case 'REJECTED_ALREADY_BUILT':
                case 'SYSTEM_ERROR':
                  result.eventType = eventTypes.BUILD_FINISHED;
                  break;
              }
              return result;
            }
          });


          adaptors.push({
            supports: function (event) {
              return event.eventType === 'BUILD_SET_STATUS_CHANGED' &&
                _(['id', 'buildStatus', 'userId', 'buildSetConfigurationId', 'buildSetConfigurationName'])
                  .every(function(e) { return _.has(event.payload, e); });
            },
            convert: function (event) {
              var result = {
                payload: event.payload
              };
              switch (event.payload.buildStatus) {
                case 'NEW':
                  result.eventType = eventTypes.BUILD_SET_STARTED;
                  break;
                case 'DONE':
                case 'REJECTED':
                case 'REJECTED_ALREADY_BUILT':
                  result.eventType = eventTypes.BUILD_SET_FINISHED;
                  break;
              }
              return result;
            }
          });

          var adaptor = _(adaptors).find(function (e) {
            return e.supports(event);
          });

          if(!_.isUndefined(adaptor)) {
            return adaptor.convert(event);
          } else {
            throw 'Invalid event format: ' + JSON.stringify(event);
          }
        }
      };
    }
  ]);

})();
