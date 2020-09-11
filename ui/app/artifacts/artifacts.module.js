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
    .module('pnc.artifacts', [])

    .config([
      '$stateProvider',
      function ($stateProvider) {

        $stateProvider.state('artifacts', {
          abstract: true,
          url: '/artifacts',
          views: {
            'content@': {
              templateUrl: 'common/templates/single-col.tmpl.html'
            }
          },
          data: {
            proxy: 'artifacts.list'
          }
        });


        $stateProvider.state('artifacts.list', {
          url: '',
          data: {
            displayName: 'Artifacts',
            title: 'Artifacts'
          },
          component: 'pncArtifactsListPage',
          resolve: {
            artifacts: [
              'ArtifactResource', 'SortHelper',
              function (ArtifactResource, SortHelper) {
                return ArtifactResource.query(SortHelper.getSortQueryString('artifactsList')).$promise;
              }
            ]
          }
        });

        $stateProvider.state('artifacts.detail', {
          url: '/{id}',
          data: {
            displayName: '{{ artifact.identifier }}',
            title: '{{ artifact.identifier }} | Artifacts'
          },
          component: 'pncArtifactsDetailPage',
          resolve: {
            artifact: [
              '$stateParams',
              'ArtifactResource',
              function ($stateParams, ArtifactResource) {
                return ArtifactResource.get({ id: $stateParams.id }).$promise;
              }
            ],
            build: [
              'artifact',
              'BuildResource',
              function(artifact, BuildResource) {
                if (!artifact.build) {
                  // build is null when given artifact was not built by PNC but it was just a dependency
                  return null;
                }
                return BuildResource.get({ id: artifact.build.id }).$promise;
              }
            ],
            usages: [
              '$stateParams',
              'BuildResource',
              function ($stateParams, BuildResource) {
                return BuildResource.getDependantBuilds({
                  id: $stateParams.id,
                  pageSize: 10
                }).$promise;
              },
            ],
            revisions: [
              'artifact',
              'ArtifactResource',
              (artifact, ArtifactResource) => ArtifactResource.queryRevisions({ id: artifact.id, pageSize: 10 }).$promise
            ]
          }
        });

      }]);

})();
