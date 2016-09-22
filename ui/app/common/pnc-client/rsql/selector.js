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
(function () {
  'use strict';

  var module = angular.module('pnc.common.pnc-client.rsql');

  /**
   * @ngdoc service
   * @kind function
   * @name pnc.common.pnc-client.rsql:selector
   * @description
   * This is an internal class used by rsqlQuery, see that for usage instructions.
   *
   * @author Alex Creasy
   */
  module.factory('selector', [
    function () {
      /*
       * For selecting a field to operate on e.g. `.where('name')`
       */
      return function selector(ctx) {
        var that = {};

        that.where = function (field) {
          ctx.addToQuery(field);
          return ctx.next();
        };

        that.brackets = function (query) {
          ctx.addToQuery('(' + query + ')');
          return ctx.jumpTo('operator');
        };

        return that;
      };
    }
  ]);

})();
