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
      buildGroupRecord: '<?',
      size: '@?',
      buttonText: '@?',
      buttonIconClass: '@?'
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

    $ctrl.$onDestroy = function () {
      unsubscribes.forEach(function (unsubscribe) {
        unsubscribe();
      });
    };

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

    function subscribe(ids) {
      Object.keys(ids).forEach(function (id) {
        unsubscribes.push(messageBus.subscribe({
          topic: 'causeway-push',
          id: id
        }));
      });
    }

    function doPushBuildRecord(modalValues) {
      BuildRecord.push($ctrl.buildRecord.id, modalValues.tagName).then(function (result) {
        subscribe(result.data);
        pncNotify.info('Brew push process started for: ' + $ctrl.buildRecord.$canonicalName());
      });
    }

    function doPushBuildGroupRecord(modalValues) {
      BuildConfigSetRecord.push($ctrl.buildGroupRecord.id, modalValues.tagName).then(function (result) {
        subscribe(result.data);
        pncNotify.info('Brew push process started for group: ' + BuildConfigSetRecord.canonicalName($ctrl.buildGroupRecord));
      });
    }
  }

})();
