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

  angular.module('pnc.common.select-modals').service('modalEditService', [
    'modalSelectService',
    'BuildConfigurationSet',
    function (modalSelectService, BuildConfigurationSet) {

     /**
      *
      */
      this.editBuildGroupBuildConfigs = function (buildGroup, buildConfigs) {
        return modalSelectService.openForBuildConfigs({
          title: 'Add / Remove Build Configs from ' + buildGroup.name,
          buildConfigs: buildConfigs
        }).result.then(function (result) {
          return BuildConfigurationSet.updateBuildConfigurations({ id: buildGroup.id }, result).$promise;
        });
      };
    }
  ]);

})();
