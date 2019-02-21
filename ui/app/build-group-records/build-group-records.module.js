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

  angular
    .module('pnc.build-group-records', [])
  
    .config([
      '$stateProvider',
      function ($stateProvider) {

        $stateProvider.state('build-group-records', {
          abstract: true,
          url: '/build-group-records',
          views: {
            'content@': {
              templateUrl: 'common/templates/single-col.tmpl.html'
            }
          },
          data: {
            proxy: 'build-group-records.list'
          }
        });


        $stateProvider.state('build-group-records.list', {
          url: '',
          data: {
            displayName: 'Build Group Records',
            title: 'Build Group Records'
          },
          component: 'pncBuildGroupRecordsListPage',
          resolve: {
            buildGroupRecords: [
              'BuildConfigSetRecord',
              function (BuildConfigSetRecord) {
                return BuildConfigSetRecord.query().$promise;
              }
            ]
          }
        });


        $stateProvider.state('build-group-records.detail', {
          url: '/{id:int}?visualization',
          data: {
            displayName: '{{ buildGroupRecord.buildConfigurationSetName }} » #{{ buildGroupRecord.id }}',
            title: '#{{ buildGroupRecord.id }} {{ buildGroupRecord.buildConfigurationSetName }} | Build Group Record'
          },
          params: {
            visualization: {
              value: 'list',
              dynamic: true
            }
          },
          component: 'pncBuildGroupRecordDetailPage',
          resolve: {
            buildGroupRecord: [
              'BuildConfigSetRecord', 
              '$stateParams', 
              function (BuildConfigSetRecord, $stateParams) {
                return BuildConfigSetRecord.get({ id: $stateParams.id }).$promise;
              }
            ],
            dependencyGraph: [
              'BuildConfigSetRecord', 
              '$stateParams', 
              function (BuildConfigSetRecord, $stateParams) {
                return BuildConfigSetRecord.getDependencyGraph({ id: $stateParams.id }).$promise;
              }
            ],
            buildRecords: [
              'dependencyGraph',
              'BuildRecord',
              function (dependencyGraph, BuildRecord) {
                return Object.keys(dependencyGraph.vertices).map(function (name) {
                  return new BuildRecord(dependencyGraph.vertices[name].data);
                });
              }
            ]
          }
        });
      }]);

})();
