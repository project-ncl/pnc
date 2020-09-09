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

  angular.module('pnc.artifacts').factory('artifactQualityLevels', [
    function () {

      const qualityLevels = {

        'NEW': {
          description: 'The artifact has not yet been verified or tested.',
          requireRole: 'pnc-user',
          labelClass: 'label-default',
          editable: true
        },

        'VERIFIED': {
          description: 'The artifact has been verified by an automated process, but has not yet been tested against a complete product or other large set of components.',
          requireRole: 'pnc-user',
          labelClass: 'label-primary',
          editable: true
        },

        'TESTED': {
          description: 'The artifact has passed integration testing.',
          requireRole: 'pnc-user',
          labelClass: 'label-success',
          editable: true
        },

        'DEPRECATED': {
          description: 'The artifact should no longer be used due to lack of support and/or a better alternative being available.',
          requireRole: 'pnc-user',
          labelClass: 'label-warning',
          editable: true
        },

        'BLACKLISTED': {
          description: 'The artifact contains a severe defect, possibly a functional or security issue.',
          requireRole: 'pnc-system-user',
          labelClass: 'label-danger',
          editable: true
        },

        'DELETED': {
          description: 'Artifact with DELETED quality is used to show BuildRecord dependencies although the artifact itself was deleted OR can identify artifacts, which are were removed from repository manager (e.g. due to conflicts), but the metadata were kept for archival purposes.',
          requireRole: 'pnc-system-user',
          labelClass: 'label-danger',
          editable: true
        },

        'TEMPORARY': {
          description: 'The artifact is built as temporary and it is planned to remove it later. The artifact cannot be used for product releases.',
          labelClass: 'label-info',
          editable: false
        }

      };

      return qualityLevels;
    }

  ]);

})();
