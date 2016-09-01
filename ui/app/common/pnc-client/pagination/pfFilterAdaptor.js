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

  var module = angular.module('pnc.common.pnc-client.pagination');

  /**
   * @ngdoc service
   * @type Function
   * @name pnc.common.pnc-client.pagination:pfFilterAdaptor
   * @description
   * Adaptor for connecting to a filteringPaginator to the PatternFly
   * pf-filter directive.
   * @author Alex Creasy
   */
  module.factory('pfFilterAdaptor', [
    function () {
      return function pfFilterAdaptor(paginator) {
        var that = {};

        that.onFilterChange = function (filters) {
          paginator.clearFilters();
          filters.forEach(function (filter) {
            paginator.addFilter({
              field: filter.id,
              value: filter.value
            });
          });
          paginator.apply();
        };

        that.resultsCount = function () {
          if (paginator.total === 1) {
            return paginator.data.length;
          }

          return paginator.total * paginator.size;
        };

        return that;
      };
    }
  ]);

})();
