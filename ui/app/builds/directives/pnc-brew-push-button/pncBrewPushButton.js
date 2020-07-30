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

  angular.module('pnc.builds').component('pncBrewPushButton', {
    bindings: {
      build: '<?',
      groupBuild: '<?'
    },
    templateUrl: 'builds/directives/pnc-brew-push-button/pnc-brew-push-button.html',
    controller: [
      '$state',
      '$uibModal',
      'pncNotify',
      'BuildResource',
      'GroupBuildResource',
      'EntityRecognizer',
      Controller
    ]
  });

  function Controller($state, $uibModal, pncNotify, BuildResource, GroupBuildResource, EntityRecognizer) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.isButtonVisible = isButtonVisible;
    $ctrl.openTagNameModal = openTagNameModal;

    // --------------------

    function isBuild() {
      if (angular.isUndefined($ctrl.build)) {
        return false;
      }
      return EntityRecognizer.isBuild($ctrl.build);
    }

    function isGroupBuild() {
      if (angular.isUndefined($ctrl.groupBuild)) {
        return false;
      }
      return EntityRecognizer.isGroupBuild($ctrl.groupBuild);
    }

    function isButtonVisible() {
      if (isBuild()) {
        return $ctrl.build.$isSuccess();
      } else if (isGroupBuild()) {
        return GroupBuildResource.isSuccess($ctrl.groupBuild);
      }
    }

    function openTagNameModal() {
      var modal = $uibModal.open({
        animation: true,
        backdrop: 'static',
        component: 'pncEnterBrewTagNameModal',
        size: 'md'
      });

      modal.result.then(function (modalValues) {
        return isBuild() ? doPushBuild(modalValues) : doPushGroupBuild(modalValues);
      });
    }

    function doPushBuild(modalValues) {
      BuildResource.brewPush({ id: $ctrl.build.id }, { tagPrefix: modalValues.tagName }).$promise
          .then(
            res => {
              console.info('Initiated brew push of build %s - response: %O', $ctrl.build.$canonicalName(), res);
              gotoBuildBrewPushPage();
            },
            err => {
              console.error('Brew push error for build %s - response: %O', $ctrl.build.$canonicalName(), err);
              if (err.status === 409) {
                gotoBuildBrewPushPage();
              }
            }
          );
    }

    function doPushGroupBuild(modalValues) {
      GroupBuildResource.brewPush({ id: $ctrl.groupBuild.id }, { tagPrefix: modalValues.tagName }).$promise
           .then(
             () => pncNotify.info(`Initiated brew push of GroupBuild: ${$ctrl.groupBuild.$canonicalName()}`),
             err => console.error('Brew push GroupBuild error: %O', err)
           );
    }

    function gotoBuildBrewPushPage() {
      $state.go('projects.detail.build-configs.detail.builds.detail.brew-push',
                {
                  projectId: $ctrl.build.project.id,
                  configurationId: $ctrl.build.buildConfigRevision.id,
                  buildId: $ctrl.build.id
                }, {
                  reload: true
                }
              );
    }
  }

})();
