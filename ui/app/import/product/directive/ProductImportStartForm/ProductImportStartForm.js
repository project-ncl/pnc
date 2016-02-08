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

  var module = angular.module('pnc.import');

  /**
   * @author Jakub Senko
   */
  module.directive('productImportStartForm', [

    function () {

      return {
        restrict: 'E',
        templateUrl: 'import/product/directive/ProductImportStartForm/product-import-start-form.html',
        scope: {
          submitDisabled: '=',
          data: '=',
          onSubmit: '&'
        },
        link: function (scope) {
          scope.submit = function () {
            if (scope.startForm.$valid) {
              if(!scope.data.scmRevision || scope.data.scmRevision.length === 0) { scope.data.scmRevision = 'master'; }
              if(!scope.data.pomPath || scope.data.pomPath.length === 0) { scope.data.pomPath = './pom.xml'; }
              scope.onSubmit();
            } else {
              _(scope.startForm).each(function (field) {
                if (_(field).has('$dirty') && field.$pristine) {
                  field.$dirty = true;
                }
              });
            }
          };
          scope.addOptionalRepository = function() {
            // check if it is defined. If no, define it
            if (typeof scope.data.repositories === 'undefined') {
              scope.data.repositories = [];
            }
            // add to the list
            scope.data.repositories.push(scope.optionalRepository);

            // clear the field in the UI
            scope.optionalRepository = '';
          };

          scope.removeOptionalRepository = function(index) {
            scope.data.repositories.splice(index, 1);
          };

          scope.formReset = function() {
            scope.data = {};
            scope.optionalRepository = '';
            scope.startForm.$setPristine();
          };
        }
      };
    }
  ]);

})();
