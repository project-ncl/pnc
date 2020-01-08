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

    /**
     * The component representing builds history for given Build Configuration or Build Set Configuration
     */
    angular.module('pnc.common.components').component('pncBuildsHistory', {
      bindings: {
        /**
         * Object: The configuration representing Build Configuration
         */
        buildConfig: '<'
      },
      templateUrl: 'common/components/pnc-builds-history/pnc-builds-history.html',
      controller: ['events', '$scope', 'BuildRecord', 'BuildConfigurationSet', 'paginator', Controller]
    });

    function Controller(events, $scope, BuildRecord, BuildConfigurationSet, paginator) {
      var $ctrl = this;

      function loadBuildsHistory() {
        ($ctrl.buildGroup ? BuildConfigurationSet.queryBuildConfigSetRecords({
          id: $ctrl.buildGroup.id,
          pageSize: 10
        }) : BuildRecord.getByConfiguration({ // it will be refactored when working on BuildConfigResource (NCL-4198)
          id: $ctrl.buildConfig.id,
          pageSize: 10
        })).$promise.then(function (page) {
          $ctrl.page = paginator(page);
        });
      }

      function processEvent(event, entity) {
        if (($ctrl.buildGroup  && entity.groupConfig.id === $ctrl.buildGroup.id.toString() ) ||
            ($ctrl.buildConfig && entity.buildConfigRevision.id  === $ctrl.buildConfig.id.toString())) {

          $ctrl.page.refresh();
        }
      }

      $ctrl.$onInit = function() {
        loadBuildsHistory();

        if ($ctrl.buildGroup) {
          $scope.$on(events.GROUP_BUILD_IN_PROGRESS, processEvent);
          $scope.$on(events.GROUP_BUILD_FINISHED, processEvent);
        } else {
          $scope.$on(events.BUILD_PENDING, processEvent);
          $scope.$on(events.BUILD_IN_PROGRESS, processEvent);
          $scope.$on(events.BUILD_FINISHED, processEvent);
        }
      };

    }

  })();
