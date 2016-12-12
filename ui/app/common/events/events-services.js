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

    BUILD_SET_FINISHED: 'BUILD_SET_FINISHED',

    BCC_BPM_NOTIFICATION: 'BCC_BPM_NOTIFICATION'
  }));

  module.factory('eventBroadcastingWebSocketListener', [
    '$log',
    '$rootScope',
    'pncEventAdaptor',
    function ($log, $rootScope, pncEventAdaptor) {

      return {

        onMessage: function (message) {
          $log.debug('Received on notification WebSocket: %O', message);
          var event = pncEventAdaptor.convert(message);
          $rootScope.$broadcast(event.eventType, event.payload);
        },

        onOpen: function () {
          $log.info('Notification WebSocket opened successfully');
        },

        onClose: function () {
          $log.info('Notification WebSocket closed');
        },
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

              switch (event.payload.buildCoordinationStatus) {
                case 'NEW':
                case 'WAITING_FOR_DEPENDENCIES':
                case 'BUILDING':
                  result.eventType = eventTypes.BUILD_STARTED;
                  break;
                case 'DONE':
                case 'REJECTED':
                case 'REJECTED_ALREADY_BUILT':
                case 'SYSTEM_ERROR':
                case 'DONE_WITH_ERRORS':
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
                  result.eventType = eventTypes.BUILD_SET_FINISHED;
                  break;
              }
              return result;
            }
          });

          adaptors.push({
            supports: function (event) {
              return _.has(event, 'eventType') && event.eventType.startsWith('BCC_');
            },
            convert: function (event) {
              // BCC events have no prescribed structure apart from having 'eventType' attribute.
              // Creating a wrapper:
              return {'eventType': eventTypes.BCC_BPM_NOTIFICATION, 'payload': event};
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


  /**
   * Get a function that will show human readable BCC notification
   * from a few pieces of data.
   */
  module.factory('bccEventHandler', [
      '$log',
      '$q',
      '$rootScope',
      'eventTypes',
      'pncNotify',
      '$state',
      'BuildConfigurationDAO',
      function ($log, $q, $rootScope, eventTypes, pncNotify, $state, BuildConfigurationDAO) {

        var res = {};

        // keeps state for each registered (BPM) taskId
        var _taskIdHandlers = {};

        $rootScope.$on(eventTypes.BCC_BPM_NOTIFICATION, function(event, payload) {
          if (_.has(payload, 'data.taskId') && _.has(_taskIdHandlers, payload.data.taskId)) {
            _taskIdHandlers[payload.data.taskId].handle(payload);
          } else {
            $log.warn('No handler for ', payload);
          }
        });

        var _translate = function(eventType, bcName) {
          var meta = { // human readable message and whether it is a success
            'BCC_REPO_CREATION_SUCCESS': [0, 'Created the repository for "' + bcName + '".'],
            'BCC_REPO_CREATION_ERROR': [-1, 'Failed to create the repository for "' + bcName + '".'],
            'BCC_REPO_CLONE_SUCCESS': [0, 'Cloned data into internal repository for "' + bcName + '".'],
            'BCC_REPO_CLONE_ERROR': [-1, 'Failed to clone data into internal repository for "' + bcName + '". Verify that the URL and revision are correct.'],
            'BCC_CREATION_SUCCESS': [1, 'Created "' + bcName + '".'],
            'BCC_CREATION_ERROR': [-1, 'Failed to create "' + bcName + '".']
          };
          return meta[eventType];
        };

        // Given a taskId from start BCC process endpoint,
        // Return a promise corresponding to the success of the operation,
        // While issuing notifications in the process.
        // Also provide BC name for human readability.
        res.register = function(taskId, bcName) {
          // TODO tidy up; success = 1, error = -1, info = 0
          var deferred = $q.defer();
          _taskIdHandlers[taskId] = {
            handle: function(payload) {
              var meta = _translate(payload.eventType, bcName);
              if(_.isUndefined(meta)) {
                return;
              }
              var msg = '';
              if(_.has(payload, 'data.message')) {
                var m = payload.data.message;
                $log.warn('', meta[1] + m);
                msg = ' ' + m.substring(0, 125);
                if(m.length > 125) {
                  msg = msg + '...';
                }
              }
              if(meta[0] === 1) {
                var id = parseInt(payload.data.buildConfigurationId);
                pncNotify.success(meta[1] + msg, 'Build Conf. #' + id, function() {
                  // TODO this is silly
                  BuildConfigurationDAO.get({ configurationId: id }).$promise.then(function(data) {
                    $state.go('projects.detail.build-configs.detail', {
                      projectId: data.project.id,
                      configurationId: id
                    });
                  });
                });
                deferred.resolve(id);
              } else if(meta[0] === -1) {
                pncNotify.error(meta[1] + msg);
                deferred.reject();
              } else {
                pncNotify.info(meta[1] + msg);
              }
            }
          };
          return deferred.promise;
        };

        return res;
      }
  ]);

})();
