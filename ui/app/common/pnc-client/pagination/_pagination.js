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
       * Decorates the $resource service so we can transparently wrap paged REST
       * data in a page object. This must enhance and not break the expected
       * behaviour of using an ng-resource class.
       */
      $provide.decorator('$resource', function ($delegate) {
        return function (url, paramDefaults, actions, options) {
          var Resource;

          var pagedActions = [];

          // ng-resource defines a number of default actions (query, get, save) and
          // users can create more default actions. We look for these first as we'll
          // have to override them in order to decorate them.
          Object.keys($resourceProvider.defaults.actions).forEach(function (key) {
            if (!actions.hasOwnProperty(key)) {
              actions[key] = $resourceProvider.defaults.actions[key];
            }
          });

          // Now we add the resource specific actions to the list. And define an
          // an interceptor.
          Object.keys(actions).forEach(function (key) {
            var action = actions[key];
            var delegateInterceptor;

            if(action.isPaged) {
              pagedActions.push(key);

              if (action.interceptor && angular.isFunction(action.interceptor.response)) {
                delegateInterceptor = action.interceptor.response;
              }

              // For some reason the ng-resource actions don't resolve the promise with
              // the fetched data. We add an interceptor that does that, this allows us
              // to decorate the individual actions to wrap the REST response in a
              // page object.
              //
              // As only one interceptor can be defined we need to check if the user
              // has already provided one and, if so, call it before ours so as not
              // to break this functionality.
              action.interceptor = action.interceptor || {};
              action.interceptor.response = function (response) {
                if (delegateInterceptor) {
                  delegateInterceptor.apply(delegateInterceptor, arguments);
                }
                return response;
              };
            }
          });

          // Create the resource class so we can decorate it before transparently
          // returning it to the user.
          Resource = $delegate(url, paramDefaults, actions, options);

          // Decorate the paged action methods, so that any paged resources
          // are wrapped in a page object.
          pagedActions.forEach(function(action) {
            var delegate = Resource[action];

            Resource[action] = function () {
              var response = delegate.apply(delegate, arguments);

              var page = angular.injector(['pnc.common.pnc-client.pagination']).get('page');

              var p = page({
                Resource: Resource
              });

              p.$resolved = false;

              // Attach the promise to the "$promise" property of the page, to
              // match ng-resource's api.
              // When the request completes we fill the page object with the data,
              // again to match ng-resource's api so the user doesn't have
              // to unwrap the promise manually.
              p.$promise = response.$promise.then(function(response) {
                var content = response.data.content || [];

                // As our interceptor has "stolen" the data from ng-resource we
                // have to do its job for it of converting each of the items into
                // a resource class object.
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

              // Return the page, it'll be empty when the user gets it, matching
              // the ng-resource libraries functionality.
              return p;

            };
          });

          return Resource;
        };
      });
    }
  ]);

})();
