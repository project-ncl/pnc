/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.artifacts').component('pncArtifactsDataTable', {
    bindings: {
     page: '<',
    },
    templateUrl: 'artifacts/list/pnc-artifacts-data-table.html',
    controller: [
      'filteringPaginator',
      'pfFilterAdaptor', 
      Controller
    ]
  });


  function Controller(filteringPaginator, pfFilterAdaptor) {
    const $ctrl = this;

    // -- Controller API --

   

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.paginator = filteringPaginator($ctrl.page);
      $ctrl.adaptor = pfFilterAdaptor($ctrl.paginator);
  
      $ctrl.toolbarConfig = {
        isTableView: true,
        filterConfig: {
          fields: [
            {
              id: 'identifier',
              title: 'Identifier',
              placeholder: 'Filter by Identifier',
              filterType: 'text'
            },
            {
              id: 'filename',
              title:  'Filename',
              placeholder: 'Filter by Filename',
              filterType: 'text'
            },
            {
              id: 'targetRepository.repositoryType',
              title: 'Repo Type',
              placeholder: 'Filter by Repo Type',
              filterType: 'select',
              filterValues: [
                'MAVEN',
                'GENERIC_PROXY',
                'NPM',
                'COCOA_POD'
              ]
            },
            {
              id: 'artifactQuality',
              title: 'Quality',
              placeholder: ' Filter by artifact quality',
              filterType: 'select',
              filterValues: [
                'NEW',
                'VERIFIED',
                'TESTED',
                'DEPRECATED',
                'BLACKLISTED',
                'TEMPORARY'
              ]
            },
            {
              id: 'md5',
              title:  'md5',
              placeholder: 'Filter by md5 checksum',
              filterType: 'text'
            },
            {
              id: 'sha1',
              title:  'sha1',
              placeholder: 'Filter by sha1 checksum',
              filterType: 'text'
            },
            {
              id: 'sha256',
              title:  'sha256',
              placeholder: 'Filter by sha256 checksum',
              filterType: 'text'
            }
          ],
          showTotalCountResults: false,
          appliedFilters: [],
          onFilterChange: $ctrl.adaptor.onFilterChange
        }
      };
    };

  }

})();
