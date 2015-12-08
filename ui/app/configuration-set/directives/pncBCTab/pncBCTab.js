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

  var module = angular.module('pnc.configuration-set');

  /**
   * @author Jakub Senko
   */
  module.directive('pncBCTab', [
    '$log',
    '$state',
    '$q',
    'PncCache',
    'BuildConfigurationSetDAO',
    'BuildRecordDAO',
    'Notifications',
    'eventTypes',
    function ($log, $state, $q, PncCache, BuildConfigurationSetDAO, BuildRecordDAO, Notifications, eventTypes) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set/directives/pncBCTab/pnc-bc-tab.html',
        scope: {
          buildConfigurationSet: '='
        },
        link: function (scope) {

          scope.page = $q.when(scope.buildConfigurationSet).then(function(bcSet) {

            return PncCache.key('pnc.record.pncBCTab').key('buildConfigurationSetId:' + scope.buildConfigurationSet.id).key('page').getOrSet(function() {
              return bcSet.getPagedBuildConfigurations();
            });

          }).then(function(page) {
            page.reload();
            return page;
          }).then(function(page) {

            var update = function (event, payload) {
              if (_.isArray(scope.buildConfigurationSet.buildConfigurationIds) &&
                _(scope.buildConfigurationSet.buildConfigurationIds).contains(payload.buildConfigurationId)) {
                page.reload();
              }
            };

            scope.$on(eventTypes.BUILD_FINISHED, update);

            return page;
          });

          scope.remove = function (configurationId) {
            $log.debug('**Removing configurationId: %0**', configurationId);

            BuildConfigurationSetDAO.removeConfiguration({
              configurationSetId: scope.buildConfigurationSet.id,
              configurationId: configurationId
            }).$promise.then(
              // Success
              function() {
                var params = {
                  configurationSetId: scope.buildConfigurationSet.id
                };
                $state.go('configuration-set.detail', params, {
                  reload: true,
                  inherit: false,
                  notify: true
                });
              }
            );
          };

          scope.delete = function(bc) {
            bc.$delete().$promise.then(function() {
              Notifications.success('Build configuration deleted.');
            });
          };
        }
      };
    }
  ]);

})();
