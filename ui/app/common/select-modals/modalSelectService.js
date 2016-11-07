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

  angular.module('pnc.common.select-modals').service('modalSelectService', [
    '$modal',
    '$q',
    '$rootScope',
    function ($modal, $q, $rootScope) {

     /*
      * Creates a modal that will start an async digest cycle
      * ensuring any chained promises are updated in the view.
      * This function passes through all arguments given to the $modal.open
      * method and returns the same $modal promise.
      */
      function asyncModal () {
        var modal = $modal.open.apply($modal, arguments);

        modal.result.then(function () {
          $rootScope.$evalAsync();
        });

        return modal;
      }

      /**
       * Opens a modal window for multiple selection of Build Groups (aka BuildConfigurationSets).
       * Takes a config object that can have the following properties:
       *
       *  title {String} - The title to display in the modal window
       *  selected {Array} - An array of BuildGroups that are initially selected.
       */
      this.openForBuildGroups = function (config) {
        return asyncModal({
          animation: true,
          size: 'md',
          templateUrl: 'common/select-modals/build-group-multi-select.html',
          controller: 'BuildGroupMultiSelectController',
          controllerAs: 'ctrl',
          bindToController: true,
          resolve: {
            modalConfig: function () {
              return $q.when(config);
            }
          }
        });
      };

      /**
       *
       *
       */
       this.openForProductVersion = function (config) {
         return asyncModal({
           animation: true,
           size: 'md',
           templateUrl: 'common/select-modals/product-version-single-select.html',
           controller: 'ProductVersionSingleSelectController',
           controllerAs: 'ctrl',
           bindToController: true,
           resolve: {
             modalConfig: function () {
               return $q.when(config);
             }
           }
         });
       };

      /**
       *
       */
       this.openForBuildConfigs = function (config) {
         return asyncModal({
           animation: true,
           size: 'xl',
           template: '<build-config-multi-select modal-ctrl="$ctrl"></build-config-multi-select>',
           controller: ['config', function (config) {
             this.config = config;
           }],
           controllerAs: '$ctrl',
           bindToController: true,
           resolve: {
             config: function () {
               return $q.when(config);
             }
           }
         });
       };
    }
  ]);

})();
