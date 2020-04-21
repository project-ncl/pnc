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

  module.value('PRODUCT_RELEASES_PATH', '/product-releases/:id');

  module.factory('ProductReleaseResource', [
    '$resource',
    'restConfig',
    'PRODUCT_RELEASES_PATH',
    'patchHelper',
    ($resource, restConfig, PRODUCT_RELEASES_PATH, patchHelper) => {
      const ENDPOINT = restConfig.getPncRestUrl() + PRODUCT_RELEASES_PATH;

      const resource = $resource(ENDPOINT, {
        id: '@id'
      },
      {
        query: {
          method: 'GET',
          isPaged: true,
        },
        save: {
          method: 'POST',
          successNotification: 'Product Release created'
        },
        update: {
          method: 'PUT',
          successNotification: 'Product Release updated'
        },
        querySupportLevels: {
          method: 'GET',
          url: ENDPOINT + '/support-levels',
          isArray: true // the returned object is an array
        }
      });

      patchHelper.assignPatchMethods(resource);

      return resource;
    }

  ]);

})();
