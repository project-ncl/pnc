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

  var module = angular.module('pnc.common.events', [
    'pnc.properties'
  ]);

  module.run([
    '$state',
    'pncNotifyUser',
    'messageBus',
    function($state, pncNotifyUser, messageBus) {

      function canonicalName(build) {
        return `${build.buildConfigRevision.name}#${build.id}`;
      }

      function buildLinkCallback(build) {
        return function() {
          $state.go('projects.detail.build-configs.detail.builds.detail.default', {
            projectId: build.project.id,
            configurationId: build.buildConfigRevision.id,
            buildId: build.id
          });
        };
      }


      /*
       * Default build notifications
       */

      messageBus.onBuildProgress('IN_PROGRESS', build => {
        console.log('BUILD STATUS: IN_PROGRESS, %O', build);
        if (build.groupBuild) {
          return;
        }
        pncNotifyUser(build.user).info(`Build ${canonicalName(build)} IN PROGRESS`,`Build #${build.id}`, buildLinkCallback(build));
      });

      messageBus.onBuildProgress('FINISHED', build => {
        console.log('BUILD STATUS: FINISHED, %O', build);

        if (build.groupBuild) {
          return;
        }

        const notify = pncNotifyUser(build.user);
        const linkText = `Build #${build.id}`;
        const callback = buildLinkCallback(build);

        switch (build.status) {
          case 'SUCCESS':
            notify.success(`Build ${canonicalName(build)} COMPLETED`, linkText, callback);
            break;
          case 'FAILED':
            notify.warn(`Build ${canonicalName(build)} FAILED`,  linkText, callback);
            break;
          case 'SYSTEM_ERROR':
            notify.error(`Build ${canonicalName(build)} completed with status SYSTEM_ERROR`,  linkText, callback);
            break;
          default:
            notify.info(`Build ${canonicalName(build)} completed with status ${build.status}`,  linkText, callback);
        }
      });

      /*
       * Default group build notifications
       */

      function groupBuildCanonicalName(groupBuild) {
        return `${groupBuild.groupConfig.name}#${groupBuild.id}`;
      }

      function groupBuildLinkCallback(groupBuild) {
        return () => $state.go('group-configs.detail', { groupConfigId: groupBuild.groupConfig.id });
      }


      messageBus.onGroupBuildProgress('IN_PROGRESS', groupBuild => {
        console.log('GROUP BUILD STATUS: IN_PROGRESS: %O', groupBuild);

        pncNotifyUser(groupBuild.user).info(`GroupBuild: ${groupBuildCanonicalName(groupBuild)} IN PROGRESS`, `GroupBuild #${groupBuild.id}`, groupBuildLinkCallback(groupBuild));
      });

      messageBus.onGroupBuildProgress('FINISHED', groupBuild => {
        console.log('GROUP BUILD STATUS: FINISHED: %O', groupBuild);

        const notify = pncNotifyUser(groupBuild.user);
        const canonicalName = groupBuildCanonicalName(groupBuild);
        const linkText = `GroupBuild #${groupBuild.id}`;
        const callback = groupBuildLinkCallback(groupBuild);

        switch(groupBuild.status) {
          case 'SUCCESS':
            notify.success(`GroupBuild ${canonicalName} completed successfully`, linkText, callback);
            break;
          case 'FAILED':
            notify.warn(`GroupBuild ${canonicalName} FAILED`, linkText, callback);
            break;
          case 'SYSTEM_ERROR':
            notify.error(`GroupBuild ${canonicalName} completed with status SYSTEM_ERROR`);
            break;
          default:
            notify.info(`GroupBuild ${canonicalName} completed with status ${groupBuild.status}`);
          }

        });

    }
  ]);

})();
