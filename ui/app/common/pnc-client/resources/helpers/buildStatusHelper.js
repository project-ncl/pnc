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

  angular
      .module('pnc.common.pnc-client.resources')
      .factory('buildStatusHelper', [
        function () {

          var PENDING = 'PENDING';
          var IN_PROGRESS = 'IN_PROGRESS';
          var FINISHED = 'FINISHED';

          var buildStatus = Object.freeze({
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
            'REJECTED':{
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
            'DONE_WITH_ERRORS': {
              progress: FINISHED,
              failed: true
            },
            'CANCELLED': {
              progress: FINISHED,
              failed: true
            }
          });

          function getStatus(statusOrResource) {
            if (angular.isString(statusOrResource)) {
              return statusOrResource;
            } else if (angular.isObject(statusOrResource)) {
              return statusOrResource.status;
            }
          }

          function isPending(buildRecord) {
            var status = getStatus(buildRecord);

            return buildStatus[status].progress === PENDING;
          }

          function isInProgress(buildRecord) {
            var status = getStatus(buildRecord);

            return buildStatus[status].progress === IN_PROGRESS;
          }

          function isFinished(buildRecord) {
            var status = getStatus(buildRecord);

            return buildStatus[status].progress === FINISHED;
          }

          function isFailed(buildRecord) {
            var status = getStatus(buildRecord);

            return buildStatus[status].failed;
          }

          function isSuccess(buildRecord) {
            return !isFailed(buildRecord);
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
