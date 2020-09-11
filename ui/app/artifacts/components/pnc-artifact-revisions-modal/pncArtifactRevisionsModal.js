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

  angular.module('pnc.artifacts').component('pncArtifactRevisionsModal', {
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
      * The following resolves are required:
      *
      * resolve.title {string} The title to display in the modal window
      * resolve.revisions {Page<ArtifactRevision> | Promise<Page<ArtifactRevision>} The page of artifact revisions to display
      */
     resolve: '<'
    },
    templateUrl: 'artifacts/components/pnc-artifact-revisions-modal/pnc-artifact-revisions-modal.html',
    controller: [Controller]
  });


  function Controller() {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.cancel = cancel;

    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.title = $ctrl.resolve.title;
      $ctrl.revisions = $ctrl.resolve.revisions;
    };

    function cancel() {
      $ctrl.dismiss();
    }

  }

})();
