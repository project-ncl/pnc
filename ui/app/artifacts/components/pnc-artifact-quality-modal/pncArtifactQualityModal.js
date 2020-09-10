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

  angular.module('pnc.artifacts').component('pncArtifactQualityModal', {
    bindings: {
     /**
      * All bindings are satisfied by ui-bootstrap modal, for more details
      * see modal section of: https://angular-ui.github.io/bootstrap/
      *
      * This component should be invoked by using the service functions in ArtifactModals.js
      */
     close: '&',
     dismiss: '&',
     modalInstance: '<',
     /**
      * Contains resolves passed to this component when the modal is opened using $uibModal service.
      * An object with property `params` must be passed as a resolve. The params object should have the
      * following properties:
      *
      * Title: String
      * The title of the modal window
      *
      * Quality: String
      * The currently selected quality level
      *
      * onSave: Function
      * Invoked when the user clicks the save button, it will be passed an object with the properties:
      * `quality` and `reason` (both strings). Whatever is returned from this callback will be used as the final
      * value for modal object (again see $uibModal for details). The return value can be a promise.
      */
     resolve: '<'
    },
    templateUrl: 'artifacts/components/pnc-artifact-quality-modal/pnc-artifact-quality-modal.html',
    controller: ['ArtifactQualityLevels', '$q', Controller]
  });


  function Controller(ArtifactQualityLevels, $q) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.save = save;
    $ctrl.cancel = cancel;

    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.title = $ctrl.resolve.params.title;
      $ctrl.quality = $ctrl.resolve.params.quality;
      $ctrl.onSave = $ctrl.resolve.params.onSave;

      $ctrl.qualityLevels = ArtifactQualityLevels.getAuthorizedLevelsForCurrentUser();
    };

    function save() {
      const result = $ctrl.onSave({
        quality: $ctrl.quality,
        reason: $ctrl.reason
      });

      $q.when(result)
        .then(r => $ctrl.close({ $value: r }));
    }

    function cancel() {
      $ctrl.dismiss();
    }

  }

})();
