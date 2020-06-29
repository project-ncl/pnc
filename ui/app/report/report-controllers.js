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
    'ReportResource',
    function(ReportResource) {

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
    'ReportResource',
    function(ReportResource) {

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

})();
