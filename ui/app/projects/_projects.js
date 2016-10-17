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

  var module = angular.module('pnc.projects', [
    'ui.router',
    'pnc.common.restclient',
    'pnc.common.directives',
    'pnc.common.authentication',
    'angularUtils.directives.uiBreadcrumbs'
  ]);



  module.config([
    '$stateProvider',
    '$urlRouterProvider',
    function($stateProvider, $urlRouterProvider) {

      // NCL-2402 changed the project module URL, this redirect should
      // be removed at some point in the future.
      $urlRouterProvider.when('/project/:id', '/projects/:id');

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
        templateUrl: 'projects/views/projects.list.html',
        data: {
          displayName: 'Projects'
        },
        controller: 'ProjectListController',
        controllerAs: 'listCtrl',
        resolve: {
          projectList: function(ProjectDAO) {
            return ProjectDAO.getAll().$promise;
          }
        }
      });

      $stateProvider.state('projects.detail', {
        url: '/{projectId:int}',
        templateUrl: 'projects/views/projects.detail.html',
        data: {
           displayName: '{{ projectDetail.name }}',
        },
        controller: 'ProjectDetailController',
        controllerAs: 'detailCtrl',
        resolve: {
          projectDetail: function(ProjectDAO, $stateParams) {
            return ProjectDAO.get({
              projectId: $stateParams.projectId}).$promise;
          }
        }
      });

      $stateProvider.state('projects.detail.create-bc', {
        url: '/create-bc',
        templateUrl: 'projects/views/projects.detail.create-bc.html',
        data: {
            displayName: 'Create Build Config',
            requireAuth: true
          },
        controller: 'CreateBCController',
        controllerAs: 'ctrl',
        resolve: {
          projectDetail: function(ProjectDAO, $stateParams) {
            return ProjectDAO.get({
              projectId: $stateParams.projectId}).$promise;
          }
        }
      });

      $stateProvider.state('projects.create', {
        url: '/create',
        templateUrl: 'projects/views/projects.create.html',
        data: {
          displayName: 'Create Project',
          requireAuth: true
        },
        controller: 'ProjectCreateController',
        controllerAs: 'createCtrl'
      });
    }
  ]);

})();
