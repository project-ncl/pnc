'use strict';

(function() {

  var module = angular.module('pnc.websockets', [
    'angular-websocket'
  ]);

  module.config(['$stateProvider', function($stateProvider) {

    $stateProvider.state('websockets', {
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col-center.tmpl.html'
        }
      }
    });

    $stateProvider.state('websockets.list', {
      url: '/websockets',
      templateUrl: 'websockets/views/websockets.html',
      controller: 'WebSocketsController'
    });

  }]);

  module.factory('MyData', ['$websocket', 'Notifications', function ($websocket, Notifications) {
    var dataStream = $websocket('ws://localhost:8080/pnc-rest/ws/build-records/notifications');
    dataStream.onMessage(function(message) {
      Notifications.success('WS response ' + message.data);
    });

  }]);

})();
