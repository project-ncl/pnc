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

  var module = angular.module('pnc.common.pnc-client.resources');

  module.value('PRODUCT_VERSIONS_PATH', '/product-versions/:id');

  module.factory('ProductVersionResource', [
    '$resource',
    'restConfig',
    'PRODUCT_VERSIONS_PATH',
    'patchHelper',
    ($resource, restConfig, PRODUCT_VERSIONS_PATH, patchHelper) => {
      const ENDPOINT = restConfig.getPncRestUrl() + PRODUCT_VERSIONS_PATH;

      const resource = $resource(ENDPOINT, {
        id: '@id'
      },
      {
        save: {
          method: 'POST',
          successNotification: 'Product Version created'
        },
        update: {
          method: 'PUT',
          successNotification: 'Product Version updated'
        },
        queryBuildConfigs: {
          method: 'GET',
          url: ENDPOINT + '/build-configs',
          isPaged: true
        },
        removeBuildConfig: {
          method: 'DELETE',
          url: ENDPOINT + '/build-configs/:buildConfigId',
          successNotification: false
        },
        queryGroupConfigs: {
          method: 'GET',
          url: ENDPOINT + '/group-configs',
          isPaged: true
        },
        queryMilestones: {
          method: 'GET',
          url: ENDPOINT + '/milestones',
          isPaged: true
        },
        queryReleases: {
          method: 'GET',
          url: ENDPOINT + '/releases',
          isPaged: true
        }
      });

      patchHelper.assignPatchMethods(resource);

      return resource;
    }

  ]);

})();
