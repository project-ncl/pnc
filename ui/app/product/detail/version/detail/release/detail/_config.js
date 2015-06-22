'use strict';

(function () {

  var module = angular.module('pnc.release');

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.release.detail', {

      abstract: true,
      url: '/{releaseId:int}',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      resolve: {
        releaseDetail: function(restClient, $stateParams) {
          return restClient.Release.get({ releaseId: $stateParams.releaseId })
          .$promise;
        }
      }
    });

  }]);

})();
