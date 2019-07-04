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

  angular.module('pnc.group-configs').component('pncGroupConfigCreatePage', {
    bindings: {
      productVersion: '<'
    },
    templateUrl: 'group-configs/create/pnc-group-config-create-page.html',
    controller: ['$state', 'GroupConfigResource', Controller]
  });

  function Controller($state, GroupConfigResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.create = create;

    // --------------------

    $ctrl.$onInit = () => {
      console.log('$ctrl.productVersion == %O', $ctrl.productVersion);
    };
    

    function create(formValues) {
      console.log('create -> %O', formValues);

      let groupConfig = new GroupConfigResource(formValues);

      let productVersion = $ctrl.productVersion || formValues.productVersion;

      if (productVersion) {
        groupConfig.productVersion = { id: productVersion.id };
      }

      groupConfig.$save().then(() => $state.go('group-configs.detail', { groupConfigId: groupConfig.id }));
      // new GroupConfigResource(formValues)
      //     .$save()
      //     .then(groupConfig => $state.go('group-configs.detail', { groupConfigId: groupConfig.id }));
    }
  }

})();
