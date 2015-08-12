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
