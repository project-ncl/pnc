/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

(function () {

  var module = angular.module('pnc.record');

  /**
   * @author Jakub Senko
   */
  module.directive('pncProductVersions', [
    'ProductVersionDAO',
    function (ProductVersionDAO) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersions/pnc-product-versions.html',
        scope: {
          productId: '='
        },
        link: function (scope) {

          scope.page = ProductVersionDAO.getPagedByProduct({productId: scope.productId});

          var formatDate = function(date) {
            return date.getFullYear() + '/' + date.getMonth() + '/' + date.getDate();
          };

          var convertFromTimestamp = function(mSec) {
            var now = new Date();
            return new Date(mSec + (now.getTimezoneOffset() * 60 * 1000) - (12 * 60 * 60 * 1000));
          };

          scope.getMilestoneTooltip = function(milestone) {
            var sDate = '';
            var prDate = '';
            var rDate = '';
            if (milestone.startingDate) {
              sDate = formatDate(convertFromTimestamp(milestone.startingDate));
            }
            if (milestone.plannedReleaseDate) {
              prDate = formatDate(convertFromTimestamp(milestone.plannedReleaseDate));
            }
            if (milestone.releaseDate) {
              rDate = formatDate(convertFromTimestamp(milestone.releaseDate));
            }
            var milestoneTooltip = '<strong>' + milestone.version + '</strong>' +
              '<br><br><strong>Phase: </strong> &lt;tbd&gt; <br>' +
              '<strong>Starting date: </strong>' + sDate + '<br>' +
              '<strong>Planned release date: </strong>' + prDate + '<br>' +
              '<strong>Release date: </strong>' + rDate + '<br>';
            return milestoneTooltip;
          };

          scope.getReleaseTooltip = function(release) {
            var rDate = '';
            if (release.releaseDate) {
              rDate = formatDate(convertFromTimestamp(release.releaseDate));
            }
            var milestoneVersion = '';
            var releaseTooltip = '<strong>' + release.version + '</strong>' +
              '<br><br><strong>Phase: </strong> &lt;tbd&gt; <br>' +
              '<strong>Release date: </strong>' + rDate + '<br>' +
              '<strong>Released from Milestone: </strong>' + milestoneVersion + '<br>' +
              '<strong>Support Level: </strong>' + release.supportLevel + '<br>';
            return releaseTooltip;
          };
        }
      };
    }
  ]);

})();
