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

(function () {

  var module = angular.module('pnc.common.restclient');

  /**
   * @description Add paging & search functionality to DAO methods. See 'decorate' function to start.
   * // TODO implement page caching (when to empty? timeout?)
   * @author Jakub Senko
   */
  module.service('PageFactory', [
    function () {
      /*jshint newcap:false*/
      var factory = this;

      /**
       * Create a new page from resource.
       * @return the page (respectively, dynamic page, not a single one).
       * Not returning a promise on purpose.
       * Users can implement loading spinners by checking isLoaded property.
       * Also, instead of the promise.then, onUpdate callbacks may be registered immediately.
       */
      factory.build = function (resource, loader, initialIndex, pageSize) {

        var DEFAULT_INDEX = 0;
        var DEFAULT_PAGE_SIZE = 10;
        if (_.isUndefined(initialIndex)) {
          initialIndex = DEFAULT_INDEX;
        }
        if (_.isUndefined(pageSize)) {
          pageSize = DEFAULT_PAGE_SIZE;
        }

        var EMPTY_DATA = {
          pageIndex: 0,
          pageSize: pageSize,
          totalPages: 0,
          content: []
        };

        // initial page state, before it is loaded
        var page = {
          _rawData: EMPTY_DATA,
          data: [],
          _onUpdate: [],
          isLoaded: false,
          _searchText: ''
        };

        /**
         * @private use 'reload' to make a request for the current page again
         * Reload the page with new data using loader function.
         * @return the page as a promise
         */
        page._refresh = function (index, pageSize, searchText, add) {
          add = typeof add !== 'undefined' ? add : false; // default parameter 

          page.isLoaded = false;
          return loader(index, pageSize, searchText).then(function (data) {
            if (!factory.verifyPageFormat(data)) {
              console.log('Warning! Data \'' + JSON.stringify(data) + '\' does not have correct format ' +
                '(not a paged resource). Using empty page instead.');
              data = factory._getEmptyPage();
            }
            if (data.totalPages === 0) {
              page._rawData = EMPTY_DATA;
              page.data = [];
            } else {
              page._rawData = data;
              var newData = factory._convertToResource(data.content, resource);
              page.data = add ? page.data.concat(newData) : newData;
            }
            page._searchText = searchText;
            _(page._onUpdate).each(function (callback) {
              callback(page);
            });
            page.isLoaded = true;
            return page;
          });
        };

        page.getPageIndex = function () {
          return page._rawData.pageIndex;
        };

        page.getPageSize = function () {
          return page._rawData.pageSize;
        };

        page.getPageCount = function () {
          return page._rawData.totalPages;
        };

        page.hasPageIndex = function (index) {
          return 0 <= index && index < page.getPageCount();
        };

        /**
         * Returns a promise of the page or throws an Error when index out of bounds.
         */
        page.loadPageIndex = function (index, add) {
          add = typeof add !== 'undefined' ? add : false; // default parameter 

          if (page.hasPageIndex(index)) {
            return page._refresh(index, page.getPageSize(), page._searchText, add);
          } else {
            throw 'Error: Invalid page index ' + index +
            '. Must be between 0 inclusive and ' + page.getPageCount() + ' exclusive.';
          }
        };

        page.hasNext = function () {
          return page.hasPageIndex(page.getPageIndex() + 1);
        };

        /**
         * Returns a promise of the page or throws an Error when index out of bounds.
         */
        page.next = function () {
          return page.loadPageIndex(page.getPageIndex() + 1);
        };

        /** 
         * New data are added to existing
         */
        page.loadMore = function () {
          if (page.hasNext()) {
            return page.loadPageIndex(page.getPageIndex() + 1, true);
          }
        };

        page.hasPrevious = function () {
          return page.hasPageIndex(page.getPageIndex() - 1);
        };

        /**
         * Returns a promise of the page or throws an Error when index out of bounds.
         */
        page.previous = function () {
          return page.loadPageIndex(page.getPageIndex() - 1);
        };

        page.onUpdate = function (callback) {
          page._onUpdate.push(callback);
        };

        page.search = function (searchText) {
          return page._refresh(DEFAULT_INDEX, pageSize, searchText);
        };

        page.reload = function () {
          return page._refresh(page.getPageIndex(), page.getPageSize(), page._searchText);
        };

        page.last = function () {
          return page.loadPageIndex(page.getPageCount() - 1);
        };

        page.first = function () {
          return page.loadPageIndex(0);
        };

        page._refresh(initialIndex, pageSize, '');

        return page;
      };

      /**
       * REST endpoints return data in pages in a specific JSON format.
       * To take advantage of this and provide simple paging & search functionality,
       * the DAO methods can be decorated to return wrapper 'page' objects that have methods
       * to load next and previous pages, and perform a fulltext search on  specified fields.
       * Actual contents can be accessed by 'data' field of the page, and callbacks can be provided
       * to be executed when page data changes. This is a convenience method and provides a default 'loader'
       * function to the general 'build' function that should be sufficient.
       * However it has some drawbacks, such as the need to specify 'search' url param
       * using QueryHelper when it is implemented using RSQL and not in backend.
       *
       * @param resource that contains the wrapped method
       * @param methodName name of the wrapped method as a string. The method MUST return data in the
       * 'page' JSON format, so only methods that return collections can be wrapped.
       * However because the collection (array) is inside the outer object, isArray setting on the resource method
       * must be false.
       * @param newMethodName decorated function name
       */
      factory.decorate = function (resource, methodName, newMethodName) {
        var origMethod = resource[methodName];
        resource[newMethodName] = function (origArgs) {
          return factory.build(resource, function (pageIndex, pageSize, searchText) {
            var args = _({
              pageIndex: pageIndex,
              pageSize: pageSize,
              search: searchText, // search must be done in backend, either via RSQL or directly
              sort: 'sort=desc=id' // if the data are not sorted, pagination makes no sense
            }).extend(origArgs); // args overwrite the paging properties, but no sense other that sorting
            return origMethod(args).$promise;
          });
        };
      };

      /**
       * Create a new method that returns all data from all pages as a single array.
       * Given that the DAO methods return data in pages
       * of predetermined size, its impossible to always get all data
       * by guessing a large number for the page size.
       * This method decorates a DAO method to automatically make a second request
       * to get all the data if needed. Data is also automatically unwrapped, as though it
       * was not paged. The original method must return data in the paged format, obviously.
       */
      factory.decorateNonPaged = function (resource, methodName, newMethodName) {
        var origMethod = resource[methodName];
        resource[newMethodName] = function (origArgs) {
          origArgs = origArgs || {};
          var args = _(origArgs).extend({
            pageIndex: 0,
            pageSize: 50 // sufficiently large number to prevent to many second calls
            // if this number should be much larger, refactoring the code in question
            // would be the best option
          });
          var result = origMethod(args);
          if (_(result).has('$promise')) {
            return result.$promise.then(function (data) {
              return factory._maybeTryAgain(data, origMethod, args);
            }).then(function (data) {
              return factory._convertToResource(data.content, resource);
            });
          } else {
            throw 'Error. Result of call to \'' + methodName + '\' does not have $promise property. ' +
            'Make sure that the method belongs to the standard angular resource.';
          }
        };
      };

      /**
       * Given an object verify that it has the correct format to be used
       * in page factory.
       * @returns true if correct
       */
      factory.verifyPageFormat = function (obj) {
        // {"pageIndex": ...,"pageSize": ...,"totalPages": ...,"content": [...]}
        if (_(obj).has('totalPages')) {
          if (obj.totalPages === 0) {
            // pageIndex, pageSize, content not required in this case
            if (_(obj).has('pageIndex') && obj.pageSize < 0) {
              return false;
            }
            if (_(obj).has('pageSize') && obj.pageSize < 1) {
              return false;
            }
            if (_(obj).has('content') && (!_.isArray(obj.content) || obj.content.length <= 0)) {
              return false;
            }
            return true;
          } else {
            return _(obj).has('pageIndex') &&
              obj.pageIndex >= 0 &&
              _(obj).has('pageSize') &&
              obj.pageSize > 0 &&
              _(obj).has('content') &&
              _.isArray(obj.content) &&
              obj.content.length > 0;
          }
        }
        return false;
      };

      factory._getEmptyPage = _.constant({
        totalPages: 0
      });


      factory._maybeTryAgain = function (data, origMethod, args) {
        if (!factory.verifyPageFormat(data)) {
          console.log('Warning! Data \'' + JSON.stringify(data) + '\' does not have correct format ' +
            '(not a paged resource). Using empty page instead.');
          return factory._getEmptyPage();
        }
        if (data.totalPages > 1) {
          // Must perform a second request to load all data. This should not happen too often.
          return origMethod(_(args).extend({
            pageIndex: 0,
            pageSize: data.pageSize * data.totalPages
          })).$promise;
        } else {
          return data;
        }
      };

      factory._convertToResource = function (data, resource) {
        // normally angular's job, but we are using data in paged format
        return _(data).map(function (e) {
          if (_(e).has('id')) {  // it might not be a resource, but a string for example
            return new resource(e);
          } else {
            return e;
          }
        });
      };
    }
  ]);
})();
