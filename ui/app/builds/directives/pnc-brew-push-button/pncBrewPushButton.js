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
    controller: ['$uibModal', 'pncNotify', 'BuildResource', 'GroupBuildResource', 'messageBus', 'EntityRecognizer', Controller]
  });

  function Controller($uibModal, pncNotify, BuildResource, GroupBuildResource, messageBus, EntityRecognizer) {
    var $ctrl = this;
        //unsubscribes = [];

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

    // function subscribe(status) {
    //   unsubscribes.push(messageBus.subscribe({
    //     topic: 'causeway-push',
    //     id: status.id
    //   }));
    // }

    // function notify(statusObj) {
    //   switch (statusObj.status) {
    //     case 'ACCEPTED':
    //       pncNotify.info('Brew push initiated for build: ' + statusObj.name + '#' + statusObj.id);
    //       break;
    //     case 'FAILED':
    //     case 'SYSTEM_ERROR':
    //     case 'REJECTED':
    //       pncNotify.error('Brew push failed for build: ' + statusObj.name + '#' + statusObj.id);
    //       break;
    //     case 'CANCELED':
    //       pncNotify.info('Brew push canceled for build: ' + statusObj.name + '#' + statusObj.id);
    //       break;
    //   }
    // }

    // function filterAccepted(statuses) {
    //   return statuses.filter(function (status) {
    //     return status.status === 'ACCEPTED';
    //   });
    // }

    // function filterRejected(statuses) {
    //   return statuses.filter(function (status) {
    //     return status.status !== 'ACCEPTED';
    //   });
    // }

    function doPushBuild(modalValues) {
      BuildResource.brewPush({ id: $ctrl.build.id }, { tagPrefix: modalValues.tagName }).$promise
          .then(
            res => console.info('Initiated brew push of build %s - response: %O', $ctrl.build.$canonicalName(), res),
            err => console.error('Brew push error for build %s - response: %O', $ctrl.build.$canonicalName(), err)
          );



      // $ctrl.build
      //      .$brewPush({}, { tagPrefix: modalValues.tagName })
      //      .then(
      //         res => console.log('Brew push build result: %O', res),
      //         err => console.error('Brew push build error: %O', err)
      //       );

      // BuildResource.brewPush({
      //   id: $ctrl.build.id
      // }, {
      //   tagPrefix: modalValues.tagName
      // }).$promise.then(function (response) {
      //   subscribe(response);
      //   notify(response);
      // });
    }

    function doPushGroupBuild(modalValues) {
      GroupBuildResource.brewPush({ id: $ctrl.groupBuild.id }, { tagPrefix: modalValues.tagName }).$promise
           .then(
             () => pncNotify.info(`Initiated brew push of GroupBuild: ${$ctrl.groupBuild.$canonicalName()}`), //(console.log('Brew push GroupBuild result: %O', res),
             err => console.error('Brew push GroupBuild error: %O', err)
           );
      // GroupBuildResource.brewPush($ctrl.groupBuild.id, modalValues.tagName).then(function (response) {
      //   const accepted = filterAccepted(response.data),
      //         rejected = filterRejected(response.data);

      //   if (accepted.length > 0) {
      //     subscribe(accepted);
      //   }

      //   if (rejected.length === 0) {
      //     pncNotify.info('Brew push initiated for Group Build: ' + GroupBuildResource.canonicalName($ctrl.groupBuild));
      //   } else {
      //     pncNotify.warn('Some Builds were rejected for brew push of Group Build: ' + GroupBuildResource.canonicalName($ctrl.groupBuild));
      //     rejected.forEach(function (reject) {
      //       notify(reject);
      //     });
      //   }
      // });
    }
  }

})();
