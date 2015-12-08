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
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('ProjectDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'PROJECT_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function($resource, $injector, REST_BASE_URL, PROJECT_ENDPOINT, PageFactory, qh, PncCacheUtil) {
      var ENDPOINT = REST_BASE_URL + PROJECT_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        projectId: '@id'
      },{
        update: {
          method: 'PUT'
        },
        _getAll: {
          method: 'GET',
          url: ENDPOINT + qh.searchOnly(['name', 'description'])
        }
      });


      PncCacheUtil.decorateIndexId(resource, 'Project', 'get');

      PncCacheUtil.decorate(resource, 'Project', '_getAll');

      PageFactory.decorateNonPaged(resource, '_getAll', 'getAll');

      PageFactory.decorate(resource, '_getAll', 'getAllPaged');

      resource.prototype.getBCs = function() {
        return $injector.get('BuildConfigurationDAO').getByProject({ projectId: this.id });
      };

      return resource;
    }
  ]);

})();
