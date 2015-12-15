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
   * @ngdoc directive
   * @name pnc.project:pncBuildConfigurations
   * @restrict E
   * @description
   * Displays a searchable, paged table of Build Configurations for a given project.
   * @example
   * @author Jakub Senko
   * @author Alex Creasy
   */
  module.directive('pncBuildConfigurations', [
    '$q',
    'PncCache',
    'BuildConfigurationDAO',
    function ($q, PncCache, BuildConfigurationDAO) {

      return {
        restrict: 'E',
        templateUrl: 'project/directives/pnc-build-configurations/pnc-build-configurations.html',
        scope: {
          pncProject: '='
        },
        link: function (scope) {

          scope.page = $q.when(scope.pncProject).then(function(project) {

            return PncCache.key('pnc.record.pncBuildConfigurations').key('projectId:' + project.id).key('page').getOrSet(function() {
              return BuildConfigurationDAO.getPagedByProject({ projectId: project.id });
            }).then(function(page) {
              page.reload();
              return page;
            });

          });
        }
      };
    }
  ]);

})();
