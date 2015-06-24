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

  module.config([
    '$stateProvider',
    function ($stateProvider) {

      $stateProvider.state('record.detail', {
        url: '/{recordId:int}',
        templateUrl: 'record/detail/record.detail.html',
        data: {
          displayName: '{{ recordDetail.name }}'
        },
        controller: 'RecordDetailController',
        controllerAs: 'recordCtrl',
        resolve: {
          recordDetail: function (restClient, $stateParams) {
            return restClient.Record.get({
              recordId: $stateParams.recordId
            }).$promise;
          },
          configurationDetail: function (restClient, recordDetail) {
            return restClient.Configuration.get({
              configurationId: recordDetail.buildConfigurationId
            }).$promise;
          },
          projectDetail: function (restClient, configurationDetail) {
            return restClient.Project.get({
              projectId: configurationDetail.projectId
            }).$promise;
          }
        }
      });

    }]);

})();
