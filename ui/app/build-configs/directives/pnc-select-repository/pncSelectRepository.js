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

  angular.module('pnc.build-configs').component('pncSelectRepository', {
    require: {
      ngModel: 'ngModel'
    },
    templateUrl: 'build-configs/directives/pnc-select-repository/pnc-select-repository.html',
    controller: ['$log', '$q', 'utils', 'pncNotify', 'ScmRepositoryResource', 'pncProperties', Controller]
  });

  function Controller($log, $q, utils, pncNotify, ScmRepositoryResource, pncProperties) {
    var $ctrl = this,
        loading = false,
        previousDigest;


    // -- Controller API --

    $ctrl.userData = {};
    $ctrl.checkForRepo = checkForRepo;
    $ctrl.isLoading = isLoading;
    $ctrl.isRepoInternal = isRepoInternal;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.ngModel.$render = function () {
        $ctrl.userData = $ctrl.ngModel.$modelValue;

        // set default only if there is no initial value coming from ngModel
        if (typeof $ctrl.userData.preBuildSyncEnabled === 'undefined') {
          $ctrl.userData.preBuildSyncEnabled = true;
        }

        if (angular.isDefined($ctrl.userData.scmUrl)) {
          $ctrl.checkForRepo($ctrl.userData.scmUrl);
        }
      };
    };

    $ctrl.$doCheck = function () {
      var latestDigest = digest();

      if (previousDigest !== latestDigest && !angular.equals({}, $ctrl.userData)) {
        $ctrl.ngModel.$setViewValue(parseViewData());
        previousDigest = latestDigest;
      }
    };

    function checkForRepo(url) {
      $ctrl.userData.selectedRepoConfig = undefined;
      $ctrl.multipleRCError = false;
      loading = true;

      getRepo(url).then(function (repo) {
        if (repo && repo.id) {
          $ctrl.userData.selectedRepoConfig = repo;
        }

      }).finally(function() { loading = false; });
    }

    function getRepo(url) {
      if (utils.isEmpty(url)) {
        return $q.when();
      }
      return ScmRepositoryResource.query({ 'search-url': url }).$promise.then(function (result) {
        var repos = result.data;

        if (repos.length > 0) {
          return repos[0];
        }
        else {
          // No repo config exists
          return undefined;
        }
      });
    }

    function isLoading() {
      return loading;
    }

    function isRepoInternal(url) {
      if (url) {
        return url.includes(pncProperties.internalScmAuthority);
      }
    }

    function parseViewData() {
      var parsed = {
        scmUrl: $ctrl.userData.scmUrl,
        revision: $ctrl.userData.revision,
        preBuildSyncEnabled: $ctrl.userData.preBuildSyncEnabled,
        useExistingRepoConfig: angular.isDefined($ctrl.userData.selectedRepoConfig)
      };

      if (angular.isDefined($ctrl.userData.selectedRepoConfig)) {
        parsed.repoConfig = $ctrl.userData.selectedRepoConfig;
      }

      return parsed;
    }

    function digest() {
      var repoId = $ctrl.userData.selectedRepoConfig ? $ctrl.userData.selectedRepoConfig.id : undefined;
      return '' + $ctrl.userData.scmUrl + $ctrl.userData.revision + $ctrl.userData.preBuildSyncEnabled + repoId;
    }

  }

})();
