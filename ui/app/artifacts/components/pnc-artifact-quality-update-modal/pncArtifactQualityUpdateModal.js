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

  angular.module('pnc.artifacts').component('pncArtifactQualityUpdateModal', {
    bindings: {
     close: '&',
     dismiss: '&',
     modalInstance: '<',
     resolve: '<'
    },
    templateUrl: 'artifacts/components/pnc-artifact-quality-update-modal/pnc-artifact-quality-update-modal.html',
    controller: ['ArtifactResource', Controller]
  });


  function Controller(ArtifactResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.save = save;
    $ctrl.cancel = cancel;

    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.artifact = angular.copy($ctrl.resolve.artifact);
      $ctrl.quality = $ctrl.artifact.artifactQuality;
      $ctrl.qualityLevels = [
        'NEW',
        'VERIFIED',
        'TESTED',
        'DEPRECATED',
        'BLACKLISTED',
        'DELETED'
      ];
    };

    function save() {
      ArtifactResource.changeQuality({
        id: $ctrl.artifact.id,
        quality: $ctrl.quality,
        reason: $ctrl.reason
      })
      .$promise
      .then(() => ArtifactResource.get({ id: $ctrl.artifact.id }).$promise)
      .then(artifact => $ctrl.close({ $value: artifact }));
    }

    function cancel() {
      $ctrl.dismiss();
    }

  }

})();
