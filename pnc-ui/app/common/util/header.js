'use strict';

(function() {

  var module = angular.module('pnc.util.header', []);

  module.directive('pncHeader', function() {
    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      templateUrl: 'common/util/views/header.html'
    };
  });

  module.directive('pncHeaderTitle', function() {
    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      templateUrl: 'common/util/views/header.title.html'
    };
  });

  module.directive('pncHeaderButtons', function() {
    return {
      restrict: 'E',
      replace: true,
      transclude: true,
      templateUrl: 'common/util/views/header.buttons.html'
    };
  });

})();
