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

  var module = angular.module('pnc.scm-repositories', [
    'ui.router',
    'ui.bootstrap',
    'pnc.common.authentication',
    'pnc.common.pnc-client'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    
    $stateProvider.state('scm-repositories', {
      url: '/scm-repositories',
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'scm-repositories.list'
      }
    });

    $stateProvider.state('scm-repositories.list', {
      url: '',
      component: 'pncScmRepositoriesListPage',
      data: {
        displayName: 'SCM Repositories',
        title: 'SCM Repositories'
      },
      resolve: {
        scmRepositories: ['ScmRepositoryResource', function(ScmRepositoryResource) {
          return ScmRepositoryResource.query().$promise;
        }]
      }
    });

    $stateProvider.state('scm-repositories.detail', {
      url: '/{scmRepositoryId:int}',
      component: 'pncScmRepositoryDetailPage',
      data: {
        displayName: '{{ scmRepository.getName() }}',
        title: '{{ scmRepository.getName() }} | SCM Repository'
      },
      resolve: {
        scmRepository: ['ScmRepositoryResource', '$stateParams', function(ScmRepositoryResource, $stateParams) {
          return ScmRepositoryResource.get({
            id: $stateParams.scmRepositoryId
          }).$promise;
        }],
        buildConfigs: ['ScmRepositoryResource', '$stateParams', function(ScmRepositoryResource, $stateParams) {
          return ScmRepositoryResource.queryBuildConfigs({
            id: $stateParams.scmRepositoryId
          }).$promise;
        }],
      }
    });

    $stateProvider.state('scm-repositories.create', {
      url: '/create',
      component: 'pncScmRepositoryCreatePage',
      data: {
        displayName: 'Create SCM Repository',
        title: 'Create SCM Repository',
        requireAuth: true
      }
    });

  }]);

})();
