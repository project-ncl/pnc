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

  module.controller('ConfigurationSetCreateController', [
    '$log',
    '$state',
    'products',
    'PncRestClient',
    'Notifications',
    function($log, $state, products, PncRestClient, Notifications) {
      $log.debug('ConfigurationSetCreateController >> this=%O, products=%O',
                 this, products);

      this.data = new PncRestClient.ConfigurationSet();

      var self = this;
      self.products = products;
      self.productVersions = [];

      self.getProductVersions = function(productId) {
        $log.debug('**Getting productVersions of Product: %0**', productId);

        if (productId) {
          PncRestClient.Version.query({
            productId: productId
          }).$promise.then(
            function(result) {
              self.productVersions = result;
              if (result) {
                self.data.productVersionId = result[0].id;
              }
            }
          );
        }
        else {
          self.productVersions = [];
        }
      };

      this.submit = function() {
        self.data.$save().then(
          function(result) {
            $log.debug('Configuration Set created: %s', result);
            Notifications.success('Configuration Set created');
            if (self.data.productVersionId) {
              var params = { productId: parseInt(self.selectedProductId), versionId: self.data.productVersionId };
              $state.go('product.version', params, { reload: true, inherit: false,
                      notify: true });
            }
            else {
              $state.go('configuration-set.list');
            }
          },
          function(response) {
            $log.error('Create Configuration Set failed: response: %O', response);
            Notifications.error('Action Failed.');
          }
        );
      };
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
