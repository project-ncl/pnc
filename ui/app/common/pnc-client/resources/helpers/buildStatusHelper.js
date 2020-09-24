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

  angular
      .module('pnc.common.pnc-client.resources')
      .factory('buildStatusHelper', [
        function () {

          const PENDING = 'PENDING';
          const IN_PROGRESS = 'IN_PROGRESS';
          const FINISHED = 'FINISHED';

          const buildStatus = Object.freeze({
            'NEW': {
              progress: PENDING,
              failed: false
            },
            'ENQUEUED': {
              progress: PENDING,
              failed: false
            },
            'WAITING_FOR_DEPENDENCIES': {
              progress: PENDING,
              failed: false
            },
            'BUILDING': {
              progress: IN_PROGRESS,
              failed: false
            },
            'BUILD_COMPLETED': {
              progress: IN_PROGRESS,
              failed: false
            },
            'DONE': {
              progress: FINISHED,
              failed: false
            },
            'REJECTED': {
              progress: FINISHED,
              failed: true
            },
            'REJECTED_FAILED_DEPENDENCIES': {
              progress: FINISHED,
              failed: true
            },
            'REJECTED_ALREADY_BUILT': {
              progress: FINISHED,
              failed: false
            },
            'SYSTEM_ERROR': {
              progress: FINISHED,
              failed: true
            },
            'FAILED': {
              progress: FINISHED,
              failed: true
            },
            'NO_REBUILD_REQUIRED': {
              progress: FINISHED,
              failed: false
            },
            'DONE_WITH_ERRORS': {
              progress: FINISHED,
              failed: true
            },
            'CANCELLED': {
              progress: FINISHED,
              failed: true
            },
            'SUCCESS': {
              progress: FINISHED,
              failed: false
            }
          });

          function getStatus(statusOrResource) {
            if (angular.isString(statusOrResource)) {
              return statusOrResource;
            } else if (angular.isObject(statusOrResource)) {
              return statusOrResource.status;
            } else {
              console.error('Status was not recognized: ' + statusOrResource);
            }
          }

          function isPending(build) {
            const status = getStatus(build);
            return buildStatus[status].progress === PENDING;
          }

          function isInProgress(build) {
            const status = getStatus(build);
            return buildStatus[status].progress === IN_PROGRESS;
          }

          function isFinished(build) {
            const status = getStatus(build);
            return buildStatus[status].progress === FINISHED;
          }

          function isFailed(build) {
            const status = getStatus(build);
            return buildStatus[status].failed;
          }

          function isSuccess(build) {
            const status = getStatus(build);
            return status === 'SUCCESS';
          }


          return Object.freeze({
            isPending: isPending,
            isInProgress: isInProgress,
            isFinished: isFinished,
            isFailed: isFailed,
            isSuccess: isSuccess
          });
        }
      ]);


})();
