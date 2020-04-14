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
    controller: ['BuildRecord', 'BuildConfigurationSetDAO', Controller]
  });


  /*
   * This component requires extensive refactoring when BC refactor takes place
   */
  function Controller(BuildRecord, BuildConfigurationSetDAO) {
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

    $ctrl.$onInit = function() {
      loadLatestBuild();

    };

  }
})();
