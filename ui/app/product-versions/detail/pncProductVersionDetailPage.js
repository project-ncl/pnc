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

  angular.module('pnc.product-versions').component('pncProductVersionDetailPage', {
    bindings: {
      productVersion: '<',
      buildConfigs: '<',
      groupConfigs: '<',
      productReleases: '<',
      productMilestones: '<'
    },
    templateUrl: 'product-versions/detail/pnc-product-version-detail-page.html',
    controller: ['ProductVersionResource', 'paginator', Controller]
  });


  function Controller(ProductVersionResource, paginator) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.getFullName = getFullName;
    $ctrl.save = save;
    $ctrl.editBuildConfigs = editBuildConfigs;
    $ctrl.editGroupConfigs = editGroupConfigs;
    $ctrl.fetchGroupConfigRefs = fetchGroupConfigRefs;
    $ctrl.refreshBuildConfigs = refreshBuildConfigs;
    $ctrl.removeBuildConfig = removeBuildConfig;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.productReleasesPage = paginator($ctrl.productReleases);
      $ctrl.productMilestonesPage = paginator($ctrl.productMilestones);
    };

    function getFullName() {
      return `${$ctrl.productVersion.product.name} ${$ctrl.productVersion.version}`;
    }

    function save($data) {
      $data.attributes = {};
      $data.attributes.BREW_TAG_PREFIX = $data.brewTagPrefix;
      delete $data.brewTagPrefix;

      return ProductVersionResource.safePatch($ctrl.productVersion, $data).$promise
          .catch(err => err.data.errorMessage || 'Unrecognised error from PNC REST API');
    }

    function editBuildConfigs(result) {
      console.log('EDIT BCs: %O', result);
      return ProductVersionResource.arrayPatch($ctrl.buildConfigs.data, result, 'buildConfigs', $ctrl.productVersion.id).$promise;
    }

    function removeBuildConfig(buildConfig) {
      console.log('REMOVE BC: %O', buildConfig);
      return $ctrl.productVersion.$removeBuildConfig({ buildConfigId: buildConfig.id });
    }

    function editGroupConfigs(result) {
      console.log('EDIT GCs: %O', result);
        const updated = {
          groupConfigs: _.keyBy(result, 'id')
        };

        ProductVersionResource.safePatch($ctrl.productVersion, updated);
    }

    function fetchGroupConfigRefs() {
      return $ctrl.productVersion.$get().then(productVersion => {
        return Object.values(productVersion.groupConfigs);
      });
    }

    function refreshBuildConfigs(buildConfigs) {
      $ctrl.buildConfigs.data = buildConfigs;
    }
  }


})();
