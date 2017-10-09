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
  
    /**
     * The component representing builds history for given Build Configuration or Build Set Configuration
     */
    angular.module('pnc.common.components').component('pncBuildsHistory', {
      bindings: {
        /**
         * Object: The configuration representing Build Configuration
         */
        buildConfig: '<?',
        /**
         * Object: The configuration representing Build Group Configuration
         */
        buildGroup: '<?'
      },
      templateUrl: 'common/components/pnc-builds-history/pnc-builds-history.html',
      controller: ['eventTypes', '$scope', 'BuildRecord', 'BuildConfigurationSet', 'paginator', Controller]
    });
  
    function Controller(eventTypes, $scope, BuildRecord, BuildConfigurationSet, paginator) {
      var $ctrl = this;

      function loadBuildsHistory() {
        ($ctrl.buildGroup ? BuildConfigurationSet.queryBuildConfigSetRecords({ 
          id: $ctrl.buildGroup.id,
          pageSize: 10
        }) : BuildRecord.getByConfiguration({ 
          id: $ctrl.buildConfig.id,
          pageSize: 10
        })).$promise.then(function (page) {
          $ctrl.page = paginator(page);
        });
      }

      function processEvent(event, payload) {
        if (($ctrl.buildGroup  && payload.buildSetConfigurationId === $ctrl.buildGroup.id ) || 
            ($ctrl.buildConfig && payload.buildConfigurationId    === $ctrl.buildConfig.id)) {
          $ctrl.page.refresh();
        }
      }

      $ctrl.$onInit = function() {
        loadBuildsHistory();
  
        if ($ctrl.buildGroup) {
          $scope.$on(eventTypes.BUILD_SET_STARTED, processEvent);
          $scope.$on(eventTypes.BUILD_SET_FINISHED, processEvent);
        } else {
          $scope.$on(eventTypes.BUILD_STARTED, processEvent);
          $scope.$on(eventTypes.BUILD_FINISHED, processEvent);
        }
      };

    }

  })();