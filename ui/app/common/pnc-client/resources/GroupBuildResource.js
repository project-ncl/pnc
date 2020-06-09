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

  module.value('GROUP_BUILD_PATH', '/group-builds/:id');

  /**
   * @author Martin Kelnar
   */
  module.factory('GroupBuildResource', [
    '$resource',
    'restConfig',
    'GROUP_BUILD_PATH',
    'GROUP_CONFIG_PATH',
    ($resource, restConfig, GROUP_BUILD_PATH, GROUP_CONFIG_PATH) => {
      const ENDPOINT = restConfig.getPncRestUrl() + GROUP_BUILD_PATH;
      const GROUP_CONFIGS_ENDPOINT = restConfig.getPncRestUrl() + GROUP_CONFIG_PATH;

      const resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        query: {
          method: 'GET',
          isPaged: true
        },
        cancel: {
          method: 'POST',
          url: ENDPOINT + '/cancel'
        },
        queryDependencyGraph: {
          method: 'GET',
          url: ENDPOINT + '/dependency-graph'
        },
        queryBuilds: {
          method: 'GET',
          url: ENDPOINT + '/builds',
          isPaged: true
        },
        removeBuild: {
          method: 'DELETE',
          successNotification: false
        },
        brewPush: {
          method: 'POST',
          url: ENDPOINT + '/brew-push',
          successNotification: false
        },
        queryByUser: {
          method: 'GET',
          isPaged: true,
          url: ENDPOINT + '/?q=user.id==:userId',
        },
        // NCL-5797 needs to add latest support
        getLatestByGroupConfig: {
          method: 'GET',
          url: GROUP_CONFIGS_ENDPOINT + '/group-builds',
          params: {
            latest: true 
          }
        }
      });


      /* 
       * canonicalName
       */
      function canonicalName(groupBuild) {
        return groupBuild.groupConfig.name + '#' + groupBuild.id;
      }
      resource.prototype.$canonicalName = function () {
        return canonicalName(this);
      };
      resource.canonicalName = canonicalName;

      /* 
       * isSuccess
       */
      function isSuccess(buildConfigSetRecord) {
        return buildConfigSetRecord.status === 'SUCCESS';
      }
      resource.prototype.$isSuccess = function () {
        return isSuccess(this);
      };
      resource.isSuccess = isSuccess;


      /* 
       * isCancelable
       */
      const CANCELABLE_STATUSES = [
        'NEW',
        'ENQUEUED',
        'WAITING_FOR_DEPENDENCIES',
        'BUILDING'
      ];
      function isCancelable(status) {
        return CANCELABLE_STATUSES.includes(status);
      }
      resource.prototype.$isCancelable = function () {
        return isCancelable(this.status);
      };

      return resource;
    }

  ]);

})();
