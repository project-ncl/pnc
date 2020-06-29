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

  var module = angular.module('pnc.report', [
    'ui.router',
    'ui.bootstrap',
    'pnc.common.directives',
    'infinite-scroll',
    'angularUtils.directives.dirPagination'
  ]);

  module.config(['$stateProvider', function ($stateProvider) {

    $stateProvider.state('report', {
      abstract: true,
	  views: {
	    'content@': {
	      templateUrl: 'common/templates/single-col.tmpl.html'
        }
      }
    });

    $stateProvider.state('report.blacklisted-artifacts-in-project', {
      url: '/blacklisted-artifacts-in-project',
      templateUrl: 'report/views/blacklisted.artifacts.in.project.html',
      data: {
        displayName: 'Show blacklisted artifacts in a project',
        title: 'Show blacklisted artifacts in a project | Report'
      },
      controller: 'BlacklistedArtifactsInProjectReportController',
      controllerAs: 'blacklistedArtifactsInProjectReportCtrl'
    });

    $stateProvider.state('report.built-artifacts-in-project', {
      url: '/built-artifacts-in-project',
      templateUrl: 'report/views/built.artifacts.in.project.html',
      data: {
        displayName: 'Show built artifacts in a project',
        title: 'Show built artifacts in a project | Report'
      },
      controller: 'BuiltArtifactsInProjectReportController',
      controllerAs: 'builtArtifactsInProjectReportCtrl'
    });

  }]);

})();
