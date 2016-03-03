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
   * @ngdoc directive
   * @name pnc.common.eventbus:pncBuildDetail
   * @restrict E
   * @description
   * @example
   * @author Alex Creasy
  */
  module.directive('pncBuildDetails', [
    function() {

    var DEFAULT_TEMPLATE = 'record/directives/pnc-build-details/pnc-build-details.html';

    function Controller($scope, $q, eventTypes, Build, BuildRecordDAO,
        BuildConfigurationDAO, ProjectDAO, EnvironmentDAO, UserDAO) {

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
            // Returns a BuildConfigurationRest object
            return BuildConfigurationDAO.get({
              configurationId: record.buildConfigurationId
            }).$promise;
          } else {
            // Returns a BuildConfigurationAuditedRest object
            return BuildRecordDAO.getAuditedBuildConfiguration({
              recordId: record.id
            }).$promise;
          }
        }

        return BuildRecordDAO.getCompletedOrRunning({
          recordId: recordId
        }).$promise.then(
          function(response) {
            result.buildRecord = response;

            // Get the BuildRecord's related entities in parallel.
            return $q.all([
              fetchConfiguration(response),
              UserDAO.get({
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
              ProjectDAO.get({
                projectId: responses[0].project.id
              }).$promise,

              EnvironmentDAO.get({
                environmentId: responses[0].environment.id
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
        'pncRecordId': '='
      },
      bindToController: true,
      controllerAs: 'ctrl',
      controller: [
        '$scope',
        '$q',
        'eventTypes',
        'Build',
        'BuildRecordDAO',
        'BuildConfigurationDAO',
        'ProjectDAO',
        'EnvironmentDAO',
        'UserDAO',
        Controller
      ]
    };
  }]);

})();
