'use strict';

(function () {

  var module = angular.module('pnc.milestone', [
    'ui.router',
    'ui.bootstrap',
    'pnc.product',
    'pnc.remote.restClient',
    'pnc.util.header',
    'pnc.util.date_utils',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('product.detail.version.detail.milestone', {
      abstract: true,
      url: '/milestone',
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      }
    });

  }]);

})();
