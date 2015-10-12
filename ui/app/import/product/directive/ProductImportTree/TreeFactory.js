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

  var module = angular.module('pnc.import');

  /**
   * Very simple tree data structure.
   *
   * @author Jakub Senko
   */
  module.service('TreeFactory', [
    function () {

      var factory = this;

      factory.build = function () {

        var tree = {
          _onUpdate: []
        };

        tree.append = function (data) {
          var subNode = createNode(tree, data);
          if (_.isUndefined(tree.nodes)) {
            tree.nodes = [];
          }
          tree.nodes.push(subNode);
          tree._refresh();
          return subNode;
        };

        var createNode = function (parent, nodeData) {
          var node = {
            //parent: parent,
            nodeData: nodeData
          };

          node.append = function (data) {
            var subNode = createNode(node, data);
            if (_.isUndefined(node.nodes)) {
              node.nodes = [];
            }
            node.nodes.push(subNode);
            tree._refresh();
            return subNode;
          };

          node.getParent = _.constant(parent);

          return node;
        };

        tree.getParent = _.constant(null);

        tree.clear = function () {
          delete tree.nodes;
        };

        tree._refresh = function () {
          _(tree._onUpdate).each(function (f) {
            f(tree);
          });
        };

        tree.onUpdate = function (f) {
          tree._onUpdate.push(f);
        };

        return tree;
      };
    }
  ]);
})();
