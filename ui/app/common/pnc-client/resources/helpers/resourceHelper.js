/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.common.pnc-client.resources').factory('resourceHelper', [
    function () {
  
      function normalize(maybeResource) {
        // maybeResource.toJSON() gives us a seperate copy of the resource that has no methods or 
        // private $prefixed properties that could harm the comparison.
        return isResource(maybeResource) ? maybeResource.toJSON() : maybeResource;
      }
  
      function isResource(obj) {
        return obj.hasOwnProperty('$promise') && obj.hasOwnProperty('$resolved') && angular.isFunction(obj.toJSON);
      }

      return Object.freeze({
        normalize,
        isResource
      });
    }

  ]);

})();
