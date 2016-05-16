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

      that.reportResults = [];
      that.scmUrl = '';
      that.afterSearch = false;

      that.defaultSortKey = 'groupId';
      that.defaultReverse = false;

      that.isResultNotEmpty = function() {
        return !_.isEmpty(that.reportResults);
      };
      
      that.reset = function(form) {
        if (form) {
          that.scmUrl = '';
          that.reportResults = [];
          that.afterSearch = false;
          form.$setPristine();
          form.$setUntouched();
        }
      };

      that.search = function() {
        ReportDAO.getBlacklistedArtifactsInProject(that.scmUrl).then(function(result) {

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
          
        });
      };

      that.sort = function(keyname){
        that.sortKey = keyname;   
        that.reverse = !that.reverse;
      };

    }

  ]);




})();
