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

    angular.module('pnc.builds').component('pncBuildDetailBrewPushPage', {
      bindings: {
        build: '<',
        brewPushResult: '<'
      },
      templateUrl: 'builds/detail/brew-push/pnc-build-detail-brew-push-page.html',
      controller: ['$scope', Controller]
    });

    function Controller($scope) {
      const $ctrl = this;

      // -- Controller API --


      // --------------------

      $ctrl.$onInit = () => {
        load($ctrl.brewPushResult);

        $scope.$on('BUILD_PUSH_STATUS_CHANGE', (event, brewPushResult) => {
          if (brewPushResult.buildId === $ctrl.build.id) {
            $scope.$applyAsync(() => load(brewPushResult));
          }
        });
      };

      function load(brewPushResult) {
        if (brewPushResult) {
          $ctrl.data = brewPushResult;
          $ctrl.prefixFilters = 'loggerName.keyword:org.jboss.pnc.causeway';
          $ctrl.matchFilters = `mdc.processContext.keyword:${$ctrl.brewPushResult.logContext}`;
        }
      }
    }

  })();
