/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.common.pnc-client.message-bus').factory('buildStatusListener', [
    '$log',
    '$rootScope',
    'eventTypes',
    'pncEventAdaptor',
    function ($log, $rootScope, eventTypes, pncEventAdaptor) {
      return function (message) {
        if (message.eventType === eventTypes.BUILD_STATUS_CHANGED) {
          var event = pncEventAdaptor.convert(message);
          $rootScope.$broadcast(event.eventType, event.payload);
          

          /*
           * Provides a more general notification for components that don't care
           * about the difference between STARTED and FINISHED events.
           * 
           * Normalizes the message payload with the BuildConfigurationRest entity. 
           */
          $rootScope.$broadcast(eventTypes.BUILD_STATUS_CHANGED, {
            id: message.payload.id,
            status: message.payload.buildCoordinationStatus,
            userId: message.payload.userId,
            buildConfigurationId: message.payload.buildConfigurationId,
            buildConfigurationName: message.payload.buildConfigurationName,
            startTime: message.payload.buildStartTime,
            endTime: message.payload.buildEndTime 
          });
        }
      };
    }
  ]);

})();
