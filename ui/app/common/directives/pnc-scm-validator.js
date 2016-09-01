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

  var module = angular.module('pnc.common.directives');

  /**
   * @ngdoc directive
   * @name pnc.common.directives:pncScmValidator
   * @restrict A
   * @param {string} pnc-scm-validator
   * String including list of allowed protocols separated by "|"
   * @param {string} pnc-exact-host
   * Optional host restriction
   * @description
   * <protocol>(://)<host><path>.git<parameters>
   * Check wheter SCM field is valid. 
   * List of allowed protocols (separated by "|"):
   * - "git"   -> example git://github.com/user/project.git#v1.0
   * - "ssh"   -> example ssh://user@host.xz:port/path/to/repo.git/ or ssh://host.xz:port/path/to/repo.git/
   * - "http"  -> example http://github.com/user/project.git
   * - "https" -> example https://github.com/user/project.git
   * - "git+ssh" -> example git+ssh://github.com/user/project.git
   * - "git@"  -> example git@github.com:user/project.git
   * @example
   * <input pnc-scm-validator="git|ssh|http|https|git+ssh|git@" pnc-exact-host="user@host.xz:port" ... >
   * @author Martin Kelnar
   */
  module.directive('pncScmValidator', function () {

      var isScmUrl = function(allowedProtocols, exactHost, url) {
        allowedProtocols = allowedProtocols.replace('git@', 'git@[\\w\\.]+').replace('git+ssh', 'git\\+ssh');

        var pattern = new RegExp(
          '^(?:'+ allowedProtocols +')' +     // protocols
          ':(?:\\/\\/)?' +                    // protocol separator
          (exactHost ? exactHost + '[\\w\\.\\/~_-]+' : '[\\w\\.@:\\/~_-]+') + // repository
          '\\.git' +                          // suffix
          '(?:\\/?|\\#[\\d\\w\\.\\-_]+?)$');  // parameters
        return pattern.test(url);
      };

      return {
        restrict: 'A',
        require: 'ngModel',
        scope: {
          allowedProtocols : '@pncScmValidator',
          exactHost        : '@pncExactHost'
        },

        link: function(scope, ele, attrs, ctrl){
          ctrl.$validators.invalidScmUrl = function(value) {
            return !value || isScmUrl(scope.allowedProtocols, scope.exactHost, value);
          };

        }

      };
    }
  );

})();
