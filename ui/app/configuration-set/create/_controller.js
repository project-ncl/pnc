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

(function() {

  var module = angular.module('pnc.configuration-set');

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
              $state.go('product.detail.version.detail', params, { reload: true, inherit: false,
                      notify: true });
            }
            else {
              $state.go('configuration-set.list');
            }
          },
          function(response) {
            $log.error('Create Configuration Set failed: response: %O', response);
            Notifications.error('Configuration Set creation failed');
          }
        );
      };
    }
  ]);

})();
