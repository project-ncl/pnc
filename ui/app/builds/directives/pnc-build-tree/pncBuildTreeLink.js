/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  /**
   * The dumb component representing Build Tree Link displaying information like build status icon, 
   * build configuration link and build record link for given Build Record or Build Group Record.
   * 
   * @example 
   * <pnc-build-tree-link build-record="buildRecord"></pnc-build-tree-link>
   */
  angular.module('pnc.build-records').component('pncBuildTreeLink', {
    bindings: {
      /**
       * Object: The BuildRecord to display the link for.
       */
      buildRecord: '<?',
      /**
       * Object: The BuildGroupRecord to display the link for.
       */
      groupBuild: '<?'
    },
    templateUrl: 'builds/directives/pnc-build-tree/pnc-build-tree-link.html',
    controller: ['$scope', 'eventTypes', Controller]
  });

  function Controller($scope, eventTypes) {
    var $ctrl = this;

    $ctrl.$onInit = function() {
      copyBuildItem($ctrl.buildRecord ? $ctrl.buildRecord : $ctrl.groupBuild);

      $scope.$on(eventTypes.BUILD_STATUS_CHANGED, onEvent);
      $scope.$on(eventTypes.BUILD_SET_STATUS_CHANGED, onEvent);
    };

    $ctrl.$onChanges = function(changedBindings) {
      if (changedBindings.buildRecord) {
        copyBuildItem($ctrl.buildRecord);
      } else if (changedBindings.groupBuild) {
        copyBuildItem($ctrl.groupBuild);
      }
    };

    function copyBuildItem(buildItem) {
      $ctrl.buildItem = angular.copy(buildItem);
    }

    function onEvent(event, payload) {
      if (payload.id === $ctrl.buildItem.id) {
        $scope.$applyAsync(function () {
          Object.assign($ctrl.buildItem, payload);
        });
      }
    }
    
  }

})();
