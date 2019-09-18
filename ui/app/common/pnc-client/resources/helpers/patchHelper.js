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

  angular.module('pnc.common.pnc-client.resources').factory('patchHelper', [
    '$log',
    'resourceHelper',
    function ($log, resourceHelper) {

      const { normalize } = resourceHelper;

      function createJsonPatch(original, modified, destructive = false) {
        $log.debug('createJsonPatch -> destructive: %s | original: %O | modified: %O', destructive, original, modified);

        const left = normalize(original);
        const right = normalize(modified);

        let patch; 

        if (destructive) {
          patch = jsonpatch.compare(left, right);
        } else {
          patch = jsonpatch.compare(left, _.merge({}, left, right));
        }

        $log.debug('createJsonPatch -> patch: %O', patch);

        return patch;
      }

      function doPatch(resource, original, modified, destructive) {
        const patch = createJsonPatch(original, modified, destructive);

        return resource.patch({ id: original.id }, patch);
      }

      /**
       * Assigns a safePatch and destructivePatch method to the given resource class.
       * 
       * 
       * safePatch: Will only create delete operations in the output patch when they have been explicitly set to
       * null in the modified method. This means that properties not present on the modified object will not cause a 
       * delete operation to be included in the patch. This is useful if, for example, you have a form that is only
       * used to edit a subset of fields of on an entity.
       * 
       * destructivePatch: Uses a standard JSON Patch comparison between original and modified objects, any fields that
       * are not present on the modified object that are present on the original will cause a delete operation to be
       * added to the patch. In most cases this is probably _NOT_ the method you're looking for. 
       * 
       */
      function assignPatchMethods(resource) {    
        resource.safePatch = function (original, modified) {
          return doPatch(resource, original, modified, false);
        };

        resource.destructivePatch = function (original, modified) {
          return doPatch(resource, original, modified, true);
        };
      }


      return Object.freeze({
        assignPatchMethods
      });
    }
  ]);

})();
