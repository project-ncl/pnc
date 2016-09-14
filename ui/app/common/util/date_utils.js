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

  var module = angular.module('pnc.util.date_utils', []);
  var MINUTE = 60 * 1000;
  var HOUR = 60 * MINUTE;

  module.factory('dateUtilConverter', function() {
    
    return {
      convertToTimestampNoonUTC: function(date) {
        //console.log('Converting date ' + date + ' to UTC noon timestamp');
        if (!date) {
          return null;
        }

        return date.getTime() +
          (12 * HOUR); // change from midnight to noon
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
