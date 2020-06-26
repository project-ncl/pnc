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

  angular.module('pnc.scm-repositories').component('pncScmRepositoryDetailPage', {
    bindings: {
      /**
       * object representing SCM Repository
       */
      scmRepository: '<',
      /**
       * object representing Build Configs
       */
      buildConfigs: '<'
    },
    templateUrl: 'scm-repositories/detail/pnc-scm-repository-detail-page.html',
    controller: ['$state', 'paginator', Controller]
  });

  function Controller($state, paginator) {
    const $ctrl = this;

    // -- Controller API --
    
    $ctrl.cancel = cancel;
    $ctrl.update = update;

    $ctrl.buildConfigurations = {
      page: null,
      displayFields: ['name', 'project', 'buildStatus']
    };

    // --------------------

    $ctrl.$onInit = function() {
      $ctrl.buildConfigurations.page = paginator($ctrl.buildConfigs); 
    };

    function reload() {
      $state.go('scm-repositories.detail', {
        scmRepositoryId: $ctrl.scmRepository.id
      }, {
        reload: true
      });
    }

    function cancel() {
      reload();
    }

    function update() {
      $ctrl.scmRepository.$update().finally(reload);
    }

  }

})();
