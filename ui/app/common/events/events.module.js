/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.common.events', [
    'pnc.properties'
  ]);

  module.run([
    '$state',
    'events',
    'BuildResource',
    'authService',
    'pncNotify',
    'pncNotifyUser',
    'messageBus',
    function($state, events, BuildResource, authService, pncNotify, pncNotifyUser, messageBus) {

      function canonicalName(build) {
        return `${build.buildConfigRevision.name}#${build.id}`;
      }

      messageBus.onBuildProgress('IN_PROGRESS', build => {
        pncNotifyUser(build.user).info(`Build ${canonicalName(build)} IN PROGRESS`,`Build #${build.id}`, buildLinkCallback(build));
      });

      messageBus.onBuildStatus('SUCCESS', build => {
        pncNotifyUser(build.user).success(`Build ${canonicalName(build)} COMPLETED`,`Build #${build.id}`, buildLinkCallback(build));
      })

      messageBus.onBuildStatus('FAILED', build => {
        pncNotifyUser(build.user).warn(`Build ${canonicalName(build)} FAILED`, `Build #${build.id}`, buildLinkCallback(build));
      });

      messageBus.onBuildStatus('SYSTEM_ERROR', build => {

      });

      messageBus.onBuildProgress('FINISHED', build => {
        switch (build.status) {
          case '':
        }
        if (build.status === 'SUCCESS') {
          pncNotifyUser
        }
        pncNotifyUser(build.user).info

      });

      function buildLinkCallback(build) {
        return function() {
          $state.go('projects.detail.build-configs.detail.builds.detail.default', {
            projectId: build.project.id,
            configurationId: build.buildConfigRevision.id,
            buildId: build.id
          });
        };
      }






      //TODO: When backend functionality is available these notifications
      // should only be fired if the userId of the payload matches the
      // current logged in user.

      // scope.$on(eventTypes.BUILD_STARTED, function(event, payload) {

      //   authService.forUserId(payload.userId).then(function() {
      //     if (payload.buildCoordinationStatus === 'NEW') {
      //       pncNotify.info('Build ' + payload.buildConfigurationName + ' in new state',
      //                    'Build #' + payload.id, buildLinkCallback(payload.id));
      //     } else if (payload.buildCoordinationStatus === 'WAITING_FOR_DEPENDENCIES') {
      //       pncNotify.info('Build ' + payload.buildConfigurationName + ' waiting for dependencies',
      //                    'Build #' + payload.id, buildLinkCallback(payload.id));
      //     } else if (payload.buildCoordinationStatus === 'ENQUEUED') {
      //       pncNotify.info('Build ' + payload.buildConfigurationName + ' was enqueued',
      //                    'Build #' + payload.id, buildLinkCallback(payload.id));
      //     } else if (payload.buildCoordinationStatus === 'BUILDING') {
      //       pncNotify.info('Build ' + payload.buildConfigurationName + ' is being built',
      //                    'Build #' + payload.id, buildLinkCallback(payload.id));
      //     }
      //   });
      // });

      // Notify user when builds finish
      // (see events-services.js for the conversion
      // between server and client buildCoordinationStatus)
      // scope.$on(eventTypes.BUILD_FINISHED, function(event, payload) {

      //   authService.forUserId(payload.userId).then(function() {
      //     if (payload.buildCoordinationStatus === 'REJECTED') {
      //       pncNotify.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' rejected.',
      //                      'Build #' + payload.id, buildLinkCallback(payload.id));
      //     } else if (payload.buildCoordinationStatus === 'REJECTED_ALREADY_BUILT') {
      //       pncNotify.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' was rejected because already built.',
      //                      'Build #' + payload.id, buildLinkCallback(payload.id));
      //     } else if (payload.buildCoordinationStatus === 'SYSTEM_ERROR') {
      //       pncNotify.error('A system error prevented the Build ' + payload.buildConfigurationName + '#' + payload.id + ' from starting.',
      //                       'Build #' + payload.id, buildLinkCallback(payload.id));
      //     } else {
      //       BuildResource.get({buildId: payload.id}).$promise.then(
      //         function (result) {
      //           if (result.status === 'BUILD_COMPLETED' || result.status === 'DONE' || result.status === 'SUCCESS') {
      //             pncNotify.success('Build ' + payload.buildConfigurationName + '#' + payload.id + ' completed',
      //                               'Build #' + payload.id, buildLinkCallback(payload.id));
      //           } else if (result.status === 'CANCELLED') {
      //             pncNotify.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' cancelled',
      //                            'Build #' + payload.id, buildLinkCallback(payload.id));
      //           } else {
      //             pncNotify.warn('Build ' + payload.buildConfigurationName + '#' + payload.id + ' failed',
      //                            'Build #' + payload.id, buildLinkCallback(payload.id));
      //           }
      //         }
      //       );
      //     }
      //   });
      // });
    }
  ]);

})();
