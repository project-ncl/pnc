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

(function () {

  var module = angular.module('pnc.common.cache');

  /**
   * @author Jakub Senko
   */
  module.factory('PncCacheUtil', [
    'PncCache',
    'PageFactory',
    function (PncCache, PageFactory) {

      var util = {};

      /**
       * Attempts to create a string key from a (POJO) object.
       * Takes all first level (i.e. ignores prototype) properties, that are of type
       * string, number or boolean; and pairs them with the corresponding keys in an alphabetical order.
       */
      var _generateKey = function(obj) {
        var res = '';
        //var keys = _(obj).keys();
        _.chain(obj).keys().filter(function(key) {
          return _.isString(obj[key]) || _.isNumber(obj[key]) || _.isBoolean(obj[key]);
        }).sortBy(_.identity).each(function(key) {
          res = res + key + ':' + obj[key] + ';'; // could use foldl
        }).value();
        return res;
      };

      /**
       * Takes a DAO method and replaces it with a new method that caches the result after calling the original.
       * If the method returns a list each object is also cached into an index by id property!
       * Cache keys are computed using _generateKey on the argument object.
       *
       * @param resource resource object that contains the DAO method.
       *   The DAO method MUST RETURN THE SAME RESOURCE TYPE.
       *   Do not pass ProjectDAO when the method returns a Product, for example.
       * @param resourceName string representing the resource name. It is used as a cache key,
       *   so it MUST BE consistent across the application,
       *   especially the entity update event 'payload.entityClass'.
       *
       * Following are the cache paths used:
       *   /resource/{resourceName}/{methodName}/{_generateKey(args)}
       *   /resource/{resourceName}/index/{fieldName, currently 'id' only}/{id}
       */
      util.decorate = function(resource, resourceName, methodName) {
        if(methodName === 'index') {
          throw new Error('Illegal DAO method name, \'index\' is reserved for special purpose.');
        }
        var origMethod = resource[methodName];
        resource[methodName] = function (args) {
          var argsString = _generateKey(args);
          return PncCache.key('resource').key(resourceName).key(methodName).key(argsString).getOrSet(function() {
            // original call
            return origMethod(args).$promise.then(function(r) {
              // also save results to the ID index (if loaded from REST)
              if(PageFactory.verifyPageFormat(r) && r.totalPages > 0) {
                _(r.content).each(function(e) {
                  if(_(e).has('id')) {
                    /*jshint newcap:false*/
                    e = new resource(e);
                    PncCache.key('resource').key(resourceName).key('index').key('id').key('' + e.id).set(e);
                  }
                });
              }
              return r;
            });
          });
        };
      };


      /**
       * Decorate DAO method that returns entities BY ID. That means, that the methods argument object
       * contains the value of the unique ID field. It differs from the util.decorate method
       * because it can use the cached ID index that the previous method contributes to.
       * This often saves a HTTP call, because entities are often listed
       * before their detail page is displayed.
       *
       * @param idFieldName is optional and may solve a badly designed API that requires other args in
       *   addition to the ID even if the ID is unique.
       *   Example: To get a ProductVersion, you must also provide productId.
       */
      util.decorateIndexId = function(resource, resourceName, methodName, idFieldName) {
        if(methodName === 'index') {
          throw new Error('Illegal method name, \'index\' is reserved for special purpose.');
        }
        //resource = $injector.get(resourceName + 'DAO');
        var origMethod = resource[methodName];
        resource[methodName] = function (args) {
          var keys = _(args).keys();
          var key = _.isString(idFieldName) ? idFieldName : keys[0];
          if(_(args).has(key)) {
            var id = args[key];
            return PncCache.key('resource').key(resourceName).key('index').key('id').key('' + id).getOrSet(function() {
              return origMethod(args).$promise;
            });
          } else {
            throw new Error('Method \'' + methodName + '\' for resource \'' + resourceName + '\'' +
              'is expected to have a single argument (the id), and name provided if there is more than one.' +
              'Given ' + JSON.stringify(args) + ' and field name is ' + idFieldName + '.');
          }
        };
      };


      /**
       * Cache is kept fresh by receiving notification about entity updates.
       * This methods evicts the cache entries according to the received event.
       */
      util.processEntityUpdateEvent = function(payload) {
        if(_(['id', 'entityClass', 'operationType']).some(function(e) { return !_(payload).has(e); })) {
          throw new Error('Payload (' + JSON.stringify(payload) + ') is not in a correct format.');
        }
        /*
         * Must delete all pages related to the resource because the content may shift in case of CREATE and DELETE,
         * and change in case of UPDATE (filtering no longer valid).
         * However, only delete the entity from indexes in case of UPDATE and DELETE.
         * Improvements are possible, but probably not worth the effort.
         */
        var resourceKey = PncCache.key('resource').key(payload.entityClass);
        _(resourceKey.getKeyArray()).each(function(key) {
          if(key === 'index') {
            if (payload.operationType === 'UPDATE' || payload.operationType === 'DELETE') {
              resourceKey.key(key).key('id').key('' + payload.id).deleteKey();
            }
          } else {
            resourceKey.key(key).deleteKey();
          }
        });
      };

      return util;
    }
  ]);
})();
