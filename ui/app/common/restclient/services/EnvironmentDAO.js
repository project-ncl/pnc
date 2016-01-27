/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('EnvironmentDAO', [
    '$resource',
    'REST_BASE_URL',
    'ENVIRONMENT_ENDPOINT',
    'PageFactory',
    function($resource, REST_BASE_URL, ENVIRONMENT_ENDPOINT, PageFactory) {
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

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorate(resource, '_getAll', 'getAll');

      return resource;
    }
  ]);

})();
