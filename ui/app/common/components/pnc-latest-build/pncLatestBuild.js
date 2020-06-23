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
   * The component representing latest build for given Build Config or Group Config
   */
  angular.module('pnc.common.components').component('pncLatestBuild', {
    bindings: {
      /**
       * Object: The configuration representing Build Config
       */
      buildConfig: '<?',
      /**
       * Object: The configuration representing Group Config
       */
      groupConfig: '<?'
    },
    templateUrl: 'common/components/pnc-latest-build/pnc-latest-build.html',
    controller: ['BuildResource', 'GroupBuildResource', 'BuildConfigResource', Controller]
  });


  /*
   * This component requires extensive refactoring when BC refactor takes place
   */
  function Controller(BuildResource, GroupBuildResource, BuildConfigResource) {
    var $ctrl = this;

    $ctrl.isLoaded = false;

    function setLatestBuild(data) {
      $ctrl.latestBuild = data;
      $ctrl.isLoaded = true;
    }

    function loadLatestBuild() {
      var resultPromise;
      if ($ctrl.groupConfig) {
        resultPromise = GroupBuildResource.getLatestByGroupConfig({ id: $ctrl.groupConfig.id }).$promise.then(function (data) {
          setLatestBuild(_.isArray(data.content) ? data.content[0] : null);
        });
      } else if ($ctrl.buildConfig) {
        resultPromise = BuildResource.getLatestByConfig({ id: $ctrl.buildConfig.id }).$promise.then(function (data) {
          setLatestBuild(_.isArray(data.content) ? data.content[0] : null);
        });
      } else {
        console.error('pncLatestBuild: no configs available');
      }
      resultPromise.finally(function() {
        $ctrl.isLoaded = true;
      });
    }

    $ctrl.$onInit = function() {
      loadLatestBuild();

      BuildConfigResource.getLatestBuild({ id: $ctrl.buildConfig.id }).$promise.then(
        build => {
          console.log('latest build = %O', build);
        },
        err => {
          console.log('error fetching latest build: %O', err);
        }
      );
    };

  }
})();
