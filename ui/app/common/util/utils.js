/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  /**
   * A collection of miscelanous helper functions.
   *
   * Developer Note: These should be pure functions that simply calculate an
   * output from their input without any side effects.
   */
  angular.module('pnc.common.util').factory('utils', [
    '$q',
    function ($q) {

      /**
       * Returns true if the given value is empty, that is one of the following criteria
       * holds:
       * - value is undefined
       * - value is null
       * - value is a string of length 0
       * - value is an array of length 0
       * - value is an object with no enumerable properties of its own (inherited properties are not checked)
       */
      function isEmpty(value) {
        if (typeof value === 'undefined') {
          return true;
        }

        if (value === null) {
          return true;
        }

        if ((typeof value === 'string' || value instanceof String) && value.length === 0) {
          return true;
        }

        if (Array.isArray(value) && value.length === 0) {
          return true;
        }

        if (typeof value === 'object' && value.constructor === Object && Object.keys(value).length === 0) {
          return true;
        }

        return false;
      }


      /**
       * Returns the inverse of isEmpty.
       */
      function isNotEmpty(value) {
        return !isEmpty(value);
      }

      /**
       * Used for parsing a boolean value from a component binding with type '@'.
       *
       * @param {string|boolean} value - the value to parse.
       * @return {boolean} the input parsed as a boolean.
       */
       function parseBoolean(value) {
         if (angular.isUndefined(value)) {
           return false;
         }

         if (typeof(value) === 'boolean') {
           return value;
         }

         if (angular.isString(value)) {
           return value.toLowerCase() === 'true';
         }

         throw new Error('Unable to parse as boolean: ' + value);
       }

       /**
        * Takes an object and forms a digest string by concatenating all string
        * properties and their keys. This is useful when setting up watches on
        * scopes so changes can be easily calculated.
        *
        * @param obj {object} the object to digest.
        * @return {string} the digest string.
        */
       function concatStrings(obj) {
         if (!angular.isObject(obj)) {
           return;
         }

        //  var digest = '';
         //
        //  Object.keys(obj).forEach(function (key) {
        //    if (angular.isString(obj[key])) {
        //      digest = digest + key + obj[key];
        //    }
        //  });
         //
        //  return digest;

        return Object.keys(obj).reduce(function (digest, key) {
          var item = obj[key];
          return angular.isString(item) ? digest + item : digest;
        }, '');
       }

       /**
        * Produces a hashcode for a given Array of items. The default behaviour
        * if given only an Array is to use the 'id' property of the object. If
        * no such property exists on any given object an error will be thrown.
        *
        * A function can be given as a second parameter which will be called for
        * each object in the array. The function will be passed the object and
        * is responsible for returning a representation of that object as a
        * number.
        *
        * @param {Array} array - The array to produce a hashcode for.
        * @param {Function} hashCodeFn - (Optional) function to produce a
        * Number representation of each object.
        * @return {Number} A hashcode of the array.
        */
       function hashCode(array, hashCodeFn) {
         var hash = angular.isFunction(hashCodeFn) ? hashCodeFn : function (obj) { return obj.id; };

         return array.reduce(function (total, item) {
           return 31 * total + hash(item);
         }, 17);
       }

       /**
        * Pretty prints an Object as a JSON string.
        *
        * @param {Object|Array} obj - The object to pretty prints
        * @param {Number} indent - The number of spaces to indent by: default 2.
        * @return {String} A pretty printed serialized form of the object.
        */
       function prettyPrint(obj, indent) {
         var _indent = indent || 2;

         JSON.stringify(obj, null, _indent);
       }

       /**
        * Takes a page object and fetches all objects of all pages and returns as a flat array.
        *
        * WARNING: This function should not be used except in exceptional circumstances.
        * It has the potential to cause severe performance issues on the backend and
        * using it should be seen as a code / UX smell. In some rare cases it is a pragmatic necessity
        * to use this to get around some shortcomings in the PNC REST API that are difficult to
        * address at present. The long term goal is to address the backend issues and delete this function
        * with extreme prejudice.
        *
        * @param {Object} page a page object
        * @return {Object} returns a promise with the
        */
       function dePaginate(page) {
         return $q.when(page)
            .then(page => {
              if (page.total === 1) {
                return page.data;
              } else {
                return page.getWithNewSize(page.total * page.count).then(resp => resp.data);
              }
            });
       }

      return {
        isEmpty,
        isNotEmpty,
        parseBoolean,
        concatStrings,
        hashCode,
        prettyPrint,
        dePaginate
      };
    }
  ]);

})();
