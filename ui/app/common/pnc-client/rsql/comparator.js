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
   * @name pnc.common.pnc-client.rsql:comparator
   * @description
   * This is an internal class used by rsqlQuery, see that for usage instructions.
   *
   * @author Alex Creasy
   */
  module.factory('comparator', [
    function () {
      return function comparator(ctx) {
        var that = {};

        that.eq = function (value) {
          ctx.addToQuery('==' + value);
          return ctx.next();
        };

        that.neq = function (value) {
          ctx.addToQuery('!=' + value);
          return ctx.next();
        };

        that.lt = function (value) {
          ctx.addToQuery('=lt=' + value);
          return ctx.next();
        };

        that.le = function (value) {
          ctx.addToQuery('=le=' + value);
          return ctx.next();
        };

        that.gt = function (value) {
          ctx.addToQuery('=gt=' + value);
          return ctx.next();
        };

        that.ge = function (value) {
          ctx.addToQuery('=ge=' + value);
          return ctx.next();
        };

        that.like = function (value) {
          ctx.addToQuery('=like="' + value +'"');
          return ctx.next();
        };

        return that;
      };
    }
  ]);

})();
