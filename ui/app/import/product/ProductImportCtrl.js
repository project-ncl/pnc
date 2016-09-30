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

  module.controller('ProductImportCtrl', [
    '$log',
    'productImport',
    'TreeFactory',
    '$scope',
    '$timeout',
    'pncNotify',
    '$state',
    'ProductVersionDAO',
    function ($log, productImport, TreeFactory, scope, $timeout, pncNotify, $state, ProductVersionDAO) {

      scope.started = false;
      scope.display = 'start';
      scope.$watch('display', function(newval) {
        scope.$root.importProductState = newval;
      });
      var data = {}; // data loaded from analyzer endpoints
      scope.startData = {};
      scope.startSubmitDisabled = false;
      scope.startTooltipIsOpen = false;
      scope.finishSubmitDisabled = false;
      scope.finishTooltipIsOpen = false;
      scope.bcData = {}; // data for the left-side form
      var tree = TreeFactory.build();
      scope.tree = tree;
      scope.validateFormCaller = {call: _.noop()};

      /**
       * Given node, return string label to use in the tree.
       */
      var getNodeText = function (node) {

        var nodeClass = 'text-';
        var nodeTitle = '';

        if (!node.getParent().nodeData) {
          nodeClass = '';
        } else if (node.nodeData.internallyBuilt) {
          nodeClass += 'success';
          nodeTitle = 'The artifact was already built.';
        } else if (node.nodeData.availableVersions && node.nodeData.availableVersions.length) {
          nodeClass += 'warning';
          nodeTitle = 'Another version of the artifact was already built.';
        } else {
          nodeClass += 'danger';
          nodeTitle = 'The artifact hasn\'t been built yet.';
        }

        return '<span class="' + nodeClass + '" title="' + nodeTitle + '">' +
          node.nodeData.gav.groupId + ':<strong>' +
          node.nodeData.gav.artifactId + '</strong>:' +
          node.nodeData.gav.version + (nodeIsValid(node) ? '' : '<span class="fa fa-exclamation-triangle" style="color: #ec7a08;"></span>') + '<span>';
      };


      var getNodeGavString = function (node) {
        return node.nodeData.gav.groupId + ':' +
          node.nodeData.gav.artifactId + ':' +
          node.nodeData.gav.version;
      };


      var nodeIsValid = function (node) {
        if (!_(node).has('valid')) {
          node.valid = true;
        }
        return node.valid;
      };


      /**
       * Handle clicking on a BC in the tree.
       */
      var nodeSelect = function (node) {
        $timeout(function () {
          scope.bcData = node;
        });
      };


      /**
       * Handle toggling a checkbox in the tree.
       * Does not check the parent boxes, but uncheckes recursively.
       */
      var nodeToggle = function (node) {
        $timeout(function () {
          if (_.isUndefined(node.state)) {
            node.state = {};
          }
          node.state.checked = node.state.checked !== true;
          if (node.state.checked) {
            var n = node.getParent();
            while (n !== null) {
              if (_.isUndefined(n.state)) {
                n.state = {};
              }
              //n.state.checked = true;
              n = n.getParent();
            }
          } else {
            var recursiveUncheck = function (n) {
              if (_.isUndefined(n.state)) {
                n.state = {};
              }
              //n.state.checked = false;
              _(n.nodes).each(function (e) {
                recursiveUncheck(e);
              });
            };
            recursiveUncheck(node);
          }
          tree._refresh();
        });
      };


      /**
       * Handle clicking on the arrow for expanding/collapsing a subtree.
       */
      var nodeExpand = function (node) {
        $timeout(function () {
          if (_.isUndefined(node.state)) {
            node.state = {};
          }
          node.state.expanded = node.state.expanded !== true;
          tree._refresh();
        });
      };


      var isStrNonEmpty = function (str) {
        return _.isString(str) && str.length > 0;
      };

      var isValidBCName = function (name) {
        var BC_NAME_REGEXP = /^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?!\.git)+$/;
        return BC_NAME_REGEXP.test(name);
      };

      var isValidURL = function (url) {
        var URL_REGEXP = /^(ftp|http|https):\/\/(\w+:{0,1}\w*@)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%@\-\/]))?$/;
        return URL_REGEXP.test(url);
      };

      var nodeValidate = function (node) {
        node.valid = isStrNonEmpty(node.nodeData.name) &&
          (!isStrNonEmpty(node.nodeData.scmUrl) || isValidURL(node.nodeData.scmUrl)) &&
          (!isStrNonEmpty(node.nodeData.name) || isValidBCName(node.nodeData.name)) &&
          (node.nodeData.environmentId !== null) &&
          (node.nodeData.projectId !== null);
        return node.valid;
      };

      /**
       * Stick the above handlers on each tree node
       * when it is being parsed from received data.
       */
      var processNode = function (node) {
        node.select = _.partial(nodeSelect, node);
        node.text = getNodeText(node);
        node.gavString = getNodeGavString(node);
        node.toggle = _.partial(nodeToggle, node);
        node.analyze = _.partial(analyzeNextLevel, node);
        node.expand = _.partial(nodeExpand, node);
        node.validate = _.partial(nodeValidate, node);
      };


      /**
       * Set checkbox state depending on the REST data
       */
      var processCheckbox = function (node) {
        if (_.isUndefined(node.state)) {
          node.state = {};
        }

        node.state.checked = node.nodeData.selected;
      };


      /**
       * Given a tree and the JSON data structure from the analyzer REST endpoint,
       * parse the structure to add nodes on the tree as needed.
       * @return true on success
       *
       * @param tree
       * Tree representation which is displayed
       *
       * @param data
       * Tree data coming from REST
       *
       */
      var parseData = function (tree, data) {
        /**
         * Check if the node contains child representing the given dependency,
         * so there are no duplicates in the tree.
         */
        var find = function (node, dependency) {
          return _(node.nodes).find(function (e) {
            return _.isEqual(e.nodeData.gav, dependency.gav);
          });
        };

        var recursiveParse = function (node, dataNode) {
          var subNode = find(node, dataNode);
          var subNodeData = _.omit(dataNode, 'dependencies');
          if (_.isUndefined(subNode)) {
            subNode = node.append(subNodeData);
            processNode(subNode);
          } else {
            subNode.nodeData = subNodeData;
          }
          processCheckbox(subNode);
          if (_.isArray(dataNode.dependencies)) {
            var res = true;
            _(dataNode.dependencies).each(function (dependency) {
              res = recursiveParse(subNode, dependency) && res;
            });
            return res;
          } else {
            return false;
          }
        };
        return recursiveParse(tree, data.topLevelBc);
      };


      /**
       * Parse the tree to get the required JSON data structure,
       * basically the reverse of {@link parseData}.
       * @param tree
       * @param update function that allows to use this function in a more general way.
       * Is called for each node parsed and returns 'BC object' for that node, which is the included
       * in the resulting JSON data.
       */
      var parseTree0 = function (tree, update) {
        var res = _.chain(data).clone().omit('scmUrl', 'scmRevision', 'topLevelBc').value();
        res.bcSetName = scope.bcSetName;
        var recursiveParse = function (node) {
          var n = update(node);
          n.dependencies = _(node.nodes).map(function (e) {
            return recursiveParse(e);
          });
          return n;
        };

        res.topLevelBc = recursiveParse(tree.nodes[0]);
        return res;
      };


      /**
       * Parse tree for analyzing next level.
       */
      var parseTree = function (tree) {
        return parseTree0(tree, function (node) {
          node.nodeData.cloneRepo = false;
          return _.clone(node.nodeData);
        });
      };

      /**
       * Parse tree for finishing process.
       * Takes checkboxes into account.
       */
      var parseTreeFinish = function (tree) {
        return parseTree0(tree, function (node) {
          node.nodeData.cloneRepo = false;
          var n = _.clone(node.nodeData);
          n.selected = !_.isUndefined(node.state) && node.state.checked;
          return n;
        });
      };


      var validateTree = function (tree) {
        var recursiveParse = function (node) {
          var valid = true;
          node.selected = !_.isUndefined(node.state) && node.state.checked;
          //var isValid = node.validate();
          if (node.selected && !node.nodeData.useExistingBc) {
            valid = node.validate();//isValid;
          }
          if (_(node).has('nodes')) {
            _(node.nodes).each(function (n) {
              valid = recursiveParse(n) && valid;
            });
          }
          node.text = getNodeText(node);
          return valid;
        };
        return recursiveParse(tree.nodes[0]);
      };


      scope.startProcess = function () {
        scope.startSubmitDisabled = true;
        scope.startTooltipIsOpen = false;
        pncNotify.info('Preparing analysis. This may take a minute, please be patient.');
        productImport.start(scope.startData).then(function (r) {
          if (_(r).has('id')) {
            data = r;
            scope.id = data.id;
            scope.bcSetName = data.bcSetName;
            parseData(tree, data);
            tree.nodes[0].nlaSuccessful = true;
            tree.nodes[0].select();
            scope.display = 'bc';
            tree._refresh();
          } else {
            pncNotify.error('Something went wrong. Make sure that you entered correct data.');
          }
        }, function(error) {
          $log.error('Error starting import process: %s', JSON.stringify(error, null, 2));
          pncNotify.error('RPC Server Error ' + error.code + ': ' + error.message);
          scope.startTooltipIsOpen = true;
        }).finally(function() {
          scope.startSubmitDisabled = false;
        });
      };


      var analyzeNextLevel = function (node) {
        node.nlaSuccessful = true;
        tree.nodes[0].select();
        node.select();
        node.nodeData.selected = true;
        data = parseTree(tree);
        pncNotify.info('Analyzing \'' + node.gavString + '\'. This may take a minute, please be patient.');
        productImport.nextLevel(data).then(function (r) {
          if (_(r).has('id')) {
            data = r;
            if (parseData(tree, data)) {
              node.nlaSuccessful = true;
              tree.nodes[0].select();
              node.select();
              node.expand();
              pncNotify.success('Successfully analyzed ' + (_.isUndefined(node.nodes) ? 0 : node.nodes.length) + ' dependencies for \'' + node.gavString + '\'.');
            } else {
              pncNotify.warn('Could not find dependencies of \'' + node.gavString + '\' in a repository.');
            }
          } else {
            node.nlaSuccessful = false;
            tree.nodes[0].select();
            node.select();
            pncNotify.error('Could not analyze \'' + node.gavString + '\'. Check that the information in the form is correct.');
          }
        }, function(error) {
          $log.error('Remote error analyzing next level: ' + JSON.stringify(error, null, 2));
          pncNotify.error('Error analyzing next level: ' + error.message);
        });
      };

      var goToProductVersion = function(id) { // TODO unnecessary rest call
        ProductVersionDAO.get({versionId:id}).$promise.then(function(r) {
          $state.go('product.detail.version', {productId: r.productId,versionId: r.id});
        });
      };


      scope.finishProcess = function () {
        if(_.isUndefined(tree.nodes[0].state) || tree.nodes[0].state.checked !== true) {
          pncNotify.warn('You have to select at least one BC.');
          return;
        }
        data = parseTreeFinish(tree);
        if (validateTree(tree)) {
          scope.finishSubmitDisabled = true;
          scope.finishTooltipIsOpen = false;
          pncNotify.info('Product is being imported. This may take a minute, please be patient.');
          productImport.finish(data).then(function (r) {
            if(r.success) {
              pncNotify.success('Product import completed!');
              scope.reset();
              goToProductVersion(r.productVersionId);
            } else {
              pncNotify.error('Product import failed. ' + r.message);
            }
          }, function(error) {
            $log.error('Remote error finishing import process: ' + JSON.stringify(error, null, 2));
            pncNotify.error('Product import failed: ' + error.message);
            scope.finishTooltipIsOpen = true;
          }).finally(function() {
            scope.finishSubmitDisabled = false;
          });
        } else {
          tree._refresh();
          pncNotify.warn('Some data is invalid or missing. Verify all checked BCs.');
        }
      };


      scope.reset = function () {
        data = {};
        tree.clear();
        scope.display = 'start';
      };
    }
  ]);

})
();
