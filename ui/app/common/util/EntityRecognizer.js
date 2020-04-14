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

  var module = angular.module('pnc.common.util');

  /**
   * List of supported entities to be recognized by EntityRecognizer.
   */
  module.constant('entityTypes', Object.freeze({
    BUILD: 'BUILD',
    GROUP_BUILD: 'GROUP_BUILD'
  }));


  /**
   * Helper to recognize supported entities (see entityTypes).
   * 
   * Example: EntityRecognizer.isBuild(buildItemToTest)
   */
  module.factory('EntityRecognizer', ['entityTypes', '$log', function (entityTypes, $log) {

    function recognizeEntity(entity) {
      var entityType = null;

      function processRecognition(processedEntity) {
        if (entityType !== null) {
          // only one condition per entity should pass
          $log.error(entityTypes.GROUP_BUILD + ' entity recognition was not successful');
        } else {
          entityType = processedEntity;
        }
      }

      // BUILD
      if (entity.buildConfigRevision !== undefined) {
        processRecognition(entityTypes.BUILD);
      }

      // GROUP BUILD
      if (entity.groupConfig !== undefined) {
        processRecognition(entityTypes.GROUP_BUILD);
      }

      return entityType;
    }


    // ------ API ------

    return {
      isBuild: entity => recognizeEntity(entity) === entityTypes.BUILD,
      isGroupBuild: entity => recognizeEntity(entity) === entityTypes.GROUP_BUILD
    };
  }]);

})();
