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
   * Displays a table of Artifacts.
   *
   * Example usage:
   *
   * <pnc-artifacts-list artifact="$ctrl.artifact" on-update="$ctrl.update($newValue, $oldValue)"></pnc-artifacts-list>"
   */
  angular.module('pnc.artifacts').component('pncArtifactsList', {
    bindings: {
      /**
       * Array: The list of Artifacts to display.
       */
      artifacts: '<',

      /**
       * Callback Function: invoked with the new and old artifact after any changes.
       * See usage example above.
       */
      onUpdate: '&'
    },
    templateUrl: 'artifacts/list/pnc-artifacts-list.html',
    controller: ['ArtifactModals', Controller]
  });


  function Controller(ArtifactModals) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.changeQuality = changeQuality;
    $ctrl.showQualityRevisions = showQualityRevisions;

    // --------------------


    $ctrl.$onInit = function () {
    };

    function changeQuality(artifact) {
      ArtifactModals.newArtifactQualityModal(artifact)
          .result
          .then(artifact => updateArtifact(artifact));
    }

    function showQualityRevisions(artifact) {
      ArtifactModals.newArtifactRevisionsModal(artifact);
    }

    function updateArtifact(artifact) {
      const index = $ctrl.artifacts.findIndex(a => artifact.id === a.id);
      const old = $ctrl.artifacts[index];

      if (index >= 0) {
        $ctrl.artifacts.splice(index, 1, artifact);
        if ($ctrl.onUpdate) {
          $ctrl.onUpdate({ $newValue: artifact, $oldValue: old });
        }
      }
    }

  }

})();
