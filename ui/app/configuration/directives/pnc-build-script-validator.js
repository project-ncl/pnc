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

  var module = angular.module('pnc.configuration');

  /**
   * Check whether Build Script input is valid.
   */
  module.directive('pncBuildScriptValidator', function () {

      var buildScriptChecker = function(value) {
        var MAVEN = 'mvn';
        var MANDATORY_ARGS = ['deploy'];

        var valueNormalized = value.toLowerCase();

        if (valueNormalized.indexOf(MAVEN) > -1) {
          var isValid = true;
          MANDATORY_ARGS.forEach(function(mandatoryArg) {
            if (valueNormalized.indexOf(mandatoryArg) === -1) {
              isValid = false;
              return;
            }
          });
          return isValid;
        }
        return true;
      };

      return {
        restrict: 'A',
        require: 'ngModel',

        link: function(scope, ele, attrs, ctrl){
          ctrl.$validators.invalidBuildScript = function(value) {
            return !value || buildScriptChecker(value);
          };

        }


      };
    }
  );

})();
