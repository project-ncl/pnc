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
   * @author Jakub Senko
   */
  module.directive('productImportTree', [
    function () {

      return {
        restrict: 'E',
        templateUrl: 'import/product/directive/ProductImportTree/product-import-tree.html',
        scope: {
          tree: '=',
          bcSetName: '=',
          onFinish: '&',
          onReset: '&'
        },
        link: function (scope, el) {

          scope.finish = function () {
            if (scope.finishForm.$valid) {
              scope.onFinish();
            } else {
              _(scope.finishForm).each(function (field) {
                if (_(field).has('$dirty') && field.$pristine) {
                  field.$dirty = true;
                }
              });
            }
          };


          scope.reset = function () {
            scope.onReset();
          };


          scope.tree.onUpdate(function (tree) {
            /**
             * Could not work with the treeview
             * data structure directly without problems
             * (the plugin makes a copy of the data).
             * Therefore, the tree structure is handled separately
             * and when updated, it is completely redrawn.
             */
            $(el).find('div .bc-tree').treeview({
              data: tree.nodes,
              collapseIcon: 'fa fa-angle-down',
              expandIcon: 'fa fa-angle-right',
              checkedIcon: 'fa fa-check-square-o',
              uncheckedIcon: 'fa fa-square-o',
              showIcon: false,
              showCheckbox: true,
              onNodeSelected: function (event, node) {
                node.select();
              },
              onNodeChecked: function (event, node) {
                node.toggle();
              },
              onNodeUnchecked: function (event, node) {
                node.toggle();
              },
              onNodeExpanded: function (event, node) {
                node.expand();
              },
              onNodeCollapsed: function (event, node) {
                node.expand();
              }
            });
          });
        }
      };
    }
  ]);

})();
