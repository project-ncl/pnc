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
   * @author Jakub Senko
   */
  module.directive('pncRecentCsBuilds', [
    '$log',
    'PncCache',
    'BuildConfigurationSetRecordDAO',
    'eventTypes',
    function ($log, PncCache, BuildConfigurationSetRecordDAO, eventTypes) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set-record/directives/pncRecentCsBuilds/pnc-recent-cs-builds.html',
        scope: {},
        link: function (scope) {

          scope.page = PncCache.key('pnc.configuration-set-record.pncRecentCsBuilds').key('page').getOrSet(function() {
            return BuildConfigurationSetRecordDAO.getPagedFinished();
          }).then(function(page) {
            page.reload();
            return page;
          }).then(function(page) {

            var update = function (event, payload) {
              /* jshint unused: false */
              page.reload();
            };

            scope.$on(eventTypes.BUILD_SET_STARTED, update);
            scope.$on(eventTypes.BUILD_SET_FINISHED, update);

            return page;
          });
        }
      };
    }
  ]);

})();
