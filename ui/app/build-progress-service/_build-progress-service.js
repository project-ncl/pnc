'use strict';

(function () {

  var module = angular.module('pnc.BuildProgressService', []);

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
      var that = this;

      that.Tester = {
        BUILD_CONFIGURATION: function (id) {
          return {
            test: function (e) {
              return e.buildConfigurationId === id;
            }
          };
        },
        IN_PROGRESS: function () {
          return {
            test: function (e) {
              return e.status === 'BUILDING';
            }
          };
        },
        FINISHED: function () {
          return {
            test: function (e) {
              return e.status !== 'BUILDING';
            }
          };
        }
      };

      that.BUILD_RECORD_UPDATER = {
        /**
         * Optionally update the item with new data and return it.
         * If the item cannot be updated, return null.
         * After this, the testers are applied to determine
         * if the updated item should be kept or deleted for that partical list.
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
         * in a given list. If the array testers match, it is inserted at the beginning.
         */
        insert: function (data) {
          return data;
        }
      };


      var trackedArray = {};


      function test(testerArray, data) {
        var res = true;
        testerArray.forEach(function (tester) {
          if (!tester.test(data)) {
            res = false;
          }
        });
        return res;
      }

      function arrayTest(testerArray, dataArray) {
        var res = true;
        dataArray.forEach(function (data) {
          if (!test(testerArray, data)) {
            res = false;
          }
        });
        return res;
      }

      function arrayFilter(testerArray, dataArray) {
        var res = [];
        dataArray.forEach(function (data) {
          if (test(testerArray, data)) {
            res.push(data);
          }
        });
        return res;
      }

      /**
       * Refresh view of the tracked scope.
       */
      function refresh(tracked) {
        $timeout(function () {
          // $timeout waits if the $digest is already in progress
          tracked.scope[tracked.reference] = tracked.data;
        });
      }


      function processReplaceDelete(tracked, data, key) {
        var updated = false;
        for (var i = 0; i < tracked.data.length; i++) {
          var u = tracked.updater.update(tracked.data[i], data);

          if (u) {
            if (test(tracked.testerArray, u)) {
              // REPLACE!
              $log.debug('Processed received data ', data, ' and REPLACED ', tracked.data[i], ' with ', u, ' in ', key, '.');
              tracked.data[i] = u;
            }
            else {
              // DELETE!
              $log.debug('Processed received data ', data, ' and DELETED ', tracked.data[i], ' in ', key, '.');
              tracked.data.splice(i, 1);
            }
            updated = true;
          }
        }
        return updated;
      }


      function processInsert(tracked, data, key) {
        var u = tracked.updater.insert(data);
        if (u !== null) {
          if (test(tracked.testerArray, u)) {
            // INSERT!
            $log.debug('Processed received data ', data, ' and INSERTED ', u, ' in ', key, '.');
            tracked.data.unshift(u);
          }
          return true;
        }
        return false;
      }


      function process(data) {
        Object.keys(trackedArray).forEach(function (key) {
          var tracked = trackedArray[key];

          var updated = processReplaceDelete(tracked, data, key);
          if (!updated) {
            updated = processInsert(tracked, data, key);
          }
          if (updated) {
            refresh(tracked);
          }
          else {
            $log.debug('Processed received data ', data, ' and NOTHING changed in ', key, '.');
          }
        });
      }

      /**
       * This function keeps track of a scope variable containing an array of build records
       * and automatically updates them when the data changes, with the help of testers.
       *
       * @param scope which contains the watched variable
       * @param reference name of the array of build records within the scope
       * @param loaderFunction function that loads fresh (initial) data,
       * for example from the REST endpoint. This data may
       * be more specific than just all build records, for example only records for a
       * specific configuration. Filters are applied.
       * @param testerArray when new build starts/or is updated with new data,
       * the service must know in which references should the build be added/replaced.
       * For example, collection of running builds for build configuration #3 should not be updated
       * when a new build for configuration #4 starts.
       * Filters make sure that the correct collection is updated.
       * @param updater object that transforms the data received from the WS endpoint
       * into data in the tracked variable. Selects data that are updated.
       */
      that.track = function (scope, reference, loaderFunction, testerArray, updater) {
        var key = '' + scope.$id + reference;

        if (trackedArray[key] !== undefined) {
          $log.error('Given reference ', key, ' is already tracked.');
          return;
        }

        trackedArray[key] = {
          scope: scope,
          reference: reference,
          loaderFunction: loaderFunction,
          testerArray: testerArray,
          updater: updater
        };

        loaderFunction().then(function (data) {
          trackedArray[key].data = arrayFilter(testerArray, data);
          refresh(trackedArray[key]);
        });
      };


      BuildRecordNotifications.listen(function (data) {
        process(data);
      });
    }
  ]);

})();
