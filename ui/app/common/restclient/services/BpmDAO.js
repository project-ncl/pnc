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

(function() {

  var module = angular.module('pnc.common.restclient');

  module.value('BPM_ENDPOINT', '/bpm/tasks');

  /**
   * @author Martin Kelnar
   */
  module.factory('BpmDAO', [
    '$http',
    'REST_BASE_URL',
    'BPM_ENDPOINT',
    function ($http, REST_BASE_URL, BPM_ENDPOINT) {
      var ENDPOINT = REST_BASE_URL + BPM_ENDPOINT;

      var resource = {};

      resource.startBuildConfigurationCreation = function(data) {
        
        return $http.post(ENDPOINT + '/start-build-configuration-creation', {
          name:                 data.name,
          description:          data.description,
          buildScript:          data.buildScript,
          scmRepoURL:           data.scmInternal.url,
          scmRevision:          data.scmInternal.revision,
          scmExternalRepoURL:   data.scmExternal.url,
          scmExternalRevision:  data.scmExternal.revision,
          projectId:            data.project.id,
          buildEnvironmentId:   data.environment.id,
          dependencyIds:        data.dependencyIds,
          productVersionId:     data.productVersionId
        });

      };

      return resource;
    }
  ]);

})();
