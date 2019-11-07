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

  angular.module('pnc.builds').component('pncBuildDetailDetailsPage', {
    bindings: {
      build: '<',
      dependencyGraph: '<'
    },
    templateUrl: 'builds/detail/details/pnc-build-detail-details-page.html',
    controller: [Controller]
  });


  function Controller() {
    const $ctrl = this;

    // -- Controller API --


    // --------------------

    $ctrl.$onInit = function () {
     
    };


    /* NCL-4433
    $scope.$on(eventTypes.BUILD_FINISHED, function (event, payload) {
      if (recordDetail.id === payload.id) {
        recordDetail.$get();
      }
    });

    $scope.$on(eventTypes.BREW_PUSH_RESULT, function (event, payload) {
      if (payload.buildRecordId === recordDetail.id) {
        $scope.$applyAsync(function () { hasPushResult = true; });
      }
    });

    var unsubscribe = messageBus.subscribe({
      topic: 'component-build',
      id: recordDetail.id
    });

    $scope.$on('$destroy', unsubscribe);
    */
  }

})();
