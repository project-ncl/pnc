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

  angular.module('pnc.artifacts').factory('ArtifactModals', [
    '$uibModal',
    'ArtifactResource',
    'BuildResource',
    function ($uibModal, ArtifactResource, BuildResource) {

      /**
       * Opens a modal window allowing the user to change the quality of an
       * individual artifact.
       *
       * @param {ArtifactResource} artifact - The artifact to be updated
       * @returns {$uibModalInstance} The modal object instance from the ui-bootstrap library.
       */
      function newArtifactQualityModal(artifact) {

        const params = {
          title: `Change Artifact Quality: ${artifact.identifier}`,

          quality: artifact.artifactQuality,

          onSave: $value => {
            return ArtifactResource.changeQuality({
              id: artifact.id,
              quality: $value.quality,
              reason: $value.reason
            })
            .$promise
            .then(() => ArtifactResource.get({ id: artifact.id }).$promise);
          }

        };

        return openArtifactQualityModal(params);
      }

      /**
       * Opens a modal window for batch updating ALL artifacts of the given build.
       *
       * @param {BuildResource} build - The build to update all artifacts of
       * @returns {$uibModalInstance} The modal object instance from the ui-bootstrap library.
       */
      function newBuildQualityModal(build) {

        const params = {
          title: `Change All Artifact Qualities for Build: ${build.$canonicalName()}`,

          quality: 'NEW',

          onSave: $value => {
            console.log('Update build quality: ', $value);

            return BuildResource.changeQuality({
              id: build.id,
              quality: $value.quality,
              reason: $value.reason
            })
            .$promise;
          }

        };

        return openArtifactQualityModal(params);
      }



      /**
       * Opens a new modal window for selecting a new artifact quality.
       *
       * @param {object} params - Config params object for the modal
       * @param {string} params.title - The title to display in the modal window
       * @param {string} params.quality - The current quality level to display
       * @param {function} params.onSave - Callback function executed when the user clicks save. The function will
       * be invoked with a single argument: an object with the following properties:
       *  `quality` {string} - The new quality value selected.
       * `reason` {string} - The reason for the quality change entered.
       *
       * Whatever is returned from this callback will be used as the final value for modal object (see $uibModal for
       * details). The return value can be a promise.
       */
      function openArtifactQualityModal(params) {
        return $uibModal.open({
          animation: true,
          size: 'lg',
          component: 'pncArtifactQualityModal',
          resolve: {
            params
          }
        });
      }

      return Object.freeze({
        newArtifactQualityModal,
        newBuildQualityModal
      });

    }
  ]);

})();
