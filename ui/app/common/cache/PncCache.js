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
   * Simple multi-level/tree cache. Each node can store a value and/or have children.
   * Promise-aware. Probably the most useful function is getOrSet.
   * The cache is not persistent, i.e. stores data only within a single browser window instance.
   *
   * @example cache.key('foo').key('bar').key('baz')
   *   .getOrSet(function() { return http.get(...); }).then(function(value) {...});
   *
   * @author Jakub Senko
   */
  module.factory('PncCache', [
    '$q',
    '$log',
    function ($q, $log) {
      // variables starting with underscore are considered private

      /**
       * Decorate a base node object with methods that all nodes must have
       * (including the root, which does not have a parent and a stored value).
       * Does not include methods to actually ge/set a value (root cannot do that).
       */
      var _decorateBaseNode = function (node) {

        if(!_(node).has('_entries')) {
          throw new Error('All nodes MUST have an \'_entries\' (children) field.');
        }

        /**
         * Convert object given as a key to a string key.
         */
        var _convertKey = function (key) {
          if (_.isString(key)) {
            if(key.indexOf('/') === -1) {
              return key;
            }
            else {
              throw new Error('Cache key string MUST NOT contain \'/\'');
            }
          }
          else if (_(key).has('toStringKey') && _.isFunction(key.toStringKey) && key.toStringKey.length === 0) {
            key = key.toStringKey();
            if(_.isString(key)) {
              return key;
            } else {
              throw new Error('Cache key has a \'toStringKey\' method, but it MUST return a string instead of \'' + key + '\'.');
            }
          } else {
            throw new Error('Cache key object (not a string) MUST HAVE a zero-arity \'toStringKey\' method.');
          }
        };

        /**
         * Access child node corresponding to the given key.
         *
         * @param key MUST BE either a string not containing '/'
         * OR an object with a zero-arity 'toStringKey' method.
         */
        node.key = function (key) {
          key = _convertKey(key);
          var childNode = node._entries[key];
          if (_.isUndefined(childNode)) {
            childNode = _createNonRootNode(node, key); // automatically create the child node if does not exist
            node._entries[key] = childNode; // bind to the parent
          }
          return childNode;
        };

        /**
         * Get the path from root to the node as a single slash-delimited string.
         */
        var _getKeyPathStr = function (node) {
          if (_.isUndefined(node._key)) {
            return '';
          }
          return _getKeyPathStr(node._parent) + '/' + node._key;
        };

        node.getKeyPathStr = function () {
          return _getKeyPathStr(node);
        };


        node.clear = function () {
          node._entries = {};
        };

        node.getKeyArray = function () {
          return _(node._entries).keys();
        };
      };

      // returns a new root node, this is what is actually returned from the cache factory
      var _createRootNode = function() {

        // ROOT node
        var node = {
          _entries: {}
        };

        _decorateBaseNode(node);

        return node;
      };

      // Create node that can store a value
      var _createNonRootNode = function (parent, key) {

        // NON-ROOT node, has a parent and a string key, _value is left undefined util it is set
        var node = {
          _parent: parent,
          _key: key,
          _entries: {}
        };

        _decorateBaseNode(node); // every node is a base node

        node.isAbsent = function () {
          return _.isUndefined(node._value);
        };

        /**
         * Returns a promise that is resolved IFF the value is found.
         * and rejected when the value does not exist. Use $q::catch to test for error.
         */
        node.get = function () {
          return $q(function (resolve, reject) {
            if (node.isAbsent()) {
              reject(new Error('No value for key \'' + node.getKeyPathStr() + '\'.'));
            } else {
              resolve(node._value);
              $log.debug('Entry read from cache at ', node.getKeyPathStr());
            }
          });
        };

        // create a promise out of the given object (if it is an function, evaluate it first)
        var _getValue = function (provider) {
          if (_.isFunction(provider)) {
            provider = provider();
          }
          return $q.when(provider);
        };

        /**
         * Save the value to cache and returns a promise resolved with the same value
         * when the operation completes successfuly.
         *
         * @param provider can be a promise, function or other value.
         * If it's a promise, it's resolved first and then the value is cached and a promise is returned again.
         * If the original promise is rejected, so is the returned promise and nothing is cached.
         * If it is a function, it is called without arguments, result is cached and wrapped inside a promise.
         * If it is any other value, it is cached and wrapped inside a promise.
         * @return promise with the (successfuly) saved value.
         */
        node.set = function (provider) {
          return _getValue(provider).then(function (value) {
            node._value = value;
            $log.debug('Entry saved to cache at ', node.getKeyPathStr());
            return value;
          });
        };

        /**
         * Save the value if absent.
         * @see set
         * @return promise with the (successfuly) saved value.
         */
        node.getOrSet = function (provider) {
          return node.get().catch(function (error) {
            /* jshint unused: false */
            return node.set(provider);
          });
        };

        node.deleteValue = function () {
          delete node._value;
        };

        /**
         * Also deletes all children!!!
         */
        node.deleteKey = function () {
          $log.debug('Entry deleted from cache at ', node.getKeyPathStr());
          delete node._parent._entries[node._key];
        };

        return node;
      };

      return _createRootNode();
    }
  ]);
})();
