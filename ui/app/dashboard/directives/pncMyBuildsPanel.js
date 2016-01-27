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
    'BuildRecordDAO',
    'UserDAO',
    'eventTypes',
    function ($log, authService, PageFactory, BuildConfigurationDAO, BuildRecordDAO, UserDAO, eventTypes) {
      return {
        restrict: 'E',
        templateUrl: 'dashboard/directives/pnc-my-builds-panel.html',
        scope: {},
        link: function (scope) {

          scope.update = function() {
            scope.page.reload();
          };

          scope.show = function() {
            return authService.isAuthenticated();
          };

          function init() {
            scope.page = PageFactory.build(BuildRecordDAO, function (pageIndex, pageSize, searchText) {
              return UserDAO.getAuthenticatedUser().$promise.then(function(result) {
                return BuildRecordDAO._getByUser({
                   userId: result.id,
                   pageIndex: pageIndex,
                   pageSize: pageSize,
                   search: searchText,
                   sort: 'sort=desc=id'
                }).$promise;
              });
            });

            scope.$on(eventTypes.BUILD_STARTED, scope.update);
            scope.$on(eventTypes.BUILD_FINISHED, scope.update);
          }

          if (authService.isAuthenticated()) {
            init();
          }
        }
      };
    }
  ]);

})();
