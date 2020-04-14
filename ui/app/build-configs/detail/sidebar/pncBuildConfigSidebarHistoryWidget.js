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

  angular.module('pnc.build-configs').component('pncBuildConfigSidebarHistoryWidget', {
    bindings: {
      buildConfig: '<',
      builds: '<'
    },
    templateUrl: 'build-configs/detail/sidebar/pnc-build-config-sidebar-history-widget.html',
    controller: ['paginator', '$scope', 'events', Controller]
  });


  function Controller(paginator, $scope, events) {
    const $ctrl = this;

    // -- Controller API --



    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.page = paginator($ctrl.builds);

      $scope.$on(events.BUILD_STATUS_CHANGED, (e, build) => {
        if (build.buildConfigRevision.id !== $ctrl.buildConfig.id.toString()) {
          return;
        }

        $scope.$applyAsync(() => {
          const index = $ctrl.page.data.findIndex(b => b.id === build.id);

          if (index > -1) {
            $ctrl.page.data.splice(index, 1, build);
          } else {
            $ctrl.page.data.unshift(build);
            $ctrl.page.data.sort((a, b) => a.submitTime - b.submitTime);

            if ($ctrl.page.data.length > $ctrl.page.size) {
              $ctrl.page.data.pop();
            }
          }
        });
      });
    };
  }

})();
