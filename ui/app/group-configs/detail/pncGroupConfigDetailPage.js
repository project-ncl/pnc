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

  angular.module('pnc.group-configs').component('pncGroupConfigDetailPage', {
    bindings: {
      /**
       * Object GroupConfigResource: The GroupConfig resource object to display info for
       */
      groupConfig: '<',
      /**
       * The product version associated with GroupConfig
       */
      productVersion: '<'
    },
    templateUrl: 'group-configs/detail/pnc-group-config-detail-page.html',
    controller: ['$log', '$state', 'modalSelectService', 'GroupConfigResource', Controller]
  });

  function Controller($log, $state, modalSelectService, GroupConfigResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.update = update;
    $ctrl.refresh = refresh;
    $ctrl.delete = deleteGroupConfig;
    $ctrl.linkWithProductVersion = linkWithProductVersion;
    $ctrl.unlinkFromProductVersion = unlinkFromProductVersion;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.formModel = $ctrl.groupConfig.toJSON();
    };


    function resetState(groupConfig, productVersion) {
      $log.debug('pncGroupConfigDetailPage::resetState [groupConfig: %O | productVersion: %O]', groupConfig, productVersion);
      $ctrl.groupConfig = groupConfig;
      $ctrl.productVersion = productVersion;
      $ctrl.formModel = groupConfig.toJSON();
    }

    function update(data) {
      return GroupConfigResource.safePatch($ctrl.groupConfig, {'name': data.name, 'productVersion': { 'id': $ctrl.formModel.productVersion.id}})
          .$promise
          .catch(
            // String retval signals to x-editable lib that the request failed and to rollback the changes in the view.
            error => error.data.errorMessage
          );
    }

    function refresh() {
      $ctrl.productVersion = $ctrl.formModel.productVersion;
    }

    function deleteGroupConfig() {
      $ctrl.groupConfig
          .$delete()
          .then(() => $state.go('group-configs.list'));
    }

    function linkWithProductVersion() {

      modalSelectService.openForProductVersion({
        title: 'Link ' + $ctrl.groupConfig.name + ' with a product version'
      })
          .result
          .then(productVersion => {
              GroupConfigResource
                  .linkWithProductVersion($ctrl.groupConfig, productVersion)
                  .then(patchedGroupConfig => resetState(patchedGroupConfig, productVersion));
      });
    }

    function unlinkFromProductVersion() {
      GroupConfigResource.unlinkFromProductVersion($ctrl.groupConfig)
          .then(patchedGroupConfig => resetState(patchedGroupConfig, null));
    }
  }

})();
