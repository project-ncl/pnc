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

  module.controller('RecordDetailController', [
    '$scope',
    '$state',
    'eventTypes',
    'recordDetail',
    'configurationDetail',
    function($scope, $state, eventTypes, recordDetail, configurationDetail) {
      this.record = recordDetail;
      this.configuration = configurationDetail;

      $scope.$on(eventTypes.BUILD_FINISHED, function() {
        $state.go($state.current, {}, {reload: true});
      });
    }
  ]);

  module.controller('RecordInfoController', [
    function () {
    }
  ]);

  module.controller('RecordResultController', [
    'buildLog',
    function(buildLog) {
      this.log = buildLog.payload;
    }
  ]);

  module.controller('RecordOutputController', [
    '$log',
    'artifacts',
    function($log, artifacts) {
      this.artifacts = artifacts;

      this.builtArtifacts = _.filter(this.artifacts,function(artifact) {
        return artifact.status === 'BINARY_BUILT';
      });

      this.importedArtifacts = _.filter(this.artifacts,function(artifact) {
        return artifact.status === 'BINARY_IMPORTED';
      });

      $log.debug('RecordOutputController >> builtArtifacts: %O', JSON.stringify(this.builtArtifacts));
      $log.debug('RecordOutputController >> importedArtifacts: %O', JSON.stringify(this.importedArtifacts));

    }
  ]);


  module.controller('RecordListController', [
    '$log',
    function ($log) {

      $log.debug('RecordListCtrl');
    }
  ]);

})();
