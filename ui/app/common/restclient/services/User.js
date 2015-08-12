'use strict';

(function () {

  var module = angular.module('pnc.common.restclient');

  module.value('USER_ENDPOINT', '/users/:userId');

  /**
   * @ngdoc service
   */
  module.factory('User', [
    '$resource',
    'REST_BASE_URL',
    'USER_ENDPOINT',
    function ($resource, REST_BASE_URL, USER_ENDPOINT) {
      var ENDPOINT = REST_BASE_URL + USER_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        userId: '@id'
      }, {});

      return resource;
    }
  ]);

})();
