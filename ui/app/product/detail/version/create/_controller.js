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

  var module = angular.module('pnc.product');

  module.controller('ProductVersionCreateController', [
    '$state',
    '$log',
    'PncRestClient',
    'Notifications',
    'productDetail',
    function($state, $log, PncRestClient, Notifications, productDetail) {

      $log.debug('ProductVersionCreateController >> this=%O, productDetail=%O, ', this, productDetail);

      this.data = new PncRestClient.Version();
      this.product = productDetail;
      var that = this;

      that.submit = function() {
        that.data.$save({productId: that.product.id }).then(
          function(result) {
            Notifications.success('Product Version created');
            $state.go('product.detail', {
              productId: productDetail.id,
              versionId: result.id
            });
          },
          function(response) {
            $log.error('Create product version failed: response: %O', response);
            Notifications.error('Product Version creation failed');
          }
        );
      };
    }
  ]);

})();
