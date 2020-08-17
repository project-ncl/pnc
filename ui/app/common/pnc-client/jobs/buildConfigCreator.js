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

  angular.module('pnc.common.pnc-client.jobs').factory('buildConfigCreator', [
    '$http',
    '$q',
    'restConfig',
    'messageBus',
    function ($http, $q, restConfig, messageBus) {

      const url = `${restConfig.getPncRestUrl()}/build-configs/create-with-scm`;

      function createWithScm(params) {
        validate(params);

        const deferred = $q.defer();

        $http({
          method: 'POST',
          url: url,
          data: params,
          successNotification: false
        }).then(resp => {
          if (resp.status === 201) {
            deferred.resolve(resp.data.buildConfig);
            return;
          }

          if (resp.status === 202) {
            const taskId = resp.data.taskId;

            if (taskId === null) {
              deferred.resolve(resp.data);
              return;
            }

            // Work around inconsistency in PNC WebSocket API and notify of job acceptance
            deferred.notify({
              job: 'BUILD_CONFIG_CREATION',
              progress: 'IN_PROGRESS',
              oldProgress: 'PENDING',
              taskId: taskId
            });

            // Notify caller of updates and finally resolve or reject promise on job completion.
            const unsubscribe = messageBus.onMessage(notification => {
              if (notification.job === 'BUILD_CONFIG_CREATION' && notification.taskId.toString() === taskId) {
                if (notification.progress === 'FINISHED') {
                  if (notification.notificationType === 'BC_CREATION_SUCCESS') {
                    deferred.resolve(notification);
                  } else {
                    deferred.reject(normalizeError(notification));
                  }
                  unsubscribe();
                } else {
                  deferred.notify(notification);
                }
              }
            });
          }
        }, errorResp => {
          deferred.reject(normalizeError(errorResp));
        });

        return deferred.promise;
      }

      /**
       * Normalizes the error response from either the http response or the websocket notification.
       * the standard format returned is:
       *
       * {
       *     source: '...' // Where the error came from, either 'http' or 'ws'.
       *     message: '...' // Human readable error message
       *     response: ? // The original response object received.
       * }
       *
       */
      function normalizeError(response) {
        let normalized = {
          response: response
        };

        // Error came from HTTP Request
        if (response.status && response.statusText) {
          normalized.source = 'http';
          normalized.message =  response.data.errorMessage;
        }

        // Error came from WebSocket
        if (response.job === 'BUILD_CONFIG_CREATION') {
          normalized.source = 'ws';
          normalized.message = response.data.message;
        }

        return normalized;
      }

      function validate(params) {
        const errors = [];

        if (!angular.isString(params.scmUrl)) {
          errors.push('[scmUrl: string]');
        }

        if(!angular.isObject(params.buildConfig)) {
          errors.push('[buildConfig: object]');
        }

        if (typeof params.preBuildSyncEnabled !== 'boolean') {
          errors.push('[preBuildSyncEnabled: boolean]');
        }

        if (errors.length > 0) {
          throw new TypeError(`buildConfigCreator.createWithScm() invoked without required params: ${errors.join()}`);
        }
      }

      return Object.freeze({
        createWithScm
      });
    }
  ]);

})();
