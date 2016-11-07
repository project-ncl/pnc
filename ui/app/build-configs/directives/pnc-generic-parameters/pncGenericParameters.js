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

  var module = angular.module('pnc.build-configs');

  /**
   * @ngdoc directive
   * @name pnc.configuration:pncGenericParameters
   * @restrict E
   * @param {object=} generic-parameters
   * List of generic parameters.
   * @param {object=} control
   * Object with a set of methods for user to control and interact with this directive,
   * currently: reset() to clear the form & data.
   * @description
   * All-in-one component to configure generic parameters.
   * @example
      <pnc-generic-parameters
        generic-parameters="genericParameters"
        control="control">
      </pnc-generic-parameters>
   * @author Martin Kelnar
   */
  module.directive('pncGenericParameters', [
    function () {

      function PncGenericParameters($log, $scope, BuildConfigurationDAO) {

        function prepareSupportedGenericParameters() {
          var result = [];

          BuildConfigurationDAO.getSupportedGenericParameters().then(function(restData) {
            _.forOwn(restData, function(value, key) {
              result.push({
                id: key,
                displayBoldText: key,
                displayText: ' - ' + value,
                fullDisplayText: key
              });
            });
          });

          return result;
        }

        $scope.genericParameter = {
          key: {
            selected: [],
            suggestions: { 
              data: prepareSupportedGenericParameters() 
            }
          }
        };

        $scope.control = {};
        
        $scope.addGenericParameter = function() {
          if ($scope.genericParameter.key.selectedId && $scope.genericParameter.value) {
            $scope.genericParameters[$scope.genericParameter.key.selectedId] = $scope.genericParameter.value;
            $scope.genericParameter.key.control.reset();
            $scope.genericParameter.value = '';
          }
        };

        $scope.removeGenericParameter = function(key) {
          delete $scope.genericParameters[key];
        };

        $scope.control.reset = function() {
          $scope.genericParameter.key.control.reset();
          $scope.genericParameter.value = '';
          $scope.genericParameters = {};
        };
      }

      return {
        restrict: 'E',
        templateUrl: 'build-configs/directives/pnc-generic-parameters/pnc-generic-parameters.html',
        scope: {
          genericParameters: '=',
          control: '='
        },
        controller: PncGenericParameters
      };
    }
  ]);

})();
