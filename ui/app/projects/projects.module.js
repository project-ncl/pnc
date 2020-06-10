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

  var module = angular.module('pnc.projects', [
    'ui.router',
    'pnc.common.directives',
    'pnc.common.authentication'
  ]);

  module.config([
    '$stateProvider',
    function ($stateProvider) {

      $stateProvider.state('projects', {
        abstract: true,
        url: '/projects',
        views: {
          'content@': {
            templateUrl: 'common/templates/single-col.tmpl.html'
          }
        },
        data: {
          proxy: 'projects.list'
        }
      });

      $stateProvider.state('projects.list', {
        url: '',
        component: 'pncProjectsListPage',
        data: {
          displayName: 'Projects',
          title: 'Projects'
        },
        resolve: {
          projects: ['ProjectResource', 'SortHelper',
            (ProjectResource, SortHelper) => ProjectResource.query(SortHelper.getSortQueryString('projectsList')).$promise]
        }
      });

      $stateProvider.state('projects.detail', {
        url: '/{projectId}',
        component: 'pncProjectDetailPage',
        data: {
          displayName: '{{ project.name }}',
          title: '{{ project.name }} | Project'
        },
        resolve: {
          project: ['ProjectResource', '$stateParams', (ProjectResource, $stateParams) => ProjectResource.get({
            id: $stateParams.projectId
          }).$promise],
          buildConfigs: ['ProjectResource', '$stateParams', (ProjectResource, $stateParams) => ProjectResource.queryBuildConfigurations({
            id: $stateParams.projectId
          }).$promise]
        }
      });

      $stateProvider.state('projects.create', {
        url: '/create',
        component: 'pncProjectCreatePage',
        data: {
          displayName: 'Create Project',
          title: 'Create Project',
          requireAuth: true
        }
      });

    }
  ]);

})();
