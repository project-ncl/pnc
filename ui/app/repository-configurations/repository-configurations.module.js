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

  var module = angular.module('pnc.repository-configurations', [
    'ui.router',
    'ui.bootstrap',
    'pnc.common.authentication',
    'pnc.common.pnc-client'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    
    $stateProvider.state('repository-configurations', {
      url: '/repository-configurations',
      abstract: true,
      views: {
        'content@': {
          templateUrl: 'common/templates/single-col.tmpl.html'
        }
      },
      data: {
        proxy: 'repository-configurations.list'
      }
    });

    $stateProvider.state('repository-configurations.list', {
      url: '',
      component: 'pncRepositoryConfigurationsListPage',
      data: {
        displayName: 'Repository Configurations'
      },
      resolve: {
        repositoryConfigurations: function(RepositoryConfiguration) {
          return RepositoryConfiguration.query().$promise;
        }
      }
    });

    $stateProvider.state('repository-configurations.detail', {
      url: '/{repositoryConfigurationId:int}',
      component: 'pncRepositoryConfigurationDetailPage',
      data: {
        displayName: 'Repository Configuration # {{ repositoryConfiguration.id }}'
      },
      resolve: {
        repositoryConfiguration: function(RepositoryConfiguration, $stateParams) {
          return RepositoryConfiguration.get({
            id: $stateParams.repositoryConfigurationId
          }).$promise;
        }
      }
    });

  }]);

})();
