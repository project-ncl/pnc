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

  angular.module('pnc.common.pnc-client.message-bus').factory('messageBus', [
    'restConfig',
    '$rootScope',
    'events',
    'BuildResource',
    'GroupBuildResource',
    function (restConfig, $rootScope, events, BuildResource, GroupBuildResource) {

      const messageBus = new PncJsLibs.MessageBus(restConfig.getPncNotificationsUrl());

      function newBuildProgressEventHandler(eventType) {
        return (build, notification) => $rootScope.$broadcast(eventType, new BuildResource(build), notification);
      }

      messageBus.onBuildProgress('PENDING', newBuildProgressEventHandler(events.BUILD_PENDING));
      messageBus.onBuildProgress('IN_PROGRESS', newBuildProgressEventHandler(events.BUILD_IN_PROGRESS));
      messageBus.onBuildProgress('FINISHED', newBuildProgressEventHandler(events.BUILD_FINISHED));

      messageBus.onBuildProgressChange(newBuildProgressEventHandler(events.BUILD_PROGRESS_CHANGED));
      messageBus.onBuildStatusChange(newBuildProgressEventHandler(events.BUILD_STATUS_CHANGED));


      function newGroupBuildProgressEventHandler(eventType) {
        return (groupBuild, notification) => $rootScope.$broadcast(eventType, new GroupBuildResource(groupBuild), notification);
      }

      messageBus.onGroupBuildProgress('IN_PROGRESS', newGroupBuildProgressEventHandler(events.GROUP_BUILD_IN_PROGRESS));
      messageBus.onGroupBuildProgress('FINISHED', newGroupBuildProgressEventHandler(events.GROUP_BUILD_FINISHED));

      messageBus.onGroupBuildProgressChange(newGroupBuildProgressEventHandler(events.GROUP_BUILD_PROGRESS_CHANGED));
      messageBus.onGroupBuildStatusChange(newGroupBuildProgressEventHandler(events.GROUP_BUILD_STATUS_CHANGED));

      messageBus.onGenericSettingMaintenanceChanged(notification => {
        if (notification.message) {
          let message = JSON.parse(notification.message);
          if (message.maintenanceModeEnabled) {
            $rootScope.$broadcast(events.MAINTENANCE_MODE_ON);
          } else {
            $rootScope.$broadcast(events.MAINTENANCE_MODE_OFF);
          }
        }
      });

      messageBus.onGenericSettingNewAnnouncement(notification => {
        if (notification.message) {
          let message = JSON.parse(notification.message);
            $rootScope.$broadcast(events.NEW_ANNOUNCEMENT, message);
        }
      });


      messageBus.onScmRepositoryCreationSuccess(notification => {
        if (notification.scmRepository) {
            $rootScope.$broadcast(events.SCM_REPOSITORY_CREATION_SUCCESS, notification.scmRepository);
        }
      });

      messageBus.onBuildPushStatusChange((buildPushResult, notification) => {
        $rootScope.$broadcast(events.BUILD_PUSH_STATUS_CHANGE, buildPushResult, notification);
      });

      return messageBus;
    }
  ]);

})();
