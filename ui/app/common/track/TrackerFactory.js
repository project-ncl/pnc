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

(function () {

  var module = angular.module('pnc.common.buildNotifications', []);

  /**
   * Create trackers, which are objects that listen for events and
   * update a data variable containing an array of items.
   * @see the track function
   *
   * @author Jakub Senko
   */
  module.service('TrackerFactory', [
    '$log',
    '$timeout',
    '$q',
    function ($log, $timeout, $q) {
      // jshint unused:false

      var filterData = function (dataFilters, data) {
        return _(dataFilters).every(function (filter) {
          return filter(data);
        });
      };


      var filterDataArray = function (dataFilters, dataArray) {
        return _(dataArray).filter(function (data) {
            return filterData(dataFilters, data);
          }
        );
      };

      /**
       * This function keeps track of a variable (tracker.data) containing an array of items
       * and automatically updates them when suitable events are received.
       *
       * @param dataLoader function that loads fresh (initial) data,
       * for example from the REST endpoints. May return a promise.
       * dataFilters are applied.
       * @param dataFilters a list of filters that determine if an item belongs
       * to the tracked data array. It is applied to determine if an updated item
       * should stay in the array or be deleted.
       * @param dataUpdater object that for each item in the array applies changes from
       * the received event or potentially creates new item to be inserted.
       * It is an object with two functions:
       * - update - which modifies an item or returns null if this is not possible.
       * - insert - generates new item or returns null if not possible.
       * After, these changes filters are always applied.
       * @sortBy function used to sort the tracked array.
       * Given data item, return a primitive value that can be used to sort
       * the data item among others, using standard comparison operators in ascending order.
       */
      this.track = function (dataLoader, dataFilters, dataUpdater, sortBy) {

        var tracker = {
          data: [],
          isLoaded: false
        };

        var refresh = function (data) {
          tracker.data = data;
          if (!_.isFunction(tracker.onUpdate)) {
            tracker.onUpdate = _.noop;
          }
          tracker.onUpdate(data);
          $timeout(_.noop);
        };

        var processReplaceDelete = function (state, payload) {
          var promises = _(state.dataArray).map(function (e) {
            var updatedData = dataUpdater.update(e, payload);
            return $q.when(updatedData).then(function (updatedData) {
              if (updatedData !== null) {
                state.updated = true;
                if (filterData(dataFilters, updatedData)) {
                  // REPLACE!
                  console.log('REPLACED!', updatedData, 'for payload', payload);
                  return updatedData;
                }
                else {
                  // DELETE!
                  console.log('DELETED!', updatedData, 'for payload', payload);
                  return null;
                }
              }
              return e;
            });
          });
          return $q.all(promises).then(function (result) {
            state.dataArray = _(result).filter(function (e) {
              return e !== null;
            });
            return state;
          });
        };

        var processInsert = function (state, payload) {
          var insertedData = dataUpdater.insert(payload);
          return $q.when(insertedData).then(function (insertedData) {
            if (insertedData !== null) {
              if (filterData(dataFilters, insertedData)) {
                // INSERT!
                console.log('INSERT!', insertedData, 'for payload', payload);
                state.dataArray.unshift(insertedData);
                state.updated = true;
              }
            }
            return state;
          });
        };

        // public
        tracker.processEvent = function (payload) {
          var state = {
            updated: false,
            dataArray: tracker.data.slice(0)
          };

          return processReplaceDelete(state, payload).then(function (state) {
            if (!state.updated) {
              return processInsert(state, payload);
            }
            return state;
          }).then(function (state) {
            if (state.updated) {
              refresh(_(state.dataArray).sortBy(sortBy));
            }
          });
        };


        $q.when(dataLoader()).then(function (dataArray) {
          return _(filterDataArray(dataFilters, dataArray)).sortBy(sortBy);
        }).then(function (data) {
          tracker.isLoaded = true;
          refresh(data);
        });

        return tracker;
      };
    }
  ]);

})();
