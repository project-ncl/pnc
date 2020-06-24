/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.dashboard');

  /**
   * @ngdoc directive
   * @name pnc.dashboard:pncMyBuildSetsPanel
   * @restrict E
   * @description
   * @example
   * @author Alex Creasy
   */
  module.directive('pncMyGroupBuildsPanel', [
    'authService',
    'GroupBuildResource',
    'events',
    'paginator',
    function (authService, GroupBuildResource, events, paginator) {
      return {
        restrict: 'E',
        templateUrl: 'dashboard/directives/pnc-my-group-builds-panel/pnc-my-group-builds-panel.html',
        scope: {},
        link: function (scope) {

          scope.show = function() {
            return authService.isAuthenticated();
          };

          function init() {

            authService.getPncUser().then(function(result) {
              return GroupBuildResource.queryByUser({
                userId: result.id,
                pageSize: 10,
                sort: '=desc=startTime'
              }).$promise.then(function(page) {
                scope.page = paginator(page);
              });
            });

            scope.displayFields = ['status', 'id', 'configurationName', 'startTime', 'endTime'];

            scope.$on(events.GROUP_BUILD_PROGRESS_CHANGED, (event, groupBuild) => {
              if (authService.isCurrentUser(groupBuild.user)) {
                scope.page.refresh();
              }
            });
          }

          if (authService.isAuthenticated()) {
            init();
          }
        }
      };
    }
  ]);

})();
