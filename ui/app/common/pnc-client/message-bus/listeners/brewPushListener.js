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

  angular.module('pnc.common.pnc-client.message-bus').factory('brewPushListener', [
    '$rootScope',
    '$state',
    'pncNotify',
    'BuildResource',
    function ($rootScope, $state, pncNotify, BuildResource) {

      function notify(build, pushStatus) {

        function navigateToPushResult() {
          $state.go('projects.detail.build-configs.detail.builds.detail.brew-push', { buildId: build.id });
        }

        function doNotify(type, message) {
          pncNotify[type](message, 'View Result', navigateToPushResult);
        }

        switch(pushStatus) {
          case 'SUCCESS':
            doNotify('success', 'Brew push completed for build: ' + build.$canonicalName());
            break;
          case 'FAILED':
          case 'SYSTEM_ERROR':
            doNotify('error', 'Brew push failed for build: ' + build.$canonicalName());
            break;
          case 'CANCELED':
            doNotify('info', 'Brew push cancelled for build: ' + build.$canonicalName());
        }
      }

      return function (message) {
        if (message.eventType === 'BREW_PUSH_RESULT') {
          $rootScope.$broadcast('BREW_PUSH_RESULT', message);

          if (message.buildId) {
            BuildResource.get({ id: message.buildId }).$promise.then(function (build) {
              notify(build, message.status);
            });
          }

        }
      };
    }
  ]);

})();
