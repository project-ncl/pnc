'use strict';

(function() {

  var module = angular.module('pnc.project');

  module.controller('ProjectListController', [
    '$log', '$state', 'projectList',
    function($log, $state, projectList) {
      $log.debug('ProjectListController >> this=%O, projectList=%O',
                 this, projectList);

      this.projects = projectList;
    }
  ]);

  module.controller('ProjectDetailController', [
    '$log', '$state', 'projectDetail',
    function($log, $state, projectDetail) {
      $log.debug('ProjectDetailController >> this=%O, projectDetail=%O',
                 this, projectDetail);

      this.project = projectDetail;
    }
  ]);

})();
