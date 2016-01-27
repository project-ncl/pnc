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

  var module = angular.module('pnc.import');

  /**
   * @author Jakub Senko
   */
  module.directive('projectDropdown', [
    'ProjectDAO',
    function (ProjectDAO) {

      return {
        restrict: 'E',
        templateUrl: 'import/product/directive/ProjectDropdown/dropdown.html',
        scope: {
          selectedId: '='
        },
        link: function (scope) {

          scope.selectedName = 'Select';

          var loader = ProjectDAO.query;
          var promise = loader().then(function (r) {
            scope.items = r;
          });

          scope.select = function (item) {
            if (!_.isUndefined(item)) {
              scope.selectedId = item.id;
              scope.selectedName = item.name;
            } else {
              scope.selectedId = null;
              scope.selectedName = 'Select';
            }
          };

          scope.$watch('selectedId', function (newval) {
            promise.then(function () {
              scope.select(_(scope.items).find(function (item) {
                return item.id === newval;
              }));
            });
          });

          scope.$root.$on('projectCreated', function(event, args) {
            scope.items.unshift(args.project);
          });
        }
      };
    }
  ]);

})();
