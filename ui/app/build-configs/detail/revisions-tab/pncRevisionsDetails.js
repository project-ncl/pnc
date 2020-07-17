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

  angular.module('pnc.build-configs').component('pncRevisionsDetails', {
    bindings: {
      revision: '<'
    },
    templateUrl: 'build-configs/detail/revisions-tab/pnc-revisions-details.html',
    controller: ['$state', 'BuildConfigResource', 'pncNotify', Controller]
  });


  function Controller($state, BuildConfigResource, pncNotify) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.restore = restore;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.hideFields = ['description'];
    };

    
    function restore() {
      BuildConfigResource.restoreRevision({
        id: $ctrl.revision.id,
        revisionId: $ctrl.revision.rev
      }, {
        /* postData need to be explicitly set as empty, otherwise the first argument would be send as postData 
           and URL parameters mapping wouldn't work correctly */
      }).$promise.then(() => {
        $state.go('^.^.default', {}, { reload: true });
        pncNotify.success('Revision: ' + $ctrl.revision.rev + ' of ' + $ctrl.revision.name + ' restored');
      });
    }
  }

})();
