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

  angular.module('pnc.projects').component('pncProjectCreatePage', {
    bindings: {
    },
    templateUrl: 'projects/create/pnc-project-create-page.html',
    controller: ['$state', 'ProjectResource', Controller]
  });

  function Controller($state, ProjectResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.create = create;
    $ctrl.reset = reset;

    // --------------------

    function create(project) {
      new ProjectResource(angular.copy(project)).$save().then(function(result) {
        $state.go('projects.detail', {
          projectId: result.id
        });
      });
    }

    function reset(form) {
      form.$setPristine();
      form.$setUntouched();
    }

  }

})();
