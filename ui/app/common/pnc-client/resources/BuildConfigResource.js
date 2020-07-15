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

  module.value('BUILD_CONFIG_PATH', '/build-configs/:id');


  module.factory('BuildConfigResource', [
    '$log',
    '$resource',
    '$http',
    'restConfig',
    'patchHelper',
    'BUILD_CONFIG_PATH',
    'BuildResource',
    ($log, $resource, $http, restConfig, patchHelper, BUILD_CONFIG_PATH, BuildResource) => {
      const ENDPOINT = restConfig.getPncRestUrl() + BUILD_CONFIG_PATH;

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
        delete: {
          method: 'DELETE',
          successNotification: 'Build Config successfully deleted'
        },
        clone: {
          method: 'POST',
          url: ENDPOINT + '/clone',
          successNotification: 'Build Config successfully cloned'
        },
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false
        },
        getBuilds: {
          method: 'GET',
          url: ENDPOINT + '/builds',
          isPaged: true
        },
        getLatestBuild: {
          method: 'GET',
          url: ENDPOINT + '/builds',
          params: {
            latest: true
          },
          interceptor: {
            // Extract the single build object from the page object the REST API returns.
            response: resp => new BuildResource(resp.resource.content[0])
          }
        },
        getDependencies: {
          method: 'GET',
          url: ENDPOINT + '/dependencies',
          isPaged: true
        },
        getGroupConfigs: {
          method: 'GET',
          url: ENDPOINT + '/group-configs',
          isPaged: true
        },
        getDependants: {
          method: 'GET',
          url: ENDPOINT + '/dependants',
          isPaged: true
        },
        getRevisions: {
          method: 'GET',
          url: ENDPOINT + '/revisions',
          isPaged: true
        },
        getRevision: {
          method: 'GET',
          url: ENDPOINT + '/revisions/:revisionId'
        },
        restoreRevision: {
          method: 'POST',
          url: ENDPOINT + '/revisions/:revisionId/restore',
          successNotification: false
        }
      });

      resource.getAlignmentParameters = function(buildType) {
        return $http.get(restConfig.getPncRestUrl() + '/build-configs/default-alignment-parameters/' + buildType).then(function (r) {
          return r.data.parameters;
        });
      };

      resource.safePatchRemovingParameters = function (original, modified) {
        let patch = patchHelper.createJsonPatch(original, modified, false);
        patch = updatePatchRemovedParameters(original, modified, patch);
        return resource.patch({ id: original.id }, patch);
      };


      resource.getSupportedGenericParameters = function() {
        return $http.get(restConfig.getPncRestUrl() + '/build-configs/supported-parameters').then(function (r) {
          return r.data;
        });
      };

      patchHelper.assignPatchMethods(resource);

      function updatePatchRemovedParameters(original, modified, patch){
        for(let key in original.parameters) {
          if (!(key in modified.parameters)){
            let op = {op: 'remove'};
            op.path = '/parameters/' + key;
            patch.push(op);
          }
        }
        $log.debug('updatePatchRemovedParameters -> patch: %O', patch);

        return patch;
      }

      return resource;
    }

  ]);

})();
