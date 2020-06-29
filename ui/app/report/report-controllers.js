/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

(function () {
  'use strict';

  var module = angular.module('pnc.report');

  module.controller('BlacklistedArtifactsInProjectReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportResource',
    function($scope, $state, $log, ReportResource) {

      var that = this;

      that.afterSearch = false;
      that.defaultSortKey = 'groupId';
      that.defaultReverse = false;

      that.isResultNotEmpty = function() {
        return !_.isEmpty(that.reportResults);
      };

      that.reset = function() {
        that.reportResults = [];
        that.afterSearch = false;
      };

      that.search = function(scmUrl, revision, pomPath, additionalRepos) {
        ReportResource.getBlacklistedArtifactsInProject(scmUrl, revision, pomPath, additionalRepos).then(function(result) {

          that.reportResults = [];

          // Show all unique artifacts
          _.forEach(result, function(topLevelDependency){
            _.forEach(topLevelDependency.gavs, function(a){
              that.reportResults.push(a);
            });
          });

          that.reportResults = _(that.reportResults).uniq(function(a){
            return JSON.stringify(_.pick(a, ['groupId', 'artifactId', 'version']));
          });

          that.sortKey = that.defaultSortKey;
          that.reverse = that.defaultReverse;
          that.afterSearch = true;

          // Default sorting
          that.reportResults = _.chain(that.reportResults).sortBy(function(result){ return result[that.defaultSortKey]; }).value();

        }, function() {
          // error

          that.reportResults = [];
        });
      };

      that.sort = function(keyname){
        that.sortKey = keyname;
        that.reverse = !that.reverse;
      };

    }

  ]);

  module.controller('BuiltArtifactsInProjectReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportResource',
    function($scope, $state, $log, ReportResource) {

      var that = this;

      that.afterSearch = false;
      that.defaultSortKey = 'groupId';
      that.defaultReverse = false;

      that.pagination = {
        current: 1
      };

      that.defaultPageSize = 30;
      that.availableVersionsLimits = [];

      that.expandAll = function() {
        // get boundaries for current page
        var start = that.defaultPageSize * (that.pagination.current - 1);
        var end = start + that.defaultPageSize;

        that.reportResults.slice(start, end).forEach(function(value, index) {
          that.availableVersionsLimits[index] = value.availableVersions.length;
        });
      };

      that.isResultNotEmpty = function() {
        return !_.isEmpty(that.reportResults);
      };

      that.reset = function() {
        that.reportResults = [];
        that.afterSearch = false;
      };

      that.search = function(scmUrl, revision, pomPath, additionalRepos) {
        ReportResource.getBuiltArtifactsInProject(scmUrl, revision, pomPath, additionalRepos).then(function(result) {
          that.reportResults = result;
          that.sortKey = that.defaultSortKey;
          that.reverse = that.defaultReverse;
          that.afterSearch = true;

          // Default sorting
          that.reportResults = _.chain(that.reportResults).sortBy(function(result){ return result[that.defaultSortKey]; }).value();

        }, function() {
          // error

          that.reportResults = [];
        });
      };

      that.sort = function(keyname){
        that.sortKey = keyname;
        that.reverse = !that.reverse;
      };

    }

  ]);



  module.controller('ProjectProductDiff', [
    '$rootScope',
    '$timeout',
    'ReportResource',
    'productList',
    function($rootScope, $timeout, ReportResource, productList) {

      var that = this;

      var getProductLabel = function(product) {
        return product.name + ' ' + product.version + ' (' + product.supportStatus + ')';
      };

      // initialize form variable with default (empty) data, this will be used to reset the form
      var initData = function() {
        that.form = {
          scmUrl : {
            error: false
          },
          productId : {
            error: false,
            productId: null,
            products: {
              data: []
            },
            selectedProducts: []
          },
          data: { // data that will be sent via REST in an expected format
            scmUrl: null,
            revision: null,
            pomPath: null,
            products: [],
            searchUnknownProducts: false
          }
        };
        // initialize the product dropdown
        that.form.productId.products.data = _(productList).map(function(product) {
          product.fullDisplayText = product.displayText = getProductLabel(product);
          return product;
        }).value();
      };

      initData();

      that.reset = function() {
        initData();
        that.productSelectControl.reset();
      };

      // validate form before submitting, returns true if valid
      that.validate = function() {
        that.form.scmUrl.error = !that.form.data.scmUrl;
        that.form.productId.error = that.form.data.products.length === 0;
        if(that.form.scmUrl.error || that.form.productId.error) { return false; }
        if(!that.form.data.revision) { that.form.data.revision = 'master'; }
        if(!that.form.data.pomPath) { that.form.data.pomPath = 'pom.xml'; }
        return true;
      };

      that.showTable = false;
      that.submitDisabled = false;

      // main action upon form submit
      that.computeDifference = function() {
        $rootScope.showSpinner = true;
        that.submitDisabled = true;
        that.form.data.products = _(that.form.productId.selectedProducts).map(function(product) { return product.id; }).value();
        if(!that.validate()) { return; } // halt when invalid
        // execute request
        ReportResource.diffProjectProduct(that.form.data).then(function(res) {
          that.tableData = transform(res); // process the received data into presentable form
          $timeout(function() {
            that.collapseAll();
          });
          that.showTable = true;
        }).catch(function() {
          that.showTable = false;
        }).finally(function() {
          $rootScope.showSpinner = false;
          that.submitDisabled = false;
        });
      };

      // process the REST response data
      var transform = function(data) {
        var TYPE_NOTES = {
          'internallyBuilt': 'INTERNALLY BUILT',
          'builtInDifferentVersion': 'BUILT IN DIFFERENT VERSION',
          'notBuilt': 'NOT BUILT',
          'blacklisted': 'BLACKLISTED'
        };

        var res = { data: {}, productNames: [], moduleNames: [] };

        _.forEach(TYPE_NOTES, function(note, type) {
          _.forEach(data[type], function(module) {
            var moduleName = module.groupId + ':' + module.artifactId;
            if(res.moduleNames.indexOf(moduleName) === -1) {
              res.moduleNames.push(moduleName);
            }
            if(!_(res.data).has(moduleName)) {
              res.data[moduleName] = [];
            }

            if(type === 'internallyBuilt' || type === 'builtInDifferentVersion') {
              _.forEach(module.gavProducts, function(dependency) {
                dependency.name = dependency.groupId + ':' + dependency.artifactId;
                dependency._type = type;
                dependency.versions = { '__project': dependency.version };
                dependency.differenceTypes = [];
                _.forEach(dependency.gavProducts, function(productDetails) {
                  var productName = getProductLabel(productDetails.product);
                  if(res.productNames.indexOf(productName) === -1) {
                    res.productNames.push(productName);
                  }
                  dependency.versions[productName] = productDetails.version;
                  dependency.differenceTypes[productName] = productDetails.differenceType;
                });
                res.data[moduleName].push(dependency);
              });
            }

            if(type === 'notBuilt' || type === 'blacklisted') {
              _.forEach(module.gavs, function(dependency) {
                dependency.name = dependency.groupId + ':' + dependency.artifactId;
                dependency._type = type;
                dependency.versions = { '__project': dependency.version };
                res.data[moduleName].push(dependency);
              });
            }
          });
        });

        // add notes to missing version values and find latest version to highlight
        _.forEach(res.data, function(dependencies) {
          _.forEach(dependencies, function(dependency) {
            // select the latest version to highlight
            dependency._latestVersion = _(dependency.versions).chain().values().sortBy(_.identity).value().reverse()[0];
            // add note to empty version, either plain '-' or 'BLACKLISTED'
            _.forEach(res.productNames, function(productName) {
              if(_.isUndefined(dependency.versions[productName])) {
                if(dependency._type === 'blacklisted') {
                  dependency.versions[productName] = 'BLACKLISTED';
                }
                if(dependency._type === 'notBuilt') {
                  dependency.versions[productName] = '-';
                }
              }
            });
          });
        });

        // sort dependencies inside modules alphabetically by artifactId
        _.forEach(res.data, function(dependencies, moduleName) {
          res.data[moduleName] = _(dependencies).sortBy(function(dependency) { return dependency.artifactId; }).value();
        });
        return res;
      };

      that.expandAll = function() {
        $('.collapse').collapse('show');
      };

      that.collapseAll = function() {
        $('.collapse').collapse('hide');
      };
    }
  ]);


})();
