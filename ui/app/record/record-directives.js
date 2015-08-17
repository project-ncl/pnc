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
   * @param {number=} pnc-build-configuration-id: Will only display builds
   * of that BuildConfiguration.
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
    'BuildRecordDAO',
    'eventTypes',
    function($log, $timeout, BuildRecordDAO, eventTypes) {

      var DEFAULT_TEMPLATE = 'record/views/pnc-recent-builds.html';

      return {
        restrict: 'E',
        templateUrl: function(elem, attrs) {
          return attrs.pncTemplate || DEFAULT_TEMPLATE;
        },
        scope: {
          pncFilterBy: '=',
          pncBcId: '=',
        },
        link: function(scope) {
          var loaded;
          var recordMap = new buckets.Dictionary();
          var filterSpec = scope.pncFilterBy || {};
          var buildConfigId = scope.pncBcId;

          scope.isLoaded = function() {
            return loaded;
          };

          scope.getRecords = function() {
            return recordMap.values();
          };

          function onBuildFinished(event, payload) {
            if (!filtersMatch(payload, filterSpec)) {
              return;
            }

            BuildRecordDAO.get({ recordId: payload.id }).$promise.then(
              function(result) {
                if (filtersMatch(result, filterSpec)) {
                  recordMap.set(result.id, result);
                }
              }
            );
          }

          function init() {
            loaded = false;
            var results;

            function getAllRecords() {
              return BuildRecordDAO.query({ sort: '=desc=id'}).$promise;
            }

            function getForConfiguration(id) {
              return BuildRecordDAO.getAllForConfiguration({
                configurationId: id,
                sort: '=desc=id'
              }).$promise;
            }

            if (buildConfigId) {
              filterSpec.buildConfigurationId = buildConfigId;
              results = getForConfiguration(buildConfigId);
            } else {
              results = getAllRecords();
            }

            // Initialise recordMap with id => record entries.
            results.then(
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
   * @param {string=} pnc-template Optional: the URL of a display template to
   * use.
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
    'RunningBuildRecordDAO',
    'eventTypes',
    function($log, RunningBuildRecordDAO, eventTypes) {

      var DEFAULT_TEMPLATE = 'record/views/pnc-running-builds.html';

      return {
        restrict: 'E',
        templateUrl: function(elem, attrs) {
          return attrs.pncTemplate || DEFAULT_TEMPLATE;
        },
        scope: {
          pncFilterBy: '=',
          pncBcId: '=',
        },
        link: function(scope) {
          var loaded;
          var recordMap = new buckets.Dictionary();
          var filterSpec = scope.pncFilterBy || {};
          var buildConfigId = scope.pncBcId;

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

            RunningBuildRecordDAO.get({ recordId: payload.id }).$promise.then(
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
            var results;

            function getAllRecords() {
              return RunningBuildRecordDAO.query({ sort: '=desc=id'}).$promise;
            }

            function getForConfiguration(id) {
              return RunningBuildRecordDAO.getAllForConfiguration({
                configurationId: id,
                sort: '=desc=id'
              }).$promise;
            }

            if (buildConfigId) {
              filterSpec.buildConfigurationId = buildConfigId;
              results = getForConfiguration(buildConfigId);
            } else {
              results = getAllRecords();
            }

            // Initialise recordMap with id => record entries.
            results.then(
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

  /**
   * @ngdoc directive
   * @name pnc.common.eventbus:pncBuildDetail
   * @restrict E
   * @description
   * @example
   * @author Alex Creasy
  */
  module.directive('pncBuildDetails', function() {

    var DEFAULT_TEMPLATE = 'record/views/pnc-build-details.html';

    function Controller($scope, $q, eventTypes, Build, BuildRecord,
        BuildConfiguration, Project, Environment, User) {
      var self = this;
      var loaded;

      self.record = null;
      self.configuration = null;
      self.project = null;
      self.environment = null;
      self.user = null;

      self.isLoaded = function() {
        return loaded;
      };

      function fetchData(recordId) {
        var result = {};

        // Returns a promise to get the related BuildConfiguration
        // if the build is in progress, or the AuditedBuildConfiguration
        // if the build has completed.
        function fetchConfiguration(record) {
          if(record.status === 'BUILDING') {
            return BuildConfiguration.get({
              configurationId: record.buildConfigurationId
            }).$promise;
          } else {
            return BuildRecord.getAuditedBuildConfiguration({
              recordId: record.id
            }).$promise;
          }
        }

        return Build.get({
          recordId: recordId
        }).then(
          function(response) {
            result.buildRecord = response;

            // Get the BuildRecord's related entities in parallel.
            return $q.all([
              fetchConfiguration(response),
              User.get({
                userId: response.userId
              }).$promise
            ]);
          }
        ).then(
          function(responses) {
            result.buildConfiguration = responses[0];
            result.user = responses[1];

            // Get the BuildConfiguration's related entities in parallel.
            return $q.all([
              Project.get({
                projectId: responses[0].projectId
              }).$promise,

              Environment.get({
                environmentId: responses[0].environmentId
              }).$promise

            ]);
          }
        ).then(
          function(response) {
            result.project = response[0];
            result.environment = response[1];
            return result;
          }
        );
      }

      function init() {
        fetchData(self.pncRecordId).then(
          function(response) {
            self.record = response.buildRecord;
            self.configuration = response.buildConfiguration;
            self.project = response.project;
            self.environment = response.environment;
            self.user = response.user;

          }
        ).finally(function() {
          loaded = true;
        });
      }

      loaded = false;
      init();
      // Listen for websocket
      $scope.$on(eventTypes.BUILD_FINISHED, init);
    }

    return {
      restrict: 'E',
      templateUrl: function(elem, attrs) {
        return attrs.pncTemplate || DEFAULT_TEMPLATE;
      },
      scope: {
        pncRecordId: '='
      },
      bindToController: true,
      controllerAs: 'ctrl',
      controller: [
        '$scope',
        '$q',
        'eventTypes',
        'Build',
        'BuildRecord',
        'BuildConfiguration',
        'Project',
        'Environment',
        'User',
        Controller
      ]
    };
  });

})();
