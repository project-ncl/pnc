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

  angular.module('pnc.scm-repositories').component('pncScmRepositoryCreatePage', {
    bindings: {
    },
    templateUrl: 'scm-repositories/create/pnc-scm-repository-create-page.html',
    controller: ['$state', 'ScmRepositoryResource', '$scope', 'events', Controller]
  });

  function Controller($state, ScmRepositoryResource, $scope, events) {
    const $ctrl = this;

    // -- Controller API --
    $ctrl.startCreating = startCreating;
    $ctrl.reset = reset;
    $ctrl.scmRepository = {};
    $ctrl.isCreatingInProgress = false;

    // --------------------

    // set default only if there is no initial value coming from ngModel
    if (typeof $ctrl.scmRepository.preBuildSyncEnabled === 'undefined') {
      $ctrl.scmRepository.preBuildSyncEnabled = true;
    }

    function gotoScmRepositoryDetailPage(id) {
      $state.go('scm-repositories.detail', {
        scmRepositoryId: id
      });
    }

    function startCreating(scmRepository) {
      $ctrl.isCreatingInProgress = true;
      create(scmRepository);
    }

    function create(scmRepository) {

      /**
       * When
       * 1) Internal SCM Repository that is not in PNC yet is used, there is response containing SCM Reposiory details
       * 2) External SCM Repository that in not in PNC yet is used, there is response without SCM Repository details, but
       *    they can be accessed later via Websockets once event RC_CREATION_SUCCESS is fired
       *
       * see NCL-4960
       */
      ScmRepositoryResource.createAndSync(scmRepository).then(function(result) {

        let scmRepositoryResult = result.data.repository;

        if (scmRepositoryResult && scmRepositoryResult.id) {
          gotoScmRepositoryDetailPage(scmRepositoryResult.id);

        } else {
          $scope.$on(events.SCM_REPOSITORY_CREATION_SUCCESS, function (event, payload) {
            // when SCM Repository is created and user is still on Create page
            if ($state.$current.name === 'scm-repositories.create') {
              gotoScmRepositoryDetailPage(payload.id);
            }
          });
        }

      }).catch(function() {
        $ctrl.isCreatingInProgress = false;
      });
    }

    function reset(form) {
      if (form) {
        form.$setPristine();
        form.$setUntouched();
        $ctrl.scmRepository = new ScmRepositoryResource();
      }
    }

  }

})();
