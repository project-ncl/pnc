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

(function() {

  var module = angular.module('pnc.common.restclient');


  /**
   * @ngdoc service
   * @name pnc.common.restclient:Build
   * @description
   *
   */
  module.factory('Build', [
    'BuildRecordDAO',
    'RunningBuildDAO',
    '$q',
    function(BuildRecordDAO, RunningBuildDAO, $q) {
      return {
        get: function(spec) {
          var deffered = $q.defer();

          function overrideRejection(response) {
            return $q.when(response);
          }

          /*
           * In order to return the BuildRecord regardless of whether it is in
           * progress or compelted we must attempt to fetch both the
           * RunningBuild and the BuildRecord for the given ID in parralell
           * (Unless something went wrong one of these requests should succeed
           * and one fail). As such we have to catch the rejection for the
           * request that failed and return a resolved promise. We can then
           * check which request succeeded in the success callback and resolve
           * the promise returned to the user with it.
           */
          $q.all([
            BuildRecordDAO.get(spec).$promise.catch(overrideRejection),
            RunningBuildDAO.get(spec).$promise.catch(overrideRejection)
          ]).then(
            function(results) {
              // Success - return whichever record we successfully pulled down.
              if (results[0].id) {
                deffered.resolve(results[0]);
              } else if (results[1].id) {
                deffered.resolve(results[1]);
              } else {
                deffered.reject(results);
              }
            },
            function(results) {
              // Error
              deffered.reject(results);
            }
          );

          return deffered.promise;
        }
      };
    }
  ]);
})();
