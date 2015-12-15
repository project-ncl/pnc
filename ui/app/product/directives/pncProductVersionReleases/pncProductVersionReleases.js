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

(function () {

  var module = angular.module('pnc.record');

  /**
   * @author Jakub Senko
   */
  module.directive('pncProductVersionReleases', [
    '$log',
    '$state',
    'PncCache',
    'ProductVersionDAO',
    'ProductReleaseDAO',
    function ($log, $state, PncCache, ProductVersionDAO, ProductReleaseDAO) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersionReleases/pnc-product-version-releases.html',
        scope: {
          version: '='
        },
        link: function (scope) {

          var productmilestones = scope.version.getMilestones();

          scope.page = PncCache.key('pnc.record.pncProductVersionReleases').key('versionId:' + scope.version.id).key('page').getOrSet(function() {
            return ProductReleaseDAO.getPagedByProductVersion({versionId: scope.version.id });
          }).then(function(page) {
            page.reload();
            return page;
          });

          scope.getMilestoneVersion = function(milestoneId) {
            var milestoneVersion = '';
            angular.forEach(productmilestones, function(versionMilestone) {
              if (versionMilestone.id === milestoneId) {
                milestoneVersion = versionMilestone.version;
              }
            });
            return milestoneVersion;
          };
        }
      };
    }
  ]);

})();
