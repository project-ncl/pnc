(function () {
  'use strict';

  angular.module('pnc.build-configs').component('pncSelectRepository', {
    templateUrl: 'build-configs/directives/pnc-select-repository/pnc-select-repository.html',
    controller: ['$log', '$q', 'utils', 'pncNotify', 'RepositoryConfiguration', 'pncProperties', Controller]
  });

  function Controller($log, $q, utils, pncNotify, RepositoryConfiguration, pncProperties) {
    var $ctrl = this;

    $ctrl.loading = 0;
    $ctrl.checkForRepo = checkForRepo;
    $ctrl.isLoading = isLoading;
    $ctrl.isRepoInternal = isRepoInternal;

    function checkForRepo(url) {
      $ctrl.repo = undefined;
      $ctrl.loading++;

      getRepo(url).then(function (repo) {
        if (repo && repo.id) {
          $ctrl.repo = repo;
        }

      }).finally(function() { $ctrl.loading--; });
    }

    function getRepo(url) {
      if (utils.isEmpty(url)) {
        return $q.when();
      }
      return RepositoryConfiguration.search({ search: url }).$promise.then(function (result) {
        var repos = result.data;

        if (repos.length === 1) {
          return repos[0];
        } else if (repos.length > 1) {
          pncNotify.warn('More than 1 RepositoryConfiguration matches this URL, see console for more information');
          $log.warn('Found %s RepositoryConfigurations for URL: %s , using the first one found, if this is a problem please report it to the administrator. Found repos %s', repos.length, url, utils.prettyPrint(repos));
          return repos[0];
        } else {
          // No repo config exists
          return undefined;
        }
      });
    }

    function isLoading() {
      return $ctrl.loading > 0;
    }

    function isRepoInternal(url) {
      if (url) {
        return url.includes(pncProperties.internalScmAuthority);
      }
    }

  }

})();
