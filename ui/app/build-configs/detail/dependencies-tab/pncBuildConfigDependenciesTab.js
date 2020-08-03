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

  angular.module('pnc.build-configs').component('pncBuildConfigDependenciesTab', {
    bindings: {
      buildConfig: '<',
      dependencies: '<'
    },
    require : {
      mainCtrl: '^^pncBuildConfigDetailMain'
    },
    templateUrl: 'build-configs/detail/dependencies-tab/pnc-build-config-dependencies-tab.html',
    controller: [Controller]
  });


  function Controller() {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.displayFields = ['name', 'project', 'buildStatus'];

    $ctrl.onRemove = onRemove;
    $ctrl.onEdit = onEdit;
    $ctrl.refreshDependencies = refreshDependencies;

    // --------------------

    function onRemove(dependency) {
      console.log('Remove dependency: %O', dependency);
      return $ctrl.buildConfig.$removeDependency({ dependencyId: dependency.id });
    }

    function onEdit(dependencies) {
      console.log('Update dependencies: %O', dependencies);
      $ctrl.buildConfig.dependencies = dependencies.reduce((map, dep) => (map[dep.id] = dep, map), {});

      return $ctrl.buildConfig.$update();
    }

    function refreshDependencies(dependencies) {
      $ctrl.dependencies.data = dependencies;
    }
  }

})();
