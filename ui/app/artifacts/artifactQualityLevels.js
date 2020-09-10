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

  angular.module('pnc.artifacts').factory('ArtifactQualityLevels', [
    'authService',
    function (authService) {

      /**
       * Any user can change an artifact to these quality levels
       */
      const QUALITY_LEVELS_UNPRIVILEGED = [
        'NEW',
        'VERIFIED',
        'TESTED',
        'DEPRECATED'
      ];

      /**
       * Only super users can change an artifact to these quality levels
       */
      const QUALITY_LEVELS_PRIVILEGED = [
        'BLACKLISTED',
        'DELETED'
      ];

      /**
       * These quality levels are set by the PNC system itself, an artifact
       * cannot be changed to or from one of these levels.
       */
      const QUALITY_LEVELS_SYSTEM_ONLY = [
        'TEMPORARY'
      ];


      function getUnprivileged() {
        return Object.freeze(QUALITY_LEVELS_UNPRIVILEGED);
      }

      function getPrivileged() {
        return Object.freeze(QUALITY_LEVELS_PRIVILEGED);
      }

      function getSystemOnly() {
        return Object.freeze(QUALITY_LEVELS_SYSTEM_ONLY);
      }

      function getAuthorizedLevelsForCurrentUser() {
        if (!authService.isAuthenticated) {
          return [];
        }

        if (authService.isSuperUser()) {
          return Object.freeze(getPrivileged().concat(getUnprivileged()));
        }

        return getUnprivileged();
      }


      return Object.freeze({
        getUnprivileged,
        getPrivileged,
        getSystemOnly,
        getAuthorizedLevelsForCurrentUser
      });

    }

  ]);

})();
