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

  var module = angular.module('pnc.common.restclient');

  module.value('PROJECT_ENDPOINT', '/projects/:projectId');

  /**
   * @ngdoc service
   * @name pnc.common.restclient:Project
   * @description
   *
   * @author Alex Creasy
   */
  module.factory('Project', [
    '$resource',
    'REST_BASE_URL',
    'PROJECT_ENDPOINT',
    function($resource, REST_BASE_URL, PROJECT_ENDPOINT) {
      var ENDPOINT = REST_BASE_URL + PROJECT_ENDPOINT;

      var Project = $resource(ENDPOINT, {
        projectId: '@id'
      },{
        update: {
          method: 'PUT',
        },
        getAllForProductVersion: {
          method: 'GET',
          url: REST_BASE_URL +
          '/projects/products/:productId/product-versions/:versionId',
          isArray: true,
        }
      });

      return Project;
    }
  ]);

})();
