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
(function () {
  'use strict';

  var module = angular.module('pnc.record');

  module.controller('RecordDetailController', [
    '$scope',
    '$state',
    '$log',
    'eventTypes',
    'recordDetail',
    function($scope, $state, $log, eventTypes, recordDetail) {
      this.record = recordDetail;

      $log.debug('Fetched BuildRecord:\n' + JSON.stringify(recordDetail, null, 4));

      $scope.$on(eventTypes.BUILD_FINISHED, function (event, payload) {
        if (recordDetail.id === payload.id) {
          recordDetail.$get();
        }
      });
    }
  ]);

  module.controller('RecordInfoController', [
    function () {
    }
  ]);

  module.controller('RecordResultController', [
    'buildLog',
    'REST_BASE_URL',
    'BUILD_RECORD_ENDPOINT',
    'recordDetail',
    function(buildLog, REST_BASE_URL, BUILD_RECORD_ENDPOINT, recordDetail) {
      this.logUrl = REST_BASE_URL + BUILD_RECORD_ENDPOINT.replace(':recordId', recordDetail.id) + '/log';
      this.logFileName = recordDetail.id + '_' + recordDetail.buildConfigurationName + '_' + recordDetail.status + '.txt';
      this.log = buildLog.payload;
    }
  ]);

  module.controller('RecordOutputController', [
    'artifacts',
    function(artifacts) {
      this.builtArtifacts = artifacts;
    }
  ]);

  module.controller('RecordDependenciesController', [
    'artifacts',
    function(artifacts) {
      this.downloadedArtifacts = artifacts;
   }
 ]);


  module.controller('RecordListController', [
    '$log',
    function ($log) {

      $log.debug('RecordListCtrl');
    }
  ]);

})();
