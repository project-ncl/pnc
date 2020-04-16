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
  
  module.factory('dateUtilConverter', function() {

    return {
      convertToTimestampNoon: function(date) {
        if (!date) {
          return null;
        }

        // change from midnight to noon
        date.setHours(12);
        date.setMinutes(0);
        date.setSeconds(0);

        return date.getTime();
      },

      convertToUTCNoon: function(date) {
        if (!date) {
          return null;
        }

        // change from midnight to noon
        date.setUTCHours(12);
        date.setUTCMinutes(0);
        date.setUTCSeconds(0);

        return date;
      },

      initDatePicker: function(scope) {
        scope.opened = [];
        scope.today = function () {
          scope.dt = new Date();
        };
        scope.today();

        scope.clear = function () {
          scope.dt = null;
        };

        scope.open = function ($event, id) {
          $event.preventDefault();
          $event.stopPropagation();

          scope.opened[id] = true;
        };

        scope.format = 'yyyy/MM/dd';
      }
    };
  });

})();
