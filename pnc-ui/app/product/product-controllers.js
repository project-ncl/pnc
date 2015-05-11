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
