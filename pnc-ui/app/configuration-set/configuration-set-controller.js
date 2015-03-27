'use strict';

(function() {

  var module = angular.module('pnc.configuration-set');

  module.controller('ConfigurationSetListController', [
    '$log',
    '$state',
    'configurationSetList',
    function($log, $state, configurationSetList) {
      $log.debug('ConfigurationSetListController >> this=%O, configurationSetList=%O',
                 this, configurationSetList);

      this.buildconfigurationsets = configurationSetList;
    }
  ]);

  module.controller('ConfigurationSetDetailController', [
    '$log',
    '$state',
    'Notifications',
    'configurationSetDetail',
    'configurations',
    function($log, $state, Notifications, configurationSetDetail, configurations) {
      $log.debug('ConfigurationSetDetailController >> this=%O', this);
      this.set = configurationSetDetail;
      this.configurations = configurations;

      var that = this;

      this.build = function() {
        $log.debug('**Initiating build of SET: %s**', this.set.name);

        this.set.$build().then(
          function(result) {
            $log.debug('Initiated Build: %O, result: %O', that.set,
                       result);
            Notifications.success('Initiated build of configurationSet:' +
                                  that.set.name);
          },
          function(response) {
            $log.error('Failed to initiated build: %O, response: %O',
                       that.set, response);
            Notifications.error('Action Failed.');
          }
        );
      };

      this.delete = function() {
        this.set.$delete().then(
          // Success
          function (result) {
            $log.debug('Delete Config Set: %O success result: %O',
             that.set, result);
            Notifications.success('Configuration Set Deleted');

            // TODO: Think of a better place to navigate to
            $state.go('product.list', {}, { reload: true, inherit: false,
              notify: true });
          },
          // Failure
          function (response) {
            $log.error('Delete ConfigurationSet: %O failed, response: %O',
             that.set, response);
            Notifications.error('Action Failed.');
          }
        );
      };

    }
    ]);

})();
