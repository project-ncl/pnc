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

  var module = angular.module('pnc.common.pnc-client.pagination');

  /**
   * @ngdoc service
   * @name pnc.common.pnc-client.pagination:paginator
   * @description
   * Inherits the page interface, but stores an internal reference to the page
   * which is updated when new pages are requested rather than returned to the
   * user. This is useful when displaying paginated collections, the paginators
   * data property can be bound to the view and will be updated whenever
   * the page is changed.
   *
   * @author Alex Creasy
   */
  module.factory('paginator', [
    '$log',
    'page',
    function ($log, $page) {

      function paginator(page) {
        var delegate = page; //

        // The paginator inherits all of the page prototype methods,
        // making the paginator a page in itself.
        // The fetch method is overridden so that any actions
        // replace the proxied page object, rather than retuning it
        // to the user.
        var that = Object.create(Object.getPrototypeOf($page()), {
          // All of the paginators properties are proxied to
          // the delegate page. However, as no setters are defined
          // the properties cannot be altered.
          data: {
            get: function () {
              return delegate.data;
            },
          },
          index: {
            get: function () {
              return delegate.index;
            }
          },
          size: {
            get: function () {
              return delegate.size;
            }
          },
          total: {
            get: function () {
              return delegate.total;
            }
          },
          _config: {
            get: function () {
              return delegate._config;
            }
          },
          _Resource: {
            get: function () {
              return delegate._Resource;
            }
          }
        });

        that.isLoaded = false;

        /**
         * Overides prototype method.
         *
         * @returns {Promise} - A promise that is resolved with the calling
         * paginator instance (which implements the page protoype interface).
         */
        that.fetch = function (params) {
          that.isLoaded = false;

          // Retrieve a new page and assign it to be the new proxied instance.
          // Return a promise that's resolved with the paginator itself
          // rather than the retrieved promise. As the paginator inherits from
          // the page prototype this complies with the page interface contract
          // without exposing the underlying page. In reality the resolved
          // reference to the paginator is of little use, however the promise
          // is still useful for executing asynchronous callbacks against the
          // paginator.
          return delegate.fetch(params).then(function(response) {
            delegate = response;
            that.isLoaded = true;
          });
        };

        delegate.$promise.then(function () {
          that.isLoaded = true;
        });

        return that;
      }

      return paginator;
    }
  ]);

})();
