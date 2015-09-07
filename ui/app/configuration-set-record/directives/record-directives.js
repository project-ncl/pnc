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

  module.factory('dataLoader', [
    'BuildConfigurationSetRecordDAO',
    function (BuildConfigurationSetRecordDAO) {
      return function () {
        return BuildConfigurationSetRecordDAO.query().$promise;
      };
    }]);

  module.factory('dataUpdater', [
    'BuildConfigurationSetRecordDAO',
    function (BuildConfigurationSetRecordDAO) {
      return {
        _load: function (id) {
          return BuildConfigurationSetRecordDAO.get({recordId: id}).$promise;
        },
        update: function (data, payload) {
          if (data.id === payload.id) {
            return this._load(payload.id);
          }
          else {
            return null;
          }
        },
        insert: function (payload) {
          return this._load(payload.id);
        }
      };
    }
  ]);

  module.factory('sortByIdDescending', [
    function () {
      return function (e) {
        return -e.id;
      };
    }
  ]);

  /**
   * @author Jakub Senko
   */
  module.directive('pncRecentCsBuilds', [
    '$log',
    '$timeout',
    'eventTypes',
    'dataLoader',
    'dataUpdater',
    'sortByIdDescending',
    'TrackerFactory',
    function ($log, $timeout, eventTypes, dataLoader, dataUpdater, sortByIdDescending, TrackerFactory) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-recent-build-sets.html',
        scope: {},
        link: function (scope) {

          var dataFilters = [function (e) {
            return e.status !== 'BUILDING';
          }];

          var tracker = TrackerFactory.track(dataLoader, dataFilters, dataUpdater, sortByIdDescending);
          scope.tracker = tracker;

          var process = function (eventType, payload) {
            tracker.processEvent(payload);
          };
          scope.$on(eventTypes.BUILD_SET_STARTED, process);
          scope.$on(eventTypes.BUILD_SET_FINISHED, process);
        }
      };
    }
  ]);

  /**
   * @author Jakub Senko
   */
  module.directive('pncRunningCsBuilds', [
    '$log',
    '$timeout',
    'eventTypes',
    'dataLoader',
    'dataUpdater',
    'sortByIdDescending',
    'TrackerFactory',
    function ($log, $timeout, eventTypes, dataLoader, dataUpdater, sortByIdDescending, TrackerFactory) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-running-build-sets.html',
        scope: {},
        link: function (scope) {

          var dataFilters = [function (e) {
            return e.status === 'BUILDING';
          }];

          var tracker = TrackerFactory.track(dataLoader, dataFilters, dataUpdater, sortByIdDescending);
          scope.tracker = tracker;

          var process = function (eventType, payload) {
            tracker.processEvent(payload);
          };
          scope.$on(eventTypes.BUILD_SET_STARTED, process);
          scope.$on(eventTypes.BUILD_SET_FINISHED, process);
        }
      };
    }
  ]);


  /**
   * @author Jakub Senko
   */
  module.directive('pncRunningBuilds2', [
    '$log',
    'RunningBuildRecordDAO',
    'eventTypes',
    'dataLoader',
    'dataUpdater',
    'sortByIdDescending',
    'TrackerFactory',
    function ($log, RunningBuildRecordDAO, eventTypes,
              dataLoader, dataUpdater, sortByIdDescending, TrackerFactory) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-running-builds.html',
        scope: {
          'buildConfigSetRecordId': '@',
          'searchText': '=',
          'count': '='
        },
        link: function (scope) {

          var dataLoader = function () {
            return RunningBuildRecordDAO.query().$promise.then(function (r) {
              return _(r).where({buildConfigSetRecordId: parseInt(scope.buildConfigSetRecordId)});
            });
          };

          var dataFilters = [function (e) {
            return e.buildConfigSetRecordId === parseInt(scope.buildConfigSetRecordId) && e.status === 'BUILDING';
          }];

          var dataUpdater = {
            _load: function (id) {
              /* The request will fail after build is done because it is no longer running.
               * In that case we must ensure deletion by returning a invalid but non-null value.
               */
              return RunningBuildRecordDAO.get({recordId: id}).$promise
                .catch(_.constant({}));
            },
            update: function (data, payload) {
              if (data.id === payload.id) {
                return this._load(payload.id);
              }
              else {
                return null;
              }
            },
            insert: function (payload) {
              return this._load(payload.id);
            }
          };

          var tracker = TrackerFactory.track(dataLoader, dataFilters, dataUpdater, sortByIdDescending);
          tracker.onUpdate = function(data) { scope.count = data.length; };
          scope.tracker = tracker;

          var process = function (eventType, payload) {
            tracker.processEvent(payload);
          };
          scope.$on(eventTypes.BUILD_STARTED, process);
          scope.$on(eventTypes.BUILD_FINISHED, process);
        }
      };
    }
  ]);


  /**
   * @author Jakub Senko
   */
  module.directive('pncRecentBuilds2', [
    '$log',
    'BuildRecordDAO',
    'eventTypes',
    'dataLoader',
    'dataUpdater',
    'sortByIdDescending',
    'TrackerFactory',
    function ($log, BuildRecordDAO, eventTypes,
              dataLoader, dataUpdater, sortByIdDescending, TrackerFactory) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pnc-recent-builds.html',
        scope: {
          'buildConfigSetRecordId': '@',
          'searchText': '=',
          'count': '='
        },
        link: function (scope) {

          var dataLoader = function () {
            return BuildRecordDAO.query().$promise.then(function (r) {
              return _(r).where({buildConfigSetRecordId: parseInt(scope.buildConfigSetRecordId)});
            });
          };

          var dataFilters = [function (e) {
            return e.buildConfigSetRecordId === parseInt(scope.buildConfigSetRecordId) && e.status !== 'BUILDING';
          }];

          var dataUpdater = {
            _load: function (id) {
              return BuildRecordDAO.get({recordId: id}).$promise;
            },
            update: function (data, payload) {
              if (data.id === payload.id) {
                return this._load(payload.id);
              }
              else {
                return null;
              }
            },
            insert: function (payload) {
              return this._load(payload.id);
            }
          };

          var tracker = TrackerFactory.track(dataLoader, dataFilters, dataUpdater, sortByIdDescending);
          tracker.onUpdate = function(data) { scope.count = data.length; };
          scope.tracker = tracker;

          var process = function (eventType, payload) {
            tracker.processEvent(payload);
          };
          scope.$on(eventTypes.BUILD_STARTED, process);
          scope.$on(eventTypes.BUILD_FINISHED, process);
        }
      };
    }
  ]);

})();
