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

  angular.module('pnc.common.pnc-client.message-bus').factory('buildSetStatusListener', [
    '$rootScope',
    'pncNotify',
    'authService',
    'eventTypes',
    'pncEventAdaptor',
    function ($rootScope, pncNotify, authService, eventTypes, pncEventAdaptor) {
      return function (message) {
        if (message.eventType === 'BUILD_SET_STATUS_CHANGED') {
          var payload = message.payload,
              event = pncEventAdaptor.convert(message);
          
          $rootScope.$broadcast(event.eventType, event.payload);
          $rootScope.$broadcast(eventTypes.BUILD_SET_STATUS_CHANGED, message.payload);

          authService.forUserId(payload.userId).then(function () {
            switch(payload.buildStatus) {
              case 'REJECTED':
                pncNotify.warn('Build of group: ' + payload.buildSetConfigurationName + ' was rejected: ' + payload.description);
                break;
            }
          });
        }
      };
    }
  ]);

})();
