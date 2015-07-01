'use strict';

(function () {

  var module = angular.module('pnc.common.websockets', [
    'angular-websocket'
  ]);

  module.constant('WEBSOCKET_CONFIG', {
    'DEFAULT_URI': 'ws://' +  location.host + '/pnc-rest/ws/build-records/notifications'
  });


})();
