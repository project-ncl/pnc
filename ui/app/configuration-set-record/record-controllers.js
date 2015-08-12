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


  module.controller('CsRecordDetailController', [
    '$state',
    'csRecordDetail',
    function ($state, csRecordDetail) {
      this.csRecordDetail = csRecordDetail;
      this.reload = function () {
        $state.go($state.current, {}, {reload: true});
      };
    }
  ]);


  module.controller('CsRecordInfoController', [
    'csRecordDetail',
    'records',
    'runningRecords',
    function (csRecordDetail, records, runningRecords) {
      this.records = records;
      this.csRecordDetail = csRecordDetail;
      this.runningRecords = runningRecords;
    }
  ]);


  module.controller('CsRecordResultController', [
    'recordsLog',
    function (recordsLog) {
      this.recordsLog = recordsLog;
    }
  ]);


  module.controller('CsRecordOutputController', [
    'recordsArtifacts',
    function (recordsArtifacts) {
      this.recordsArtifacts = recordsArtifacts;
    }
  ]);


  module.controller('CsRecordListController', [
    '$state',
    function ($state) {
      this.reload = function () {
        $state.go($state.current, {}, {reload: true});
      };
    }
  ]);

})();
