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
            Notifications.success('Product updated');
          },
          function(response) {
            $log.error('Update product: %O failed, response: %O',
                       that.product, response);
            Notifications.error('Product update failed');
          }
        );
      };
    }
  ]);

  module.controller('ProductVersionController', [
    '$log',
    'productDetail',
    'versionDetail',
    'buildConfigurationSets',
    'productReleases',
    'productMilestones',
    'Notifications',
    function ($log, productDetail, versionDetail, buildConfigurationSets, productReleases, productMilestones, Notifications) {
      $log.debug('VersionDetailController >> this=%O, productDetail=%O, ' +
                 'versionDetail=%O, buildConfigurationSets=%0', this, productDetail, versionDetail, buildConfigurationSets);

      var that = this;
      that.product = productDetail;
      that.version = versionDetail;
      that.buildconfigurationsets = buildConfigurationSets;
      that.productreleases = productReleases;
      that.productmilestones = productMilestones;

      // Update a product version after editing
      that.update = function() {
        $log.debug('Updating product version: %O', that.version);

        that.version.$update().then(
          function(result) {
            $log.debug('Update Product Version: %O, result: %O', that.version,
                       result);
            Notifications.success('Product Version updated');
          },
          function(response) {
            $log.error('Update product version: %O failed, response: %O',
                       that.version, response);
            Notifications.error('Product Version update failed');
          }
        );
      };
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
            Notifications.error('Product creation failed');
          }
        );
      };
    }
  ]);

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
