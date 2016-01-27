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

(function() {

  var module = angular.module('pnc.common.restclient');

  /**
   * @ngdoc service
   * @name pnc.common.restclient:cachedGetter
   * @description
   * A function for adding cached getters to ng-resource models. This
   * is useful when your model has a one-to-one or one-to-many relationship
   * with another ng-resource model.
   * @example
   * # Create a Person model.
   * ```js
      angular.module('myModule')
        .factory('Person', function($resource) {
          var Person = $resource('/person/:id');
          return Person;
        }
      );
   * ```
   * # Create a Dog model, with an additional method to get its owner.
   * ```js
      angular.module('myModule')
        .factory('Dog', function($resource, cachedGetter, Person) {
          var Dog = $resource('/dog/:id');

          // Add a method to the Dog prototype so all dogs have a method
          // to get the owner (a person object). The callback function is
          // invoked and passed the dog object that the method is being
          // invoked upon.
          Dog.prototype.getOwner = cachedGetter(function(dog) {
            return Person.get({ id: dog.ownerId });
          });
        }
      );
   * ```
   * # Add a controller.
   * ```js
      angular.module('myModule')
        .controller('MyController', function(Dog) {
          this.dogs = Dog.query();
        }
      );
   * ```
   * # Use in a view
   * ```html
      <div ng-controller="MyController as ctrl">
        <h1>Dogs</h1>
        <table>
          <thead>
            <th>Name</th>
            <th>Breed</th>
            <th>Owner's Name</th>
          </thead>
          <tbody>
            <tr ng-repeat="dog in ctrl.dogs">
              <td>{{ dog.name }}</td>
              <td>{{ dog.breed }}</td>
              <td>{{ dog.getOwner().name }}</td>
            </tr>
          </tbody>
        </table>
      </div>
   * ```
   * @author Alex Creasy
   */
  module.factory('cachedGetter', [
    '$http',
    function($http) {

      // name of the property under which we will hide all cached properties
      // this makes it easy to clear the cache.
      var cache = '_cachedGetterProperties_';

      // All properties in the cache are indexed by a unique key.
      var nextKey = 0;

      /*
       * We need to clean up the cache before sending an http request
       * otherwise we will send junk data back to the server along with
       * our object. We push a request transformer to the $http object
       * that will automatically look for the cache and remove it on each
       * request.
       */
      $http.defaults.transformRequest.unshift(
        function(value) {
          if(value && value.hasOwnProperty(cache)) {
            delete value[cache];
          }
          return value;
        }
      );

      return function(getter) {

        var key = nextKey++;

        return function() {
          // If the cache doesn't exist create it.
          if(!this[cache]) {
            this[cache] = {};
          }

          // Check if this object is cached and return it if we have.
          if(angular.isObject(this[cache][key])) {
            return this[cache][key];
          }

          this[cache][key] = getter(this);
          return this[cache][key];
        };

      };
    }
  ]);

})();
