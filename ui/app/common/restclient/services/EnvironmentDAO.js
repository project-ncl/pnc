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

  module.value('ENVIRONMENT_ENDPOINT', '/environments/:environmentId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('EnvironmentDAO', [
    '$resource',
    'REST_BASE_URL',
    'ENVIRONMENT_ENDPOINT',
    'PageFactory',
    'PncCacheUtil',
    function($resource, REST_BASE_URL, ENVIRONMENT_ENDPOINT, PageFactory, PncCacheUtil) {
      var ENDPOINT = REST_BASE_URL + ENVIRONMENT_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        environmentId: '@id'
      },{
        _getAll: {
          method: 'GET'
        },
        update: {
          method: 'PUT'
        }
      });

      PncCacheUtil.decorateIndexId(resource, 'Environment', 'get');

      PncCacheUtil.decorate(resource, 'Environment', '_getAll');

      PageFactory.decorateNonPaged(resource, '_getAll', 'getAll');

      return resource;
    }
  ]);

})();
