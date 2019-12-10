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
// @ts-check



(function () {
  'use strict';

  angular.module('pnc.common.pnc-client.message-bus').factory('messageBus', [
    'restConfig',
    '$rootScope',
    function (restConfig, $rootScope) {

      const messageBus = new PncJsLibs.MessageBus(restConfig.getPncNotificationsUrl());

      messageBus.onBuildStatusChange((build, notification) => {
         let payload = {
           id: parseInt(build.id),
           buildCoordinationStatus: build.status,
           userId: parseInt(build.user.id),
           buildConfigurationId: parseInt(build.buildConfigRevision.id),
           buildConfigurationName: build.buildConfigRevision.name,
           buildStartTime: build.startTime,
           buildEndTime: build.endTime
         };

         let eventType;

         if (notification.progress === 'IN_PROGRESS') {
           eventType = 'BUILD_STARTED';
         } else if (notification.progress === 'FINISHED') {
           eventType = 'BUILD_FINISHED';
         }

         $rootScope.$broadcast(eventType, payload);

         $rootScope.$broadcast('BUILD_STATUS_CHANGED', {
          id: payload.id,
          status: payload.buildCoordinationStatus,
          userId: payload.userId,
          buildConfigurationId: payload.buildConfigurationId,
          buildConfigurationName: payload.buildConfigurationName,
          startTime: payload.buildStartTime,
          endTime: payload.buildEndTime
        });

      });

      messageBus.onGroupBuildStatusChange((groupBuild, notification) => {

        let payload = {
          id: parseInt(groupBuild.id),
          buildStatus: groupBuild.status,
          userId: parseInt(groupBuild.user.id),
          buildSetConfigurationId: parseInt(groupBuild.groupConfig.id),
          buildSetConfigurationName: groupBuild.groupConfig.name,
          startTime: groupBuild.startTime,
          endTime: groupBuild.endTime
        };

        let eventType;

        if (notification.progress === 'IN_PROGRESS') {
          eventType = 'BUILD_SET_STARTED';
        } else if (notification.progress === 'FINISHED') {
          eventType = 'BUILD_SET_FINISHED';
        }

        $rootScope.$broadcast(eventType, payload);

        $rootScope.$broadcast('BUILD_SET_STATUS_CHANGED', {
          id: payload.id,
          status: payload.buildStatus,
          userId: payload.userId,
          buildConfigurationSetId: payload.buildSetConfigurationId,
          buildConfigurationSetName: payload.buildSetConfigurationName,
          startTime: payload.buildSetStartTime,
          endTime: payload.buildSetEndTime
        });
      });

      return messageBus;
    }
  ]);

})();
