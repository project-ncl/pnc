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
  module.directive('pncProductVersionMilestones', [
    '$log',
    '$state',
    function ($log, $state) {

      return {
        restrict: 'E',
        templateUrl: 'product/directives/pncProductVersionMilestones/pnc-product-version-milestones.html',
        scope: {
          version: '=',
          product: '='
        },
        link: function (scope) {

          var versionDetail = scope.version;
          var productDetail = scope.product;

          scope.unreleaseMilestone = function (milestone) {
            $log.debug('Unreleasing milestone: %O', milestone);

            milestone.releaseDate = null;
            milestone.downloadUrl = null;

            milestone.$update({
              versionId: versionDetail.id
            }).then(
              function () {
                $state.go('product.detail.version', {
                  productId: productDetail.id,
                  versionId: versionDetail.id
                }, {
                  reload: true
                });
              }
            );
          };

          // Mark Milestone as current in Product Version
          scope.markCurrentMilestone = function (milestone) {
            $log.debug('Mark milestone as current: %O', milestone);

            versionDetail.currentProductMilestoneId = milestone.id;

            versionDetail.$update({
              productId: productDetail.id,
              versionId: versionDetail.id
            }).then(function () {
              $state.go('product.detail.version', {
                productId: productDetail.id,
                versionId: versionDetail.id
              }, {
                reload: true
              });
            });
          };
        }
      };
    }
  ]);

})();
