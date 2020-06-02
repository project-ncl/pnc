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

  module.value('USER_PATH', '/users/:id');

  module.factory('UserResource', [
    '$resource',
    'restConfig',
    'USER_PATH',
    function ($resource, restConfig, USER_PATH) {
      const ENDPOINT = restConfig.getPncRestUrl() + USER_PATH;

      var resource = $resource(ENDPOINT, {
        id: '@id'
      }, {
        // Method should not be called directly otherwise it will redirect not logged user 
        // when status code 401 is returned (see httpResponseInterceptor for more details), 
        // call authService#getPncUser() instead
        getAuthenticatedUser: {
          method: 'GET',
          url: restConfig.getPncRestUrl() + '/users/current',
          isArray: false,
          cache: true,
          successNotification: false
        }
      });

      return resource;
    }

  ]);

})();
