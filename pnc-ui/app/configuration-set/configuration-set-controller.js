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
    'PncRestClient',
    function($log, $state, Notifications, configurationSetDetail, configurations, PncRestClient) {
      var self = this;

      $log.debug('ConfigurationSetDetailController >> this=%O', self);
      self.set = configurationSetDetail;
      self.configurations = configurations;

      self.build = function() {
        $log.debug('**Initiating build of SET: %s**', self.set.name);

        self.set.$build().then(
          function(result) {
            $log.debug('Initiated Build: %O, result: %O', self.set,
                       result);
            Notifications.success('Initiated build of configurationSet:' +
                                  self.set.name);
          },
          function(response) {
            $log.error('Failed to initiated build: %O, response: %O',
                       self.set, response);
            Notifications.error('Action Failed.');
          }
        );
      };

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

      self.remove = function(configurationId) {
        $log.debug('**Removing configurationId: %0 from Build Configuration Set: %0**', configurationId, self.set);

        PncRestClient.ConfigurationSet.removeConfiguration({
            configurationSetId: self.set.id,
            configurationId: configurationId
        }).$promise.then(
          // Success
          function (result) {
            $log.debug('Removal of Configuration from Configuration Set: %O success result: %O',
             self.set, result);
            Notifications.success('Configuration removed from Configuration Set');
            var params = { configurationSetId: self.set.id };
            $state.go('configuration-set.detail', params, { reload: true, inherit: false, notify: true });
          },
          // Failure
          function (response) {
            $log.error('Removal of Configuration from Configuration Set: %O failed, response: %O',
             self.set, response);
            Notifications.error('Action Failed.');
          }
        );
      };

    }
    ]);

})();
