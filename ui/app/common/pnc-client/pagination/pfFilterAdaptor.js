/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
   * @ngdoc service
   * @type Function
   * @name pnc.common.pnc-client.pagination:pfFilterAdaptor
   * @description
   * Adaptor for connecting a filteringPaginator to an angular-patternfly
   * pf-filter directive.
   * @author Alex Creasy
   */
  angular.module('pnc.common.pnc-client.pagination').factory('pfFilterAdaptor', [
    'PF_FILTER_TYPES',
    function (PF_FILTER_TYPES) {
      return function pfFilterAdaptor(paginator) {
        var that = {};

        that.onFilterChange = function (filters) {
          // This is a hack, on the below line "this.fields" references internal state within the pfFilter
          // object from the angular-patternfly lib. This is possible due to an oddity of javascript and how it
          // defines what "this" references in a given context. We're accessing private internal state of a 3rd party
          // library here so the usual warnings apply.
          //
          const filterDefinitions = this.fields;
          paginator.clearFilters();

          filters.forEach(filter => {
            const filterDef = filterDefinitions.find(fd => fd.id === filter.id);
            paginator.addFilter({
              field: filter.id,
              value: filter.value,
              comparator: PF_FILTER_TYPES[filterDef.filterType],
              method: filterDef.filterMethod || 'RSQL'
            });
          });
          paginator.apply();
        };


        that.onSortChange = function (field, asc) {
          /**
           * pfSort passes "isAscending" to the function, but "paginator.sortBy"
           * uses "desc" for api request.
           */
          paginator.sortBy(field, !asc);
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
