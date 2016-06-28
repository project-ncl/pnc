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

  var module = angular.module('pnc.report');

  module.controller('ProductShippedReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportDAO',
    'whitelistProducts',
    function($scope, $state, $log, ReportDAO, whitelistProducts) {

    	var that = this;
    	that.reportResults = [];
    	that.products = {};
    	that.products.data = _.clone(whitelistProducts);

    	that.defaultPageSize = 50;
    	that.defaultSortKey = 'gav.groupId';
    	that.defaultReverse = false;

    	that.productSelection = {
          selected: []
        };

        /* Enrich the data to use pnc-select-items directive */
        _.each(that.products.data, function(product){
          product.displayBoldText = product.name;
          product.displayText = ' - ' + product.version + ' (' + product.supportStatus + ')';
          product.fullDisplayText = product.displayBoldText + product.displayText;
        });

        that.isProductSelected = function() {
          return (!_.isUndefined(that.productSelection) && that.productSelection.selected.length > 0);
        };

        that.isResultNotEmpty = function() {
          return (!_.isUndefined(that.reportResults) && that.reportResults.length > 0);
        };

        that.reset = function(form) {
          if (form) {
            that.productSelection.selected = [];
            that.selectedProductId = undefined;
            that.reportResults = [];
            that.reportSearchFilter = {};
            form.$setPristine();
            form.$setUntouched();
          }
        };

        that.search = function() {
          ReportDAO.getWhitelistProductArtifacts(that.productSelection.selected[0]).then(function(result) {
            that.reportResults = result;
            // Default sorting is ascending on gav.groupId
            that.reportResults = _.chain(that.reportResults).sortBy(function(result){ return result.gav.groupId; }).value();
            that.reportSearchFilter = that.productSelection.selected[0];
            that.sortKey = that.defaultSortKey;
            that.reverse = that.defaultReverse;
          });
        };

        that.sort = function(keyname){
          that.sortKey = keyname;   //set the sortKey to the param passed
          that.reverse = !that.reverse; //if true make it false and vice versa
        };
    }
  ]);



  module.controller('ProductsForArtifactReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportDAO',
    function($scope, $state, $log, ReportDAO) {

      var that = this;

      that.reportResults = [];
      that.gav = {};
      that.afterSearch = false;

      that.defaultSortKey = 'name';
      that.defaultReverse = false;

      that.isResultNotEmpty = function() {
        return !_.isEmpty(that.reportResults);
      };
      
      that.reset = function(form) {
        if (form) {
          that.gav = {};
          that.reportResults = [];
          that.afterSearch = false;
          form.$setPristine();
          form.$setUntouched();
        }
      };

      that.search = function() {
        ReportDAO.getProductsByGAV(that.gav.groupId, that.gav.artifactId, that.gav.version).then(function(result) {
            that.reportResults = result;
            that.sortKey = that.defaultSortKey;
            that.reverse = that.defaultReverse;
            that.afterSearch = true;

            // Default sorting 
            that.reportResults = _.chain(that.reportResults).sortBy(function(result){ return result[that.defaultSortKey]; }).value();
        });
      };

      that.sort = function(keyname){
        that.sortKey = keyname;   
        that.reverse = !that.reverse;
      };

    }

  ]);




  module.controller('BlacklistedArtifactsInProjectReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportDAO',
    function($scope, $state, $log, ReportDAO) {

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
        ReportDAO.getBlacklistedArtifactsInProject(scmUrl, revision, pomPath, additionalRepos).then(function(result) {

          that.reportResults = [];

          // Show all unique artifacts
          _(result).each(function(topLevelDependency){
            _(topLevelDependency.gavs).each(function(a){
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

  module.controller('DifferentArtifactsInProductsReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportDAO',
    'whitelistProducts',
    function($scope, $state, $log, ReportDAO, whitelistProducts) {

      var that = this;
      that.afterSearch = false;
      that.gavsAdded = [];
      that.gavsRemoved = [];
      that.gavsChanged = [];
      that.gavsUnchanged = [];
      that.products = {};
      that.products.data = _.clone(whitelistProducts);

      that.defaultPageSize = 50;

      that.defaultAddedSortKey = 'groupId';
      that.defaultRemovedSortKey = 'groupId';
      that.defaultChangedSortKey = 'groupId';
      that.defaultUnchangedSortKey = 'groupId';
      that.defaultAddedReverse = false;
      that.defaultRemovedReverse = false;
      that.defaultChangedReverse = false;
      that.defaultUnchangedReverse = false;

      that.productLeftSelection = {
        selected: []
      };
      that.productRightSelection = {
        selected: []
      };

      /* Enrich the data to use pnc-select-items directive */
      _.each(that.products.data, function(product){
        product.displayBoldText = product.name;
        product.displayText = ' - ' + product.version + ' (' + product.supportStatus + ')';
        product.fullDisplayText = product.displayBoldText + product.displayText;
      });

      that.isProductLeftSelected = function() {
        return (!_.isUndefined(that.productLeftSelection) && that.productLeftSelection.selected.length > 0);
      };
      that.isProductRightSelected = function() {
        return (!_.isUndefined(that.productRightSelection) && that.productRightSelection.selected.length > 0);
      };

      that.isGavAddedNotEmpty = function() {
        return (!_.isUndefined(that.gavsAdded) && that.gavsAdded.length > 0);
      };
      that.isGavRemovedNotEmpty = function() {
        return (!_.isUndefined(that.gavsRemoved) && that.gavsRemoved.length > 0);
      };
      that.isGavChangedNotEmpty = function() {
        return (!_.isUndefined(that.gavsChanged) && that.gavsChanged.length > 0);
      };
      that.isGavUnchangedNotEmpty = function() {
        return (!_.isUndefined(that.gavsUnchanged) && that.gavsUnchanged.length > 0);
      };

      that.isFormValid = function() {
        return (!_.isUndefined(that.productLeftSelection) && that.productLeftSelection.selected.length > 0) && 
          (!_.isUndefined(that.productRightSelection) && that.productRightSelection.selected.length > 0) && 
          that.productLeftSelection.selected[0].id !== that.productRightSelection.selected[0].id;
      };

      that.reset = function(form) {
        if (form) {

          that.productLeftSelection.selected = [];
          that.productRightSelection.selected = [];
          that.selectedProductLeftId = undefined;
          that.selectedProductRightId = undefined;
          that.afterSearch = false;
          that.gavsAdded = [];
          that.gavsRemoved = [];
          that.gavsChanged = [];
          that.gavsUnchanged = [];
          that.reportLeftProductSearchFilter = {};
          that.reportRightProductSearchFilter = {};
          form.$setPristine();
          form.$setUntouched();
        }
      };

      that.search = function() {
        ReportDAO.getDifferentArtifactsInProducts(that.productLeftSelection.selected[0], that.productRightSelection.selected[0]).then(function(result) {

          that.reportLeftProductSearchFilter = that.productLeftSelection.selected[0];
          that.reportRightProductSearchFilter = that.productRightSelection.selected[0];
          that.afterSearch = true;
          that.gavsAdded = result.added;
          that.gavsRemoved = result.removed;
          that.gavsChanged = result.changed;
          that.gavsUnchanged = result.unchanged;

          // Default sorting is ascending on gav.groupId
          that.gavsAdded = _.chain(that.gavsAdded).sortBy(function(result){ return result.groupId; }).value();
          that.gavsRemoved = _.chain(that.gavsRemoved).sortBy(function(result){ return result.groupId; }).value();
          that.gavsChanged = _.chain(that.gavsChanged).sortBy(function(result){ return result.groupId; }).value();
          that.gavsUnchanged = _.chain(that.gavsUnchanged).sortBy(function(result){ return result.groupId; }).value();

          that.sortKeyAdded = that.defaultAddedSortKey;
          that.reverseAdded = that.defaultAddedReverse;

          that.sortKeyRemoved = that.defaultRemovedSortKey;
          that.reverseRemoved = that.defaultRemovedReverse;

          that.sortKeyChanged = that.defaultChangedSortKey;
          that.reverseChanged = that.defaultChangedReverse;

          that.sortKeyUnchanged = that.defaultUnchangedSortKey;
          that.reverseUnchanged = that.defaultUnchangedReverse;
        });
      };

      that.sortAdded = function(keyname){
        that.sortKeyAdded = keyname;
        that.reverseAdded = !that.reverseAdded;
      };
      that.sortRemoved = function(keyname){
        that.sortKeyRemoved = keyname;
        that.reverseRemoved = !that.reverseRemoved;
      };
      that.sortChanged = function(keyname){
        that.sortKeyChanged = keyname;
        that.reverseChanged = !that.reverseChanged;
      };
      that.sortUnchanged = function(keyname){
        that.sortKeyUnchanged = keyname;
        that.reverseUnchanged = !that.reverseUnchanged;
      };
    }
  ]);


  module.controller('BuiltArtifactsInProjectReportController', [
    '$scope',
    '$state',
    '$log',
    'ReportDAO',
    function($scope, $state, $log, ReportDAO) {

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
        ReportDAO.getBuiltArtifactsInProject(scmUrl, revision, pomPath, additionalRepos).then(function(result) {
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
    'ReportDAO',
    'productList',
    function($rootScope, $timeout, ReportDAO, productList) {

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
        });
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
        that.form.data.products = _(that.form.productId.selectedProducts).map(function(product) { return product.id; });
        if(!that.validate()) { return; } // halt when invalid
        // execute request
        ReportDAO.diffProjectProduct(that.form.data).then(function(res) {
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

        _(TYPE_NOTES).each(function(note, type) {
          _(data[type]).each(function(module) {
            var moduleName = module.groupId + ':' + module.artifactId;
            if(!_(res.moduleNames).contains(moduleName)) {
              res.moduleNames.push(moduleName);
            }
            if(!_(res.data).has(moduleName)) {
              res.data[moduleName] = [];
            }

            if(type === 'internallyBuilt' || type === 'builtInDifferentVersion') {
              _(module.gavProducts).each(function(dependency) {
                dependency.name = dependency.groupId + ':' + dependency.artifactId;
                dependency._type = type;
                dependency.versions = { '__project': dependency.version };
                _(dependency.gavProducts).each(function(productAndVersion) {
                  var productName = getProductLabel(productAndVersion.product);
                  if(!_(res.productNames).contains(productName)) {
                    res.productNames.push(productName);
                  }
                  dependency.versions[productName] = productAndVersion.version;
                });
                res.data[moduleName].push(dependency);
              });
            }

            if(type === 'notBuilt' || type === 'blacklisted') {
              _(module.gavs).each(function(dependency) {
                dependency.name = dependency.groupId + ':' + dependency.artifactId;
                dependency._type = type;
                dependency.versions = { '__project': dependency.version };
                res.data[moduleName].push(dependency);
              });
            }
          });
        });

        // add notes to missing version values and find latest version to highlight
        _(res.data).each(function(dependencies) {
          _(dependencies).each(function(dependency) {
            // select the latest version to highlight
            dependency._latestVersion = _(dependency.versions).chain().values().sortBy(_.identity).value().reverse()[0];
            // add note to empty version, either plain '-' or 'BLACKLISTED'
            _(res.productNames).each(function(productName) {
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
        _(res.data).each(function(dependencies, moduleName) {
          res.data[moduleName] = _(dependencies).sortBy(function(dependency) { return dependency.artifactId; });
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
