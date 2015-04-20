'use strict';

(function() {

  var module = angular.module('pnc.record');

  module.controller('RecordDetailController', [
    'recordDetail', 'configurationDetail', 'projectDetail',
    function(recordDetail, configurationDetail, projectDetail) {
      this.record = recordDetail;
      this.configuration = configurationDetail;
      this.project = projectDetail;
    }
  ]);

  module.controller('RecordInfoController', ['$log',
    function($log) {
      $log.debug('RecordInfoController');
    }
  ]);

  module.controller('RecordResultController', ['$log', 'buildLog',
    function($log, buildLog) {
      $log.debug('RecordResultController >> buildLog: %O', buildLog);
      this.log = buildLog.payload;
    }
  ]);

  module.controller('RecordOutputController', ['$log', 'artifacts',
    function($log, artifacts) {
      $log.debug('RecordOutputController >> artifacts: %O', artifacts);
      this.artifacts = artifacts;
    }
  ]);

})();
