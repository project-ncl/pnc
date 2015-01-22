'use strict';

(function() {
  angular.module('pncui').controller('RootCtrl', function ($scope) {
    $scope.messages = [
      {
       'text': 'Hello World'
      },
    ];
  });
})();