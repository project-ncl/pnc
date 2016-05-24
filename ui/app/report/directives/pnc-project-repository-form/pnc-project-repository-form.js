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

  /**
   * @ngdoc directive
   * @name pnc.report:pncProjectRepositoryForm
   * @restrict E
   * @param {string@} searchCallback
   * Search logic which needs to be done once search button is clicked.
   * @param {string@} resetCallback
   * Additional reset logic which needs to be done once reset button is clicked.
   * @description
   * A directive that allows users to enter project repository related information.
   * @example
   * <pnc-project-repository-form
      search-callback="search(scmUrl, revision, pomPath, additionalRepos)"
      reset-callback="reset()">
     </pnc-project-repository-form>
   */
  module.directive('pncProjectRepositoryForm', function() {
    return {
      restrict: 'E',
      scope: {
        searchCallback: '&',
        resetCallback: '&'
      },
      templateUrl: 'report/directives/pnc-project-repository-form/pnc-project-repository-form.html',
      controller: [
        '$scope',
        function($scope) {
          
          var resetFields = function() {
            // fields
            $scope.scmUrl = '';
            $scope.revision = '';
            $scope.pomPath = '';
            $scope.additionalRepos = [];
          };

          resetFields();

          $scope.reset = function(form) {
            if (form) {
              resetFields();

              $scope.resetCallback();

              form.$setPristine();
              form.$setUntouched();
            }
          };

          $scope.search = function() {
            $scope.searchCallback({
              scmUrl: $scope.scmUrl, 
              revision: $scope.revision, 
              pomPath: $scope.pomPath, 
              additionalRepos: $scope.additionalRepos
            });
          };

        }
      ]
    };

  });

})();
