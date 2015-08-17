/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

(function () {

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
    'BuildConfigurationSetRecordDAO',
    function ($log, $timeout, BuildConfigurationSetRecordDAO) {

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
            return BuildConfigurationSetRecordDAO.query().$promise;
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
    'BuildConfigurationSetRecordDAO',
    function ($log, $timeout, BuildConfigurationSetRecordDAO) {

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
            return BuildConfigurationSetRecordDAO.query().$promise;
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
