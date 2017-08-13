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

  /**
   * A collection of miscelanous helper functions.
   *
   * Developer Note: These should be pure functions that simply calculate an
   * output from their input without any side effects.
   */
  angular.module('pnc.common.util').factory('utils', [
    function () {

      /**
       * Returns true if the given value is undefined, null or an empty string.
       */
      function isEmpty(value) {
        return angular.isUndefined(value) || value === null ||
            (angular.isString(value) && value === '');
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
       function digestStrings(obj) {
         if (!angular.isObject(obj)) {
           return;
         }

         var digest = '';

         Object.keys(obj).forEach(function (key) {
           if (angular.isString(obj)) {
             digest = digest + key + obj[key];
           }
         });

         return digest;
       }

      return {
        isEmpty: isEmpty,
        parseBoolean: parseBoolean,
        digestStrings: digestStrings
      };
    }
  ]);

})();
