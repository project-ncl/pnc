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

  angular.module('pnc.build-records').component('pncBuildRecordPushButton', {
    bindings: {
      buildRecord: '<?',
      buildGroupRecord: '<?'
    },
    templateUrl: 'build-records/directives/pnc-build-record-push-button/pnc-build-record-push-button.html',
    controller: ['$uibModal', 'pncNotify', 'BuildRecord', 'BuildConfigSetRecord', 'messageBus', Controller]
  });

  function Controller($uibModal, pncNotify, BuildRecord, BuildConfigSetRecord, messageBus) {
    var $ctrl = this,
        unsubscribes = [];

    // -- Controller API --

    $ctrl.isButtonVisible = isButtonVisible;
    $ctrl.openTagNameModal = openTagNameModal;

    // --------------------

    function isBuildRecord() {
      return angular.isDefined($ctrl.buildRecord);
    }

    function isBuildGroupRecord() {
      return angular.isDefined($ctrl.buildGroupRecord);
    }

    function isButtonVisible() {
      if (isBuildRecord()) {
        return $ctrl.buildRecord.$isSuccess();
      } else if (isBuildGroupRecord()) {
        return BuildConfigSetRecord.isSuccess($ctrl.buildGroupRecord);
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
        return isBuildRecord() ? doPushBuildRecord(modalValues) : doPushBuildGroupRecord(modalValues);
      });
    }

    function subscribe(statuses) {
      statuses.forEach(function (status) {
        unsubscribes.push(messageBus.subscribe({
          topic: 'causeway-push',
          id: status.id
        }));
      });
    }

    function notify(statusObj) {
      switch (statusObj.status) {
        case 'ACCEPTED':
          pncNotify.info('Brew push initiated for build: ' + statusObj.name + '#' + statusObj.id);
          break;
        case 'FAILED':
        case 'SYSTEM_ERROR':
        case 'REJECTED':
          pncNotify.error('Brew push failed for build: ' + statusObj.name + '#' + statusObj.id);
          break;
        case 'CANCELED':
          pncNotify.info('Brew push canceled for build: ' + statusObj.name + '#' + statusObj.id);
          break;
      }
    }

    function filterAccepted(statuses) {
      return statuses.filter(function (status) {
        return status === 'ACCEPTED';
      });
    }

    function filterRejected(statuses) {
      return statuses.filter(function (status) {
        return status !== 'ACCEPTED';
      });
    }

    function doPushBuildRecord(modalValues) {
      BuildRecord.push($ctrl.buildRecord.id, modalValues.tagName).then(function (response) {
        subscribe(response.data);
        notify(response.data[0]);
      });
    }

    function doPushBuildGroupRecord(modalValues) {
      BuildConfigSetRecord.push($ctrl.buildGroupRecord.id, modalValues.tagName).then(function (response) {
        var accepted = filterAccepted(response.data),
            rejected = filterRejected(response.data);

        if (accepted.length > 0) {
          subscribe(accepted);
        }

        if (rejected.length === 0) {
          pncNotify.info('Brew push initiated for group build: ' + BuildConfigSetRecord.canonicalName($ctrl.buildGroupRecord));
        } else {
          pncNotify.warn('Some Build Records were rejected for brew push of group build: ' + BuildConfigSetRecord.canonicalName($ctrl.buildGroupRecord));
          rejected.forEach(function (reject) {
            notify(reject);
          });
        }
      });
    }
  }

})();
