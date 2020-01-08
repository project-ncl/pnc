/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
   * @name pnc.dashboard:pncMyBuildsPanel
   * @restrict E
   * @description
   * If the user is authenticated displays a panel of the user's builds.
   * @example
   * <pnc-my-builds-panel></pnc-my-builds-panel>
   * @author Alex Creasy
   */
  module.directive('pncMyBuildsPanel', [
    '$log',
    'authService',
    'PageFactory',
    'BuildConfigurationDAO',
    'BuildResource',
    'UserDAO',
    'events',
    'paginator',
    function ($log, authService, PageFactory, BuildConfigurationDAO, BuildResource, UserDAO, events, paginator) {
      return {
        restrict: 'E',
        templateUrl: 'dashboard/directives/pnc-my-builds-panel.html',
        scope: {},
        link: function (scope) {

          var pncUser;

          scope.update = function(event, build) {
            if (pncUser.id === build.user.id) {
              scope.page.refresh();
            }
          };

          scope.show = function() {
            return authService.isAuthenticated();
          };

          function init() {

            authService.getPncUser().then(function(result) {
              pncUser = result;
              return BuildResource.queryByUser({
                userId: result.id,
                pageSize: 10
              }).$promise.then(function(page){
                scope.page = paginator(page);
              });
            });

            scope.displayFields = ['status', 'id', 'configurationName', 'startTime', 'endTime'];

            scope.$on(events.BUILD_PENDING, scope.update);
            scope.$on(events.BUILD_IN_PROGRESS, scope.update);
            scope.$on(events.BUILD_FINISHED, scope.update);
          }

          if (authService.isAuthenticated()) {
            init();
          }
        }
      };
    }
  ]);

})();
