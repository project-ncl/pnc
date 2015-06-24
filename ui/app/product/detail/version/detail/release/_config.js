'use strict';

(function () {

  var module = angular.module('pnc.release', [
    'ui.router',
    'ui.bootstrap',
    'pnc.product',
    'pnc.remote.restClient',
    'pnc.util.header',
    'pnc.util.date_utils',
    'angularUtils.directives.uiBreadcrumbs'
  ]);


  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.release', {
      abstract: true,
      url: '/release',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      }
    });

  }]);

})();
