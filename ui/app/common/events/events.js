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
      .module('pnc.common.events')
      .constant('events', Object.freeze({

        BUILD_PENDING: 'BUILD_PENDING',
        BUILD_IN_PROGRESS: 'BUILD_IN_PROGRESS',
        BUILD_FINISHED: 'BUILD_FINISHED',
        BUILD_PROGRESS_CHANGED: 'BUILD_PROGRESS_CHANGED',
        BUILD_STATUS_CHANGED: 'BUILD_STATUS_CHANGED',

        GROUP_BUILD_IN_PROGRESS: 'GROUP_BUILD_IN_PROGRESS',
        GROUP_BUILD_FINISHED: 'GROUP_BUILD_FINISHED',
        GROUP_BUILD_PROGRESS_CHANGED: 'GROUP_BUILD_PROGRESS_CHANGED',
        GROUP_BUILD_STATUS_CHANGED: 'GROUP_BUILD_STATUS_CHANGED',

        SCM_REPOSITORY_CREATION_SUCCESS: 'SCM_REPOSITORY_CREATION_SUCCESS',

        NEW_ANNOUNCEMENT: 'NEW_ANNOUNCEMENT',
        MAINTENANCE_MODE_ON: 'MAINTENANCE_MODE_ON',
        MAINTENANCE_MODE_OFF: 'MAINTENANCE_MODE_OFF',

        BUILD_PUSH_STATUS_CHANGE: 'BUILD_PUSH_STATUS_CHANGE'

      }));
})();
