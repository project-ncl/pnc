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

  angular
    .module('pnc.group-builds', [])

    .config([
      '$stateProvider',
      function ($stateProvider) {

        $stateProvider.state('group-builds', {
          abstract: true,
          url: '/group-builds',
          views: {
            'content@': {
              templateUrl: 'common/templates/single-col.tmpl.html'
            }
          },
          data: {
            proxy: 'group-builds.list'
          }
        });


        $stateProvider.state('group-builds.list', {
          url: '',
          data: {
            displayName: 'Group Builds',
            title: 'Group Builds'
          },
          component: 'pncGroupBuildsListPage',
          resolve: {
            groupBuilds: [
              'GroupBuildResource', 'SortHelper',
              function (GroupBuildResource, SortHelper) {
                return GroupBuildResource.query(SortHelper.getSortQueryString('groupBuildsList')).$promise;
              }
            ]
          }
        });


        $stateProvider.state('group-builds.detail', {
          url: '/{id}?visualization',
          data: {
            displayName: '{{ groupBuild.groupConfig.name }} » #{{ groupBuild.id }}',
            title: '#{{ groupBuild.id }} {{ groupBuild.groupConfig.name }} | Group Build'
          },
          params: {
            visualization: {
              value: 'list',
              dynamic: true
            }
          },
          component: 'pncGroupBuildDetailPage',
          resolve: {
            groupBuild: [
              'GroupBuildResource',
              '$stateParams',
              function (GroupBuildResource, $stateParams) {
                return GroupBuildResource.get({ id: $stateParams.id }).$promise;
              }
            ],
            dependencyGraph: [
              'GroupBuildResource',
              '$stateParams',
              function (GroupBuildResource, $stateParams) {
                return GroupBuildResource.queryDependencyGraph({ id: $stateParams.id }).$promise;
              }
            ],
            builds: [
              'dependencyGraph',
              'BuildResource',
              function (dependencyGraph, BuildResource) {
                return Object.keys(dependencyGraph.vertices).map(function (name) {
                  return new BuildResource(dependencyGraph.vertices[name].data);
                });
              }
            ]
          }
        });
      }]);

})();
