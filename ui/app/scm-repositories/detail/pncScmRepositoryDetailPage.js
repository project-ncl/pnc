/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
      scmRepository: '<'
    },
    templateUrl: 'scm-repositories/detail/pnc-scm-repository-detail-page.html',
    controller: ['$state', 'BuildConfiguration', 'rsqlQuery', 'paginator', Controller]
  });

  function Controller($state, BuildConfiguration, rsqlQuery, paginator) {
    var $ctrl = this;

    // -- Controller API --
    
    $ctrl.cancel = cancel;
    $ctrl.update = update;

    $ctrl.buildConfigurations = {
      page: null,
      displayFields: ['name', 'project', 'buildStatus']
    };

    // --------------------

    $ctrl.$onInit = function() {
      getBuildConfigurationsPageByScmRepository($ctrl.scmRepository.id);
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

    // New rest endpoint instead of RSQL will be used once NCL-4946 is implemented
    function getBuildConfigurationsPageByScmRepository(scmRepositoryId) {
      var q = rsqlQuery().where('repository.id').eq(scmRepositoryId).end();
    
      return BuildConfiguration.query({ q: q }).$promise.then(function (page) { 
        $ctrl.buildConfigurations.page = paginator(page); 
      });
    }

  }

})();
