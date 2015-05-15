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

  module.controller('ProductListController', [
    '$log', '$state', 'productList',
    function($log, $state, productList) {
      $log.debug('ProductListController >> this=%O, productList=%O',
                 this, productList);

      this.products = productList;
    }
  ]);

  module.controller('ProductDetailController', [
    '$log', 'productDetail', 'productVersions', 'Notifications',
    function ($log, productDetail, productVersions, Notifications) {
      $log.debug('ProductDetailController >> this=%O, productDetail=%O, ' +
                 'productVersions=%O', this, productDetail, productVersions);
      var that = this;
      that.product = productDetail;
      that.versions = productVersions;

      // Update a product after editing
      that.update = function() {
        $log.debug('Updating product: %O', that.product);

        that.product.$update().then(
          function(result) {
            $log.debug('Update Product: %O, result: %O', that.product,
                       result);
            Notifications.success('Product updated.');
          },
          function(response) {
            $log.error('Update product: %O failed, response: %O',
                       that.product, response);
            Notifications.error('Action Failed.');
          }
        );
      };
    }
  ]);

  module.controller('ProductVersionController', [
    '$log', 'productDetail', 'versionDetail', 'buildConfigurationSets', 'productReleases', 'productMilestones',
    function ($log, productDetail, versionDetail, buildConfigurationSets, productReleases, productMilestones) {
      $log.debug('VersionDetailController >> this=%O, productDetail=%O, ' +
                 'versionDetail=%O, buildConfigurationSets=%0', this, productDetail, versionDetail, buildConfigurationSets);

      this.product = productDetail;
      this.version = versionDetail;
      this.buildconfigurationsets = buildConfigurationSets;
      this.productreleases = productReleases;
      this.productmilestones = productMilestones;
    }
  ]);

  module.controller('ProductCreateController', [
    '$state',
    '$log',
    'PncRestClient',
    'Notifications',
    function($state, $log, PncRestClient, Notifications) {

      this.data = new PncRestClient.Product();
      var that = this;

      that.submit = function() {
        that.data.$save().then(
          function(result) {
            Notifications.success('Product created');
            $state.go('product.detail', {
              productId: result.id
            });
          },
          function(response) {
            $log.error('Create product failed: response: %O', response);
            Notifications.error('Action Failed.');
          }
        );
      };
    }
  ]);

})();
