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
   * @name pnc.common.pnc-client.rsql:context
   * @description
   * This is an internal class used by rsqlQuery, see that for usage instructions.
   *
   * @author Alex Creasy
   */
  module.factory('context', [
    'selector',
    'comparator',
    'operator',
    function (selector, comparator, operator) {
      return function context() {
        var that = {};
        var q = [];

        var nodes = {
          selector: function () {
            this.next = this.comparator;
            return selector(that);
          },
          comparator: function () {
            this.next = this.operator;
            return comparator(that);
          },
          operator: function () {
            this.next = this.selector;
            return operator(that);
          },
        };

        nodes.next = nodes.selector.bind(nodes);

        that.addToQuery = function (str) {
          q.push(str);
        };

        that.next = function () {
          return nodes.next();
        };

        that.end = function () {
          return q.join('');
        };

        return that;
      };
    }
  ]);

})();
