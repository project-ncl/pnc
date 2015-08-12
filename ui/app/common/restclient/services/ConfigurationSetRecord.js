'use strict';

(function () {

  var module = angular.module('pnc.common.restclient');

  module.value('BUILD_CONFIG_SET_RECORD_ENDPOINT', '/build-config-set-records/:recordId');

  /**
   * @ngdoc service
   */
  module.factory('ConfigurationSetRecord', [
    '$resource',
    'BuildConfigurationSet',
    'User',
    'cachedGetter',
    'REST_BASE_URL',
    'BUILD_CONFIG_SET_RECORD_ENDPOINT',
    function ($resource, BuildConfigurationSet, User, cachedGetter, REST_BASE_URL, BUILD_CONFIG_SET_RECORD_ENDPOINT) {

      var ENDPOINT = REST_BASE_URL + BUILD_CONFIG_SET_RECORD_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        recordId: '@id'
      }, {});

      resource.prototype.getConfigurationSet = cachedGetter(
        function (record) {
          return BuildConfigurationSet.get({ configurationSetId: record.buildConfigurationSetId });
        }
      );

      resource.prototype.getUser = cachedGetter(
        function (record) {
          return User.get({ userId: record.userId });
        }
      );

      return resource;
    }
  ]);

})();
