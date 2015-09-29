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

  var module = angular.module('pnc.record');

  /**
   * @author Jakub Senko
   */
  module.directive('pncProductVersionBCs', [
    '$log',
    '$state',
    'BuildConfigurationDAO',
    function ($log, $state, BuildConfigurationDAO) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersionBCs/pnc-product-version-bcs.html',
        scope: {
          version: '='
        },
        link: function (scope) {

          scope.page = BuildConfigurationDAO.getPagedByProductVersion({
            productId: scope.version.productId, versionId: scope.version.id });

          // Executing a build of a configuration
          scope.buildConfig = function(config) {
            $log.debug('**Initiating build of: %O', config.name);

            BuildConfigurationDAO.build({
              configurationId: config.id
            }, {});
          };
        }
      };
    }
  ]);

})();
