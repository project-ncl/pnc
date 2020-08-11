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

  angular.module('pnc.builds').component('pncBuildTree', {
    bindings: {
      /**
       * object representing Build
       */
      build: '<?',
      /**
       * object representing Group Build
       */
      groupBuild: '<?',
      /**
       * object representing dependencyGraph, allows loading via resolve, if absent it will be
       * pulled automatically
       */
      dependencyGraph: '<?'
    },
    templateUrl: 'builds/directives/pnc-build-tree/pnc-build-tree.html',
    controller: ['BuildResource', 'GroupBuildResource', '$timeout', '$scope', '$log', '$q', 'EntityRecognizer', Controller]
  });

  function Controller(BuildResource, GroupBuildResource, $timeout, $scope, $log, $q, EntityRecognizer) {

    var $ctrl = this;
    var buildItemPromise = null;
    var UNIQUE_ID = $scope.$id; // a unique ID for each component

    var NO_PARENT_EXISTS = null;

    // -- Controller API --
    $ctrl.buildTree = null;
    $ctrl.buildItem = null;
    $ctrl.isLoaded = false;
    $ctrl.loadingErrorMessage = '';
    $ctrl.expandNodes = expandNodes;
    $ctrl.componentIdAttr = 'build-tree-' + UNIQUE_ID;


    // --------------------


    /**
     * Calculate and save dependencies & dependents to the vertices of the dependency graph
     *
     * @param {Object} dependencyGraph - Dependency graph from this build (or group build)
     */
    function calculateDependencies(dependencyGraph) {
      dependencyGraph.edges.forEach(edge => {
        let sourceId = edge.source;
        let targetId = edge.target;
        let sourceNode = dependencyGraph.vertices[sourceId];
        let targetNode = dependencyGraph.vertices[targetId];

        sourceNode._dependencyBuildIds = sourceNode._dependencyBuildIds || [];
        targetNode._dependentBuildIds = targetNode._dependentBuildIds || [];
        sourceNode._dependencyBuildIds.push(targetId);
        targetNode._dependentBuildIds.push(sourceId);
      });
    }

    $ctrl.$onInit = function() {
      $ctrl.buildItem = $ctrl.build ? $ctrl.build : $ctrl.groupBuild;

      if ($ctrl.dependencyGraph) {
        buildItemPromise = $q.when($ctrl.dependencyGraph);
      } else {
        buildItemPromise = ($ctrl.build ? BuildResource : GroupBuildResource).getDependencyGraph({
          id: $ctrl.buildItem.id
        }).$promise;
      }

      buildItemPromise.then(function (dependencyGraph) {
        calculateDependencies(dependencyGraph);
        $ctrl.buildTree = convertGraphToTree(dependencyGraph, { expandLevel: 2 });
      }).catch((error) => {
        $ctrl.loadingErrorMessage = error.data.errorMessage;
      }).finally(function () {
        $ctrl.isLoaded = true;
      });

    };

    // construct table grid when data is ready
    $ctrl.$postLink = function() {
      buildItemPromise.then(function () {
        $timeout(function() {
          $('.table-treegrid').treegrid();
          $ctrl.dependencyStructureIsLoaded = true;
        });
      });
    };


    /*
     * Expand All is not natively supported by Patternfly.
     */
    function expandNodes(level) {

      // more performant way to expand first two levels
      if (level > 0 && level <= 2) {
        var $nodeAll = $('#' + $ctrl.componentIdAttr + ' .treegrid-node');

        if (!$nodeAll.first().parent().hasClass('collapsed')) {
          $nodeAll.first().click();
        }

        for (var i = 1; i <= level; i++) {
          $nodeAll.filter('.level-' + i).click();
        }
      }

      // expand other levels
      else {
        var expandOptions = {};
        if (level) {
          if (level === -1) {
            expandOptions.expandFailed = true;
          } else if (level >= 1) {
            expandOptions.expandLevel = level;
          }
        } else {
          expandOptions = null;
        }

        $ctrl.dependencyStructureIsLoaded = false;
        buildItemPromise.then(function (dependencyGraph) {
          $ctrl.buildTree.dependencyStructure = convertGraphToTree(dependencyGraph, expandOptions).dependencyStructure;
          $timeout(function() {
            $('.table-treegrid').treegrid();
            $ctrl.dependencyStructureIsLoaded = true;
          });
        });
      }
    }

    /**
     * Returns an array of vertex ids for all root vertices in the graph,
     * (i.e. those that have no parent vertex)
     */
    function getRootVertices(graph) {

      var targets = graph.edges.map(function (edge) {
        return edge.target;
      });

      // A vertex is a root if it is never the target of an edge (i.e. it has no parents)
      return Object.keys(graph.vertices).filter(function (vertexName) {

        var found = targets.find(function (target) {
          return target === vertexName;
        });

        return angular.isUndefined(found);
      });
    }


    function convertGraphToTree(dependencyGraph, expandOptions) {

      var PREFIX_PARENT = '#';
      var ID_SEPARATOR = '-';

      var dependencyStructure = [];

      /**
       * Creates dependency structure
       *
       * @param {Object} build - processed build
       * @param {Object} customBuildParent - parent build for processed build
       */
      function createDependencyStructure(build, customBuildParent, level) {

        level = level || 1;

        // Build or GroupBuild
        var isBuild = EntityRecognizer.isBuild(build);

        var isCurrentPageBuild = customBuildParent === NO_PARENT_EXISTS;

        var attrClass = '';
        if (expandOptions) {
          if ((expandOptions.expandLevel && level > expandOptions.expandLevel) ||
              (expandOptions.expandFailed && (build.status === 'DONE' || build.status === 'REJECTED_ALREADY_BUILT') && !isCurrentPageBuild)) {
            attrClass += 'collapsed';
          }
        }
        if (isCurrentPageBuild) {
          attrClass += ' bg-lightblue';
        }

        var customBuild = {
          id: build.id,
          attrId: customBuildParent ? customBuildParent.attrId + ID_SEPARATOR + build.id : UNIQUE_ID + '-' + build.id,
          attrParent: customBuildParent ? PREFIX_PARENT + customBuildParent.attrId : undefined,
          attrClass: attrClass,
          tdAttrClass: 'level-' + level
        };

        if (isBuild) {
          customBuild.isBuild = true;
        } else {
          customBuild.isGroupBuild = true;

          // The BuildGroupRecord's list of dependencies contains ALL BuildRecords in the tree,
          // this needs to be filtered down to just the top level, otherwise we'll duplicate
          // every branch in the tree back to the top level.
          build._buildIds = getRootVertices(dependencyGraph);
        }

        dependencyStructure.push(customBuild);
        build._dependencyBuildIds = dependencyGraph.vertices[build.id]._dependencyBuildIds || [];
        (isBuild ? build._dependencyBuildIds : build._buildIds).forEach(function (buildId) {
          if (dependencyGraph.vertices[buildId]) {
            createDependencyStructure(dependencyGraph.vertices[buildId].data, customBuild, level + 1);
          }
        });

        return dependencyStructure;
      }
      if (EntityRecognizer.isBuild($ctrl.buildItem)) {
        $ctrl.buildItem._dependentBuildIds = dependencyGraph.vertices[$ctrl.buildItem.id]._dependentBuildIds || [];
      }

      return {
        dependentStructure: $ctrl.buildItem._dependentBuildIds, // undefined when GroupBuild is being processed
        dependencyStructure: createDependencyStructure($ctrl.buildItem, NO_PARENT_EXISTS),
        nodes: dependencyGraph.vertices
      };
    }

  }

})();
