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
   * The component representing latest build for given Build Configuration or Build Group Configuration
   */
  angular.module('pnc.common.components').component('pncLatestBuild', {
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
    templateUrl: 'common/components/pnc-latest-build/pnc-latest-build.html',
    controller: ['eventTypes', '$scope', 'BuildRecord', 'BuildConfigurationSetDAO', 'UserDAO', Controller]
  });


  /*
   * This component requires extensive refactoring when BC refactor takes place
   */
  function Controller(eventTypes, $scope, BuildRecord, BuildConfigurationSetDAO, UserDAO) {
    var $ctrl = this;

    $ctrl.isLoaded = false;

    function setLatestBuild(data) {
      $ctrl.latestBuild = data;
      $ctrl.isLoaded = true;
    }

    function loadLatestBuild() {
      var resultPromise;
      if ($ctrl.buildGroup) {
        resultPromise = BuildConfigurationSetDAO.getLatestBuildConfigSetRecordsForConfigSet({ configurationSetId: $ctrl.buildGroup.id }).then(function (data) {
          setLatestBuild(_.isArray(data) ? data[0] : null);
        });
      } else {
        // it will be refactored when working on BuildConfigResource (NCL-4198)
        resultPromise = BuildRecord.getLastByConfiguration({ id: $ctrl.buildConfig.id }).$promise.then(function (data) {
          setLatestBuild(_.isArray(data.content) ? data.content[0] : null);
        });
      }
      resultPromise.finally(function() {
        $ctrl.isLoaded = true;
      });
    }

    function updateLatestBuild(id, status, startTime, endTime, userId) {
      if ($ctrl.latestBuild && id && status) {
        $ctrl.latestBuild.id        = id;
        $ctrl.latestBuild.status    = status;
        $ctrl.latestBuild.startTime = startTime; // when building
        $ctrl.latestBuild.endTime   = endTime;   // when finished

        // todo NCL-3085
        if ($ctrl.latestBuild.userId !== userId) {
          $ctrl.latestBuild.userId = userId;

          UserDAO.get({ userId: userId }).$promise.then(function(data) {
            $ctrl.latestBuild.username = data.username;
          });
        }

      } else {
        loadLatestBuild();
      }
    }

    function processLatestBuild(event, payload) {
      if ($ctrl.buildGroup && payload.buildSetConfigurationId === $ctrl.buildGroup.id) {
        updateLatestBuild(payload.id, payload.buildStatus, payload.buildSetStartTime, payload.buildSetEndTime, payload.userId);
      } else if ($ctrl.buildConfig && payload.buildConfigurationId === $ctrl.buildConfig.id) {
        updateLatestBuild(payload.id, payload.buildCoordinationStatus, payload.buildStartTime, payload.buildEndTime, payload.userId);
      }
    }

    $ctrl.$onInit = function() {
      loadLatestBuild();

    };

  }
})();
