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
(function () {
  'use strict';

  /**
   * @ngdoc directive
   * @name pnc.common.directives:pncPageControls
   * @restrict EA
   * @description
   * @example
   * @author
   */
  angular.module('pnc.common.pnc-client.pagination').directive('pncPageControls', function () {

    var DEFAULT_TEMPLATE = 'common/pnc-client/pagination/directives/pnc-page-controls/pnc-page-controls.html';

    var PAGESIZE_OPTIONS = [10, 25, 50, 100, 200];

     return {
       restrict: 'EA',
       templateUrl: function(elem, attrs) {
         return attrs.pncTemplate || DEFAULT_TEMPLATE;
       },
       scope: {
         page: '=pncPage'
       },
       bindToController: true,
       controllerAs: 'ctrl',
       controller: function () {
         var self = this;

         function refresh() {
           self.index = self.page.index + 1;
           self.size = self.page.size;
           self.total = self.page.total;
         }

         self.pageSizes = PAGESIZE_OPTIONS;

         self.getPageIfExists = function (index) {
           if (self.page.has(index - 1)) {
              self.page.get(index - 1);
           }
         };

         self.page.onUpdate(function () {
            refresh();
         });

         refresh();
       }
     };
  });

})();
