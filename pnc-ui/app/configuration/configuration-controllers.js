'use strict';

(function() {

  var module = angular.module('pnc.configuration');

  module.controller('ConfigurationListController', [
    '$log', '$state', 'configurationList',
    function($log, $state, configurationList) {
      $log.debug('ConfigurationListController >> this=%O, configurationList=%O',
                 this, configurationList);

      this.configurations = configurationList;
    }
  ]);

  module.controller('ConfigurationCreateController', [
    '$state', '$log', 'PncRestClient', 'Notifications',
    'environments', 'projects',
    function($state, $log, PncRestClient, Notifications, environments,
             projects) {

      this.data = new PncRestClient.Configuration();
      this.environments = environments;
      this.projects = projects;

      var that = this;

      this.submit = function() {

        that.data.$save().then(
          function(result) {
            Notifications.success('Configuration created');
            $state.go('configuration.detail.show', {
              configurationId: result.id
            });
          },
          function(response) {
            $log.error('Create configuration failed: response: %O', response);
            Notifications.error('Action Failed.');
          }
        );
      };
    }
  ]);

  // module.controller('ConfigurationDetailController', [
  //   '$log', 'configurationDetail',
  //   function($log, configurationDetail) {
  //     $log.debug('ConfigurationDetailController >> configurationDetail=%O',
  //                configurationDetail);
  //     this.configuration = configurationDetail;
  //   }
  // ]);

})();
