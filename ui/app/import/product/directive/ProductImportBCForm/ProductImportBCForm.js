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
  module.directive('productImportBCForm', [
    'Notifications',
    function (Notifications) {

      return {
        restrict: 'E',
        templateUrl: 'import/product/directive/ProductImportBCForm/product-import-bc-form.html',
        scope: {
          node: '=',
          validateFormCaller: '='
        },
        link: function (scope) {
          scope.refresh = _.noop;
          scope.$watch('node', function () {
            scope.data = scope.node.nodeData;
            scope.analyzeNextLevelDisabled = scope.node.nlaSuccessful;
            dirtyForm();
          });


          var dirtyForm = function() {
            _(scope.bcForm).each(function (field) {
              if (_(field).has('$dirty') && field.$pristine) {
                field.$dirty = true;
              }
            });
          };


          var validate = function () {
            if (scope.bcForm.$valid && scope.data.environment.id !== null && scope.data.project.id !== null) {
              return true;
            } else {
              dirtyForm();
              Notifications.warn('Some data are invalid or missing. Verify that form for ' +
                scope.node.gavString + ' is correctly filled in.');
              return false;
            }
          };


          scope.submit = function () {
            if (validate()) {
              scope.node.analyze();
            }
          };


          scope.validateFormCaller.call = validate;
        }
      };
    }
  ]);

})();
