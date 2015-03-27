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
    '$log', 'productDetail', 'productVersions',
    function ($log, productDetail, productVersions) {
      $log.debug('ProductDetailController >> this=%O, productDetail=%O, ' +
                 'productVersions=%O', this, productDetail, productVersions);

      this.product = productDetail;
      this.versions = productVersions;
    }
  ]);

  module.controller('ProductVersionController', [
    '$log', 'productDetail', 'versionDetail', 'buildConfigurationSets',
    function ($log, productDetail, versionDetail, buildConfigurationSets) {
      $log.debug('VersionDetailController >> this=%O, productDetail=%O, ' +
                 'versionDetail=%O, buildConfigurationSets=%0', this, productDetail, versionDetail, buildConfigurationSets);

      this.product = productDetail;
      this.version = versionDetail;
      this.buildconfigurationsets = buildConfigurationSets;
    }
  ]);

})();
