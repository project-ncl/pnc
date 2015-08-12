'use strict';

(function () {
  /* globals _ */

  var module = angular.module('pnc.configuration-set-record');

  /**
   * TODO implement push notifications
   * TODO make this general
   *
   * @author Jakub Senko
   */
  module.directive('pncRecentCsBuilds', [
    '$log',
    '$timeout',
    'PncRestClient',
    function ($log, $timeout, PncRestClient) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-recent-builds.html',
        scope: {
          pncFilterBy: '=',
        },
        link: function (scope) {

          var recordMap = new buckets.Dictionary();

          scope.getRecords = function () {
            return _(recordMap.values()).sortBy(function (e) {
              return -e.id;
            });
          };

          var loadInitialData = function () {
            return PncRestClient.ConfigurationSetRecord.query().$promise;
          };

          loadInitialData().then(function (result) {
            _.chain(result)
              .filter(function (e) {
                return e.status !== 'BUILDING';
              })
              .each(function (record) {
                recordMap.set(record.id, record);
              })
              .value();
          });
        }
      };
    }
  ]);

  /**
   * TODO implement push notifications
   * TODO make this general
   *
   * @author Jakub Senko
   */
  module.directive('pncRunningCsBuilds', [
    '$log',
    '$timeout',
    'PncRestClient',
    function ($log, $timeout, PncRestClient) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-running-builds.html',
        scope: {
          pncFilterBy: '=',
        },
        link: function (scope) {

          var recordMap = new buckets.Dictionary();

          scope.getRecords = function () {
            return _(recordMap.values()).sortBy(function (e) {
              return -e.id;
            });
          };

          var loadInitialData = function () {
            return PncRestClient.ConfigurationSetRecord.query().$promise;
          };

          loadInitialData().then(function (result) {
            _.chain(result)
              .filter(function (e) {
                return e.status === 'BUILDING';
              })
              .each(function (record) {
                recordMap.set(record.id, record);
              })
              .value();
          });
        }
      };
    }
  ]);

})();
