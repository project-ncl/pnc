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

  // Mathematical base to work in. Currently: Binary units (i.e. KiB etc). To use KB / MB etc switch to decimal (10).
  const BASE = 2; 
  // Thershold at which to switch units.
  const THRESHOLD = Math.pow(BASE, 10);
  // Decimal places to display result in.
  const DECIMAL_PLACES = 2;
  // Separator character between quantity and unit for output.
  const SEPARATOR = ' ';
  // Units to display, ordered by order of magnitude.  
  const units = ['B', 'KiB', 'MiB', 'GiB', 'TiB'];

  angular.module('pnc.common.filters')
      .filter('fileSize', [
        function () {
          return function (sizeInBytes) {

            if (sizeInBytes === 0) {
              return '0' + SEPARATOR + units[0];
            }

            const exp = Math.floor(Math.log(sizeInBytes) / Math.log(THRESHOLD), units.length);

            if (exp === 0) {
              return sizeInBytes + SEPARATOR + units[exp];
            }

            return (sizeInBytes / Math.pow(THRESHOLD, exp)).toFixed(DECIMAL_PLACES) + SEPARATOR + units[exp];
          };
        }
      ]);

})();