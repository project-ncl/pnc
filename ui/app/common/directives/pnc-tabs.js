'use strict';

(function () {

  var module = angular.module('pnc.common.directives');

  /**
   * @ngdoc directive
   * @restrict E
   * @example <tabs><tab title='First'>foo</tab><tab title='Second'>bar</tab></tabs>
   * @author Jakub Senko
   */
  module.directive('pncTabs', function () {
    return {
      restrict: 'E',
      scope: {},
      templateUrl: 'common/directives/views/pnc-tabs.html',
      transclude: true,
      controller: [
        '$log',
        '$scope',
        function ($log, $scope) {

          var tabs = $scope.tabs = [];

          var select = $scope.select = function (tabScope) {
            angular.forEach(tabs, function (tabScope) {
              tabScope.selected = false;
            });
            tabScope.selected = true;
          };

          this.registerTab = function (tabScope) {
            if (tabs.length === 0) {
              select(tabScope);
            }
            tabs.push(tabScope);
          };
        }
      ]
    };
  });


  module.directive('pncTab', function () {
    return {
      scope: {
        title: '=',
        show: '='
      },
      templateUrl: 'common/directives/views/pnc-tab.html',
      transclude: true,
      require: '^pncTabs',
      link: function (scope, e, a, tabsCtrl) {

        tabsCtrl.registerTab(scope);
      }
    };
  });

})();
