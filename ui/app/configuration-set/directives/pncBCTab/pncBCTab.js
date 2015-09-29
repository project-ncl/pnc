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
  module.directive('pncBCTab', [
    '$log',
    '$state',
    'BuildConfigurationSetDAO',
    'BuildRecordDAO',
    'Notifications',
    function ($log, $state, BuildConfigurationSetDAO, BuildRecordDAO, Notifications) {

      return {
        restrict: 'E',
        templateUrl: 'configuration-set/directives/pncBCTab/pnc-bc-tab.html',
        scope: {
          buildConfigurationSet: '='
        },
        link: function (scope) {

          scope.latestBuildRecords = {};

          scope.page = BuildConfigurationSetDAO.getPagedConfigurations({
            configurationSetId: scope.buildConfigurationSet.id });

          scope.page.onUpdate(function(page) {
            _(page.data).each(function(bc) {
              if(!_(scope.latestBuildRecords).has(bc.id)) { // avoid unnecessary requests
                BuildRecordDAO.getLatestForConfiguration({ configurationId: bc.id }).then(function (data) {
                  scope.latestBuildRecords[bc.id] = data;
                });
              }
            });
          });

          scope.remove = function(configurationId) {
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
