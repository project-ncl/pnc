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

      this.products = productList;
    }
  ]);

  module.controller('ProductDetailController', [
    '$log',
    'productDetail',
    'productVersions',
    'ProductMilestoneDAO',
    'ProductReleaseDAO',
    function($log, productDetail, productVersions, ProductMilestoneDAO, ProductReleaseDAO) {

      var that = this;
      that.product = productDetail;
      that.versions = productVersions;

      // Update a product after editing
      that.update = function() {
        $log.debug('Updating product: %O', that.product);
        that.product.$update();
      };

      // Build wrapper objects
      that.versionMilestones = [];
      that.versionReleases = [];

      // Retrieve all the artifacts of all the build records of the build configurations set
      angular.forEach(that.versions, function(version) {

        ProductMilestoneDAO.getAllForProductVersion({
          versionId: version.id
        }).then(
          function(results) {
            angular.forEach(results, function(result) {
              that.versionMilestones.push(result);
            });
          }
        );

        ProductReleaseDAO.getAllForProductVersion({
          versionId: version.id
        }).then(
          function(results) {
            angular.forEach(results, function(result) {
              that.versionReleases.push(result);
            });
          }
        );
      });
    }
  ]);

  module.controller('ProductVersionController', [
    '$log',
    '$state',
    'productDetail',
    'versionDetail',
    function($log, $state, productDetail, versionDetail) {

      var that = this;
      that.product = productDetail;
      that.version = versionDetail;

      // Update a product version after editing
      that.update = function() {
        $log.debug('Updating product version: %O', that.version);
        that.version.$update(
        ).then(
          function() {
            $state.go('product.detail.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {
              reload: true
            });
          }
        );
      };
    }
  ]);

  module.controller('ProductCreateController', [
    '$state',
    '$log',
    'ProductDAO',
    function($state, $log, ProductDAO) {

      this.data = new ProductDAO();
      var that = this;

      that.submit = function() {
        that.data.$save().then(function(result) {
          $state.go('product.detail', {
            productId: result.id
          });
        });
      };
    }
  ]);

  module.controller('ProductVersionCreateController', [
    '$state',
    '$log',
    'ProductVersionDAO',
    'productDetail',
    function($state, $log, ProductVersionDAO, productDetail) {

      this.data = new ProductVersionDAO();
      this.product = productDetail;
      var that = this;

      that.submit = function() {
        that.data.productId = that.product.id;
        //that.data.version = that.data.version;
        that.data.$save().then(function(result) {
            $state.go('product.detail', {
              productId: result.productId
            }, {
              reload: true
            });
          }
        );
      };
    }
  ]);

})();
