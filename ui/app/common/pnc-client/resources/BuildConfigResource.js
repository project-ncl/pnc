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

  const module = angular.module('pnc.common.pnc-client.resources');

  module.value('BUILD_CONFIG_PATH', '/build-configs/:id');


  module.factory('BuildConfigResource', [
    '$resource',
    'restConfig',
    'BUILD_CONFIG_PATH',
    ($resource, restConfig, BUILD_CONFIG_PATH) => {
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
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false
        }
      });

      return resource;
    }

  ]);

})();
