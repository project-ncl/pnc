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

  var module = angular.module('pnc.record');

  /**
   * @author Jakub Senko
   */
  module.directive('pncRunningBuildsPanel', [
    'PncCache',
    'RunningBuildRecordDAO',
    'eventTypes',
    function (PncCache, RunningBuildRecordDAO, eventTypes) {

      return {
        restrict: 'E',
        templateUrl: 'configuration/directives/pncRunningBuildsPanel/pnc-running-builds.html',
        scope: {
          configurationId: '='
        },
        link: function (scope) {

          scope.page = PncCache.key('pnc.record.pncRunningBuildsPanel').key('configurationId:' + scope.configurationId).key('page').getOrSet(function() {
            return RunningBuildRecordDAO.getPagedByBC({ configurationId: scope.configurationId });
          }).then(function(page) {
            page.reload();
            return page;
          }).then(function(page) {

            var update = function (event, payload) {
              /* jshint unused: false */
              page.reload();
            };

            scope.$on(eventTypes.BUILD_STARTED, update);
            scope.$on(eventTypes.BUILD_FINISHED, update);

            return page;
          });
        }
      };
    }
  ]);

})();
