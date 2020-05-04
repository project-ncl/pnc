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

  var module = angular.module('pnc.product-milestones');

  module.controller('MilestoneDetailController', [
    '$scope',
    '$state',
    '$stateParams',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    'distributedArtifacts',
    'performedBuilds',
    'latestRelease',
    function($scope, $state, $stateParams, productDetail, versionDetail, milestoneDetail,
        distributedArtifacts, performedBuilds, latestRelease) {

      var that = this;
      that.product = productDetail;
      that.productVersion = versionDetail;
      that.milestone = milestoneDetail;
      that.distributedArtifacts = distributedArtifacts;
      that.performedBuilds = performedBuilds;
      that.latestRelease = latestRelease;
    }
  ]);

  module.controller('MilestoneLogController', [
    'latestRelease',
    function(latestRelease) {
      var that = this;
      that.latestRelease = latestRelease;
    }
  ]);

  module.controller('MilestoneCloseController', [
    '$scope',
    '$state',
    '$stateParams',
    'productDetail',
    'versionDetail',
    'milestoneDetail',
    function($scope, $state, $stateParams, productDetail,
      versionDetail, milestoneDetail) {

      var that = this;

      that.product = productDetail;
      that.productVersion = versionDetail;

      that.data = milestoneDetail;

      that.submit = function() {
        that.data.$closeMilestone().then(
          function() {
            $state.go('product.detail.version', {
              productId: productDetail.id,
              versionId: versionDetail.id
            }, {
              reload: true
            });
          }
        );
      };
    }
  ]);

})();
