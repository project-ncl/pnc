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
  * @type function
  * @name pnc.common.pnc-client.pagination:page
  * @description
  * A factory function for producing page objects. A page object provides methods
  * for consuming a paginated RESTful api.
  *
  * @param {Object} spec - A specification object to create the page from.
  * @param {Array} spec.data - The contents of the page.
  * @param {Number} spec.index - The page index.
  * @param {Number} spec.size - The page size.
  * @param {Number} spec.total - The total number of pages.
  * @param {Object} spec.config - The $http config object used to retrieve this
  * resource. see: https://docs.angularjs.org/api/ng/service/$http#usage
  * @param {Object} spec.Resource [optional] - A resource "class" object created
  * using the $resource factory in ngResource. If one is provided then all
  * elements of a fetched paged will be passed to this constructor before
  * being added to the page.
  * see: https://docs.angularjs.org/api/ngResource/service/$resource
  * @returns {Object} - A page object
  * @author Alex Creasy
  */
  module.factory('page', [
    '$log',
    '$http',
    function ($log, $http) {

      var proto = {};

      /**
       * Factory function to create page objects.
       *
       */
       function page(spec) {
         spec = spec || {};

         var that = Object.create(proto, {
           _config: {
             get: function() {
               return angular.copy(spec.config);
             },
             set: function(config) {
               spec.config = angular.copy(config);
             }
           }
         });

         that.data = spec.data || [];
         that.index = spec.index || 0;
         that.size = spec.size || 0;
         that.total = spec.total || 1;
         that._Resource = spec.Resource;
         that.$promise = spec.promise;

         return that;
       }

     /**
      * Iterates over all items in the page, applying the
      * callback function to each item
      */
      proto.forEach = function (callback) {
        this.data.forEach(function () {
          callback.apply(this.data, arguments);
        }, this);
      };

     /**
      * Fetches a page from the same resource using the given paramaters. This
      * may be useful for filtering pages or otherwise supplying additional
      * querystring paramaters to this resource.
      *
      * @param {Object} params - Map of strings or objects which will be
      * serialized and appended to the http request as GET parameters.
      * @return {Promise} A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.fetch = function (params) {
        $log.debug('Fetching page: this=%O, params: %O', this, params);
        var Resource = this._Resource;
        var config = this._config;
        config.params = params;

        var promise = $http(config).then(function(response) {
          var p = page({
            index: response.data.pageIndex,
            size: response.data.pageSize,
            total: response.data.totalPages,
            data: response.data.content,
            Resource: Resource,
            config: response.config,
            $promise: promise
          });

          // If the resource class is present convert all the data
          // objects into resources.
          if (Resource) {
            for (var i = 0; i < p.data.length; i++) {
              p.data[i] = new Resource(p.data[i]);
            }
          }

          return p;
        });

        return promise;
      };

     /**
      * Check if a given page index exists.
      *
      * @return {Boolean} Returns true if the specified page index exists.
      * Otherwise returns false.
      */
      proto.has = function (index) {
        return index >= 0 && index < this.total;
      };

     /**
      * Fetches the page with the specified index.
      *
      * @param {Number} index - the page index to fetch.
      * @return {Promise} A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.get = function (index) {
        var params;
        if (!this.has(index)) {
          throw new RangeError('Requested page index out of bounds: ' + index);
        }

        params = this._config.params || {};
        params.pageIndex = index;
        return this.fetch(params);
      };

     /**
      * Check if there is another page after the current one.
      *
      * @return {Boolean} Returns true if the nexy page index exists.
      * Otherwise returns false.
      */
      proto.hasNext = function () {
        return this.has(this.index + 1);
      };

     /**
      * Check if there is another page before the current one.
      *
      * @return {Boolean} Returns true if the page index immediately prior to
      * the current one exists. Otherwise returns false.
      */
      proto.hasPrevious = function () {
        return this.has(this.index - 1);
      };

     /**
      * Fetches the first page.
      *
      * @return {Promise} A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.first = function () {
        return this.get(0);
      };

     /**
      * Fetches the last page.
      *
      * @return {Promise} A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.last = function () {
        return this.get(this.total - 1);
      };

     /**
      * Fetches the next page.
      *
      * @return {Promise} A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.next = function () {
        return this.get(this.index + 1);
      };

     /**
      * Fetches the previous page.
      *
      * @return {Promise} - A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.previous = function () {
        return this.get(this.index - 1);
      };

     /**
      * Refreshes the current page
      *
      * @return {Promise} - A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.refresh = function () {
        return this.get(this.index);
      };

     /**
      * Fetches the first page with the given new page size.
      *
      * @param {Number} size - the new page size.
      * @return {Promise} - A promise that will be resolved with a page object
      * representing the requested resource.
      */
      proto.getWithNewSize = function (size) {
        var params = this._config.params || {};
        params.pageSize = size;
        params.pageIndex = 0; // Reset index as it will be meaningless with new size.
        return this.fetch(params);
      };

      return page;
    }
  ]);

})();
