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

  const module = angular.module('pnc.common.pnc-client.resources');

  module.value('GROUP_CONFIG_PATH', '/group-configs/:id');

  /**
   *
   * @author Alex Creasy
   */
  module.factory('GroupConfigResource', [
    '$resource',
    'restConfig',
    'GROUP_CONFIG_PATH',
    'patchHelper',
    ($resource, restConfig, GROUP_CONFIG_PATH, patchHelper) => {
      const ENDPOINT = restConfig.getPncRestUrl() + GROUP_CONFIG_PATH;

      const resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        query: {
          method: 'GET',
          isPaged: true
        },
        save: {
          method: 'POST',
          successNotification: false
        },
        update: {
          method: 'PUT',
          successNotification: false
        },
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false
        },
        queryGroupBuilds: {
          method: 'GET',
          url: ENDPOINT + '/group-builds',
          isPaged: true
        },
        queryBuildConfigs: {
          method: 'GET',
          url: ENDPOINT + '/build-configs',
          isPaged: true
        },
        addBuildConfig: {
          method: 'POST',
          url: ENDPOINT + 'build-configs',
          successNotification: false
        },
        removeBuildConfig: {
          method: 'DELETE',
          url: ENDPOINT + '/build-configs/:buildConfigId',
          successNotification: false
        },
        queryBuilds: {
          method: 'GET',
          url: ENDPOINT + '/builds'
        },
        getLatestBuild: {
          method: 'GET',
          url: ENDPOINT + '/builds',
          params: {
            latest: true
          }
        }
      });

      patchHelper.assignPatchMethods(resource);

      resource.patchBuildConfigs = function (original, modified, groupConfigId) {
        let originalIds = {buildConfigs: {}}, modifiedIds = {buildConfigs: {}};

        for (const id of original.map(bc => bc.id)){
          originalIds.buildConfigs[id] = {id: id};
        }
        for (const id of modified.map(bc => bc.id)){
          modifiedIds.buildConfigs[id] = {id: id};
        }

        let patch = patchHelper.createJsonPatch(originalIds, modifiedIds, true);
        return resource.patch({id: groupConfigId}, patch).$promise;
      };

      resource.linkWithProductVersion = function (groupConfig, productVersion) {
        return resource.safePatch(groupConfig, { productVersion: { id: productVersion.id }}).$promise;
      };

      resource.unlinkFromProductVersion = function (groupConfig) {
        return resource.safePatch(groupConfig, { productVersion: null }).$promise;
      };

      return resource;
    }

  ]);

})();
