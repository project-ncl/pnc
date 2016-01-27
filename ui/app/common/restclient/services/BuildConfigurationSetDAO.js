/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

  module.value('BUILD_CONFIGURATION_SET_ENDPOINT', '/build-configuration-sets/:configurationSetId');

  /**
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('BuildConfigurationSetDAO', [
    '$resource',
    'REST_BASE_URL',
    'BUILD_CONFIGURATION_SET_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    function($resource, REST_BASE_URL, BUILD_CONFIGURATION_SET_ENDPOINT, PageFactory, qh) {
      var ENDPOINT = REST_BASE_URL + BUILD_CONFIGURATION_SET_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        'configurationSetId': '@id'
      },{
        _getAll: {
          method: 'GET',
          url: ENDPOINT + qh.searchOnly(['name'])
        },
        update: {
          method: 'PUT'
        },
        _getConfigurations: {
          method: 'GET',
          url: ENDPOINT + '/build-configurations' + qh.searchOnly(['name'])
        },
        forceBuild: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false,
          params: {
            rebuildAll: true
          }
        },
        build: {
          method: 'POST',
          url: ENDPOINT + '/build',
          successNotification: false
        },
        removeConfiguration: {
          method: 'DELETE',
          url: ENDPOINT + '/build-configurations/:configurationId'
        },
        addConfiguration: {
          method: 'POST',
          url: ENDPOINT + '/build-configurations'
        },
        _getRecords: {
          method: 'GET',
          url: ENDPOINT + '/build-records'
        }
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getConfigurations', 'getConfigurations');
      PageFactory.decorateNonPaged(resource, '_getRecords', 'getRecords');

      PageFactory.decorate(resource, '_getAll', 'getAll');
      PageFactory.decorate(resource, '_getConfigurations', 'getPagedConfigurations');

      return resource;
    }
  ]);

})();
