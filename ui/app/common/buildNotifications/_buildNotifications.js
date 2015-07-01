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

  var module = angular.module('pnc.common.buildNotifications', []);

  /**
   * Keeps track of running builds. See the "track" function.
   * @ngdoc service
   * @name BuildProgressService
   * @author Jakub Senko
   */
  module.service('BuildProgressService', [
    '$log',
    '$timeout',
    'BuildRecordNotifications',
    function ($log, $timeout, BuildRecordNotifications) {
      // jshint unused:false

      this.BUILD_RECORD_FILTER = {
        WITH_BUILD_CONFIGURATION: function (id) {
          return {
            filter: function (e) {
              return e.buildConfigurationId === id;
            }
          };
        },
        IS_IN_PROGRESS: function () {
          return {
            filter: function (e) {
              return e.status === 'BUILDING';
            }
          };
        },
        IS_FINISHED: function () {
          return {
            filter: function (e) {
              return e.status !== 'BUILDING';
            }
          };
        }
      };

      this.BUILD_RECORD_UPDATER = {
        /**
         * Optionally update the item with new data and return it.
         * If the item cannot be updated, return null.
         * After this, the filters are applied to determine
         * if the updated item should be kept or deleted for that particular list.
         */
        update: function (old, data) {
          if (old.id === data.id) {
            return data; // only replace build record with the same id
          }
          else {
            return null;
          }
        },
        /**
         * Similar to update, but called after none of the existing items were updated
         * in a given list. If the array filters match, it is inserted at the beginning.
         */
        insert: function (data) {
          return data;
        }
      };


      var trackedItemsArray = {};


      function filterData(dataFiltersArray, data) {
        for(var i = 0; i < dataFiltersArray.length; i++) {
          if (!dataFiltersArray[i].filter(data)) {
            return false;
          }
        }
        return true;
      }


      function filterDataArray(dataFiltersArray, dataArray) {
        var res = [];
        dataArray.forEach(function (data) {
          if (filterData(dataFiltersArray, data)) {
            res.push(data);
          }
        });
        return res;
      }

      /**
       * Refresh view of the trackedItem scope.
       */
      function refresh(trackedItem) {
        $timeout(function () {
          // $timeout waits if the $digest is already in progress
          trackedItem.scope[trackedItem.reference] = trackedItem.data;
        });
      }


      function processReplaceDelete(trackedItem, data, key) {
        var updated = false;
        for (var i = 0; i < trackedItem.data.length; i++) {
          var updatedData = trackedItem.updater.update(trackedItem.data[i], data);

          if (updatedData) {
            if (filterData(trackedItem.dataFiltersArray, updatedData)) {
              // REPLACE!
              $log.debug('Processed received data ', data, ' and REPLACED ', trackedItem.data[i], ' with ', updatedData, ' in ', key, '.');
              trackedItem.data[i] = updatedData;
            }
            else {
              // DELETE!
              $log.debug('Processed received data ', data, ' and DELETED ', trackedItem.data[i], ' in ', key, '.');
              trackedItem.data.splice(i, 1);
            }
            updated = true;
          }
        }
        return updated;
      }


      function processInsert(trackedItem, data, key) {
        var insertedData = trackedItem.updater.insert(data);
        if (insertedData !== null) {
          if (filterData(trackedItem.dataFiltersArray, insertedData)) {
            // INSERT!
            $log.debug('Processed received data ', data, ' and INSERTED ', insertedData, ' in ', key, '.');
            trackedItem.data.unshift(insertedData);
          }
          return true;
        }
        return false;
      }


      function process(data) {
        Object.keys(trackedItemsArray).forEach(function (key) {
          var trackedItem = trackedItemsArray[key];

          var updated = processReplaceDelete(trackedItem, data, key);
          if (!updated) {
            updated = processInsert(trackedItem, data, key);
          }
          if (updated) {
            refresh(trackedItem);
          }
          else {
            $log.debug('Processed received data ', data, ' and NOTHING changed in ', key, '.');
          }
        });
      }

      /**
       * This function keeps track of a scope variable containing an array of build records
       * and automatically updates them when the data changes, with the help of filters.
       *
       * @param scope which contains the watched variable
       * @param reference name of the array of build records within the scope
       * @param dataLoaderFunction function that loads fresh (initial) data,
       * for example from the REST endpoints. This data may
       * be more specific than just all build records, for example only records for a
       * specific configuration. Filters are applied.
       * @param dataFiltersArray a list of filters to be applied to the data to make sure that 
       * the correct collection is updated (when new a build starts/or is updated with new data,
       * the service must know in which references should the build be added/replaced).
       * For example, the collection of running builds for build configuration #3 should not be updated
       * when a new build for configuration #4 starts. 
       * @param updater object that transforms the data received from the WS endpoint
       * into data in the tracked variable. Selects data that are updated.
       */
      this.track = function (scope, reference, dataLoaderFunction, dataFiltersArray, updater) {
        var key = '' + scope.$id + reference;

        if (trackedItemsArray[key] !== undefined) {
          $log.error('Given reference ', key, ' is already tracked.');
          return;
        }

        trackedItemsArray[key] = {
          scope: scope,
          reference: reference,
          dataLoaderFunction: dataLoaderFunction,
          dataFiltersArray: dataFiltersArray,
          updater: updater
        };

        dataLoaderFunction().then(function (dataArray) {
          trackedItemsArray[key].data = filterDataArray(dataFiltersArray, dataArray);
          refresh(trackedItemsArray[key]);
        });
      };


      BuildRecordNotifications.listen(function (data) {
        process(data);
      });
    }
  ]);

})();
