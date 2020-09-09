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

  angular.module('pnc.artifacts').component('pncArtifactQualityLabel', {
    bindings: {
      artifact: '<'
    },
    templateUrl: 'artifacts/components/pnc-artifact-quality-label/pnc-artifact-quality-label.html',
    controller: [Controller]
  });


  function Controller() {
    const $ctrl = this;

    // -- Controller API --


    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.label = $ctrl.artifact.artifactQuality;

      $ctrl.labelClasses = ['label'];

      switch ($ctrl.label) {
        case 'NEW':
          $ctrl.labelClasses.push('label-default');
          break;
        case 'VERIFIED':
          $ctrl.labelClasses.push('label-info');
          break;
        case 'TESTED':
          $ctrl.labelClasses.push('label-success');
          break;
        case 'DEPRECATED':
          $ctrl.labelClasses.push('label-warning');
          break;
        case 'DELETED':
        case 'BLACKLISTED':
          $ctrl.labelClasses.push('label-danger');
          break;
        case 'TEMPORARY':
          $ctrl.labelClasses.push('label-primary');
          break;
        default:
          $ctrl.labelClasses.push('label-default');
      }

      $ctrl.$onChanges = (changes) => {
        if (changes && changes.artifact) {
          $ctrl.$onInit();
        }
      };
    };
  }

})();
