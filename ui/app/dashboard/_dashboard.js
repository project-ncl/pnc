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
'use strict';

(function() {

  var module = angular.module('pnc.Dashboard', [
    'ui.router',
    'patternfly.notification',
    'angularUtils.directives.uiBreadcrumbs'
  ]);

  module.config(['$stateProvider', function($stateProvider) {
    $stateProvider.state('dashboard', {
      url: '/',
      views: {
        'content@': {
          templateUrl: 'dashboard/views/dashboard.html',
          controller: 'DashboardController'
        }
      },
      data: {
        displayName: 'Dashboard'
      },
      resolve: {
        restClient: 'PncRestClient',
        productList: function(restClient) {
          return restClient.Product.query().$promise;
        },
        projectList: function(restClient) {
          return restClient.Project.query().$promise;
        }
      }
    });
  }]);

})();
