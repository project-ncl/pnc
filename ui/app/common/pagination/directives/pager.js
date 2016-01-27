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

  var module = angular.module('pnc.common.directives');

  /**
   * @author Jakub Senko
   */
  module.directive('pncPager', function () {
    return {
      restrict: 'E',
      scope: {
        page: '=',
        size: '='
      },
      templateUrl: 'common/pagination/directives/pager.html',
      link: function (scope) {
        var SIZE = 8;
        var size = _.isUndefined(scope.size) ? SIZE : scope.size;
        size = Math.floor(size / 2);
        var refresh = function () {
          var index = scope.page.getPageIndex();
          var count = scope.page.getPageCount();
          var left = Math.max(0, index - size);
          var right = Math.min(index + size + 1, count);
          scope.range = _.range(left, right);
        };
        scope.page.onUpdate(refresh);
        refresh();
      }
    };
  });

})();
