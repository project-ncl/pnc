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

(function() {

  var module = angular.module('pnc.record');

  /**
   * Compares each of the properties of filterSpec to the same named
   * properties of entity and returns true only if they are ALL strictly
   * equal.
   */
  function filtersMatch(entity, filterSpec) {
    if (angular.isUndefined(filterSpec)) {
      return true;
    }

    var result = true;
    Object.getOwnPropertyNames(filterSpec).forEach(function(key) {
      if (filterSpec[key] !== entity[key]) {
        result = false;
      }
    });
    return result;
  }

  /**
  * @ngdoc directive
  * @name pnc.common.eventbus:pncRecentBuilds
  * @restrict E
  * @param {object=} pnc-filter-by Optional: Each property of the provided
  * object will be compared against the properties of the same name on
  * any records received from HTTP queries. Unless all properties match, the
  * record will be ignored.
  * @param {string=} pnc-template Optional: the URL of a display template to
  * use.
  * @description
  * Displays a table of recently completed builds.
  * @example
  * # Without filtering
  * ```html
    <pnc-recent-builds></pnc-recent-builds>
  * ```
  *
  * # Filter for builds of BuildConfiguration where id=7 and for user with id=23.
  * ```html
    <div ng-controller="myController as ctrl">
      <pnc-recent-builds pnc-filter-by="ctrl.filterSpec"></pnc-recent-builds>
    </div>
  * ```
  *
  * ```js
    angular.module('myModule')
      .controller('myController', function() {
        this.filterSpec = {
          buildConfigurationId: 7,
          userId: 23
        }
      });
  * ```
  * @author Alex Creasy
  */
  module.directive('pncRecentBuilds', [
    '$log',
    '$timeout',
    'PncRestClient',
    'eventTypes',
    function($log, $timeout, PncRestClient, eventTypes) {

      var DEFAULT_TEMPLATE = 'record/views/pnc-recent-builds.html';

      return {
        restrict: 'E',
        template: function(elem, attrs) {
          var tmplUrl = DEFAULT_TEMPLATE;
          if (attrs.pncTemplate) {
            tmplUrl = attrs.pncTemplate;
          }
          return '<div ng-include="\'' + tmplUrl + '\'"></div>';
        },
        scope: {
          pncFilterBy: '=',
        },
        link: function(scope) {
          var loaded;
          var recordMap = new buckets.Dictionary();
          var filterSpec = scope.pncFilterBy;

          scope.isLoaded = function() {
            return loaded;
          };

          scope.getRecords = function() {
            return recordMap.values();
          };

          // scope.getBuildConfiguration(buildRecord) {
          //   buildRecord.getBuildConfiguration().$promise
          // }

          function onBuildFinished(event, payload) {
            if (!filtersMatch(payload, filterSpec)) {
              return;
            }

            PncRestClient.Record.get({ recordId: payload.id }).$promise.then(
              function(result) {
                if (filtersMatch(result, filterSpec)) {
                  recordMap.set(result.id, result);
                }
              }
            );
          }

          function init() {
            loaded = false;
            // Initialise recordMap with id => record entries.
            PncRestClient.Record.query().$promise.then(
              function success(result) {
                $log.debug('pnc-recent-builds: initial fetch: %O', result);
                result.forEach(function(record) {
                  if (filtersMatch(record, filterSpec)) {
                    recordMap.set(record.id, record);
                  }
                });
                // Listen after initial fetch of records to prevent duplicates.
                scope.$on(eventTypes.BUILD_FINISHED, onBuildFinished);
              }
            ).finally(function() {
              loaded = true;
            });
          }

          init();
        }
      };
    }
  ]);


  /**
  * @ngdoc directive
  * @name pnc.common.eventbus:pncRunningBuilds
  * @restrict E
  * @param {object=} pnc-filter-by Optional: Each property of the provided
  * object will be compared against the properties of the same name on
  * any records received from HTTP queries. Unless all properties match, the
  * record will be ignored.
  * @description
  * Displays a table of running builds.
  * @example
  * # Without filtering
  * ```html
    <pnc-running-builds></pnc-running-builds>
  * ```
  *
  * # Filter for builds of BuildConfiguration where id=7 and for user with id=23.
  * ```html
    <div ng-controller="myController as ctrl">
      <pnc-running-builds pnc-filter-by="ctrl.filterSpec"></pnc-running-builds>
    </div>
  * ```
  *
  * ```js
    angular.module('myModule')
      .controller('myController', function() {
        this.filterSpec = {
          buildConfigurationId: 7,
          userId: 23
        }
      });
  * ```
  * @author Alex Creasy
  */
  module.directive('pncRunningBuilds', [
    '$log',
    'PncRestClient',
    'eventTypes',
    function($log, PncRestClient, eventTypes) {

      var DEFAULT_TEMPLATE = 'record/views/pnc-running-builds.html';

      return {
        restrict: 'E',
        template: function(elem, attrs) {
          var tmplUrl = DEFAULT_TEMPLATE;
          if (attrs.pncTemplate) {
            tmplUrl = attrs.pncTemplate;
          }
          return '<div ng-include="\'' + tmplUrl + '\'"></div>';
        },
        scope: {
          pncFilterBy: '=',
        },
        link: function(scope) {
          var loaded;
          var recordMap = new buckets.Dictionary();
          var filterSpec = scope.pncFilterBy;

          scope.isLoaded = function() {
            return loaded;
          };

          scope.getRecords = function() {
            return recordMap.values();
          };

          function onBuildStarted(event, payload) {

            if(!filtersMatch(payload, filterSpec)) {
              return;
            }

            PncRestClient.Running.get({ recordId: payload.id }).$promise.then(
              function(result) {
                recordMap.set(result.id, result);
              }
            );
          }

          function onBuildFinished(event, payload) {
            recordMap.remove(payload.id);
          }

          function init() {
            loaded = false;

            // Initialise recordMap with id => record entries.
            PncRestClient.Running.query().$promise.then(
              function success(result) {
                $log.debug('pnc-running-builds: initial fetch: %O', result);
                result.forEach(function(record) {
                  if(filtersMatch(record, filterSpec)) {
                    recordMap.set(record.id, record);
                  }
                });
                // Listen after initial fetch of records to prevent duplicates.
                scope.$on(eventTypes.BUILD_STARTED, onBuildStarted);
                scope.$on(eventTypes.BUILD_FINISHED, onBuildFinished);
              }
            ).finally(function() {
              loaded = true;
            });
          }

          init();
        }
      };
    }
  ]);

})();
