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
(function () {
  'use strict';

  var module = angular.module('pnc.common.pnc-client.pagination', [
    'ngResource'
  ]);

  module.config([
    '$provide',
    '$resourceProvider',
    function ($provide, $resourceProvider) {

      /**
       * Decorate the $resource service to register pagination interceptors with
       * correct resource actions. This also allows us to grab the resource
       * constructor so we can pass it to the created page.
       */
      $provide.decorator('$resource', function ($delegate) {
        return function (url, paramDefaults, actions, options) {
          var Resource;

          var pagedActions = [];

          Object.keys($resourceProvider.defaults.actions).forEach(function (key) {
            if (!actions.hasOwnProperty(key)) {
              actions[key] = $resourceProvider.defaults.actions[key];
            }
          });

          Object.keys(actions).forEach(function (key) {
            var action = actions[key];
            var delegateInterceptor;

            if(action.isPaged) {
              pagedActions.push(key);

              // If the dev provided an interceptor save it so we can apply it after ours.
              if (action.interceptor && angular.isFunction(action.interceptor.response)) {
                delegateInterceptor = action.interceptor.response;
              }

              action.interceptor = action.interceptor || {};
              action.interceptor.response = function (response) {
                if (delegateInterceptor) {
                  delegateInterceptor.apply(delegateInterceptor, arguments);
                }
                return response;
              };
            }
          });

          Resource = $delegate(url, paramDefaults, actions, options);

          // Decorate the paged action methods
          pagedActions.forEach(function(action) {
            var delegate = Resource[action];

            Resource[action] = function () {
              var response = delegate.apply(delegate, arguments);

              var page = angular.injector(['pnc.common.pnc-client.pagination']).get('page');

              var p = page({
                Resource: Resource
              });

              p.$resolved = false;

              p.$promise = response.$promise.then(function(response) {
                var content = response.data.content || [];
                for (var i = 0; i < content.length; i++) {
                  content[i] = new Resource(content[i]);
                }

                p.index = response.data.pageIndex;
                p.size = response.data.pageSize;
                p.total = response.data.totalPages;
                p._config = response.config;
                p.data = content;
                p.$resolved = true;
              });

              return p;

            };
          });

          return Resource;
        };
      });
    }
  ]);

})();
