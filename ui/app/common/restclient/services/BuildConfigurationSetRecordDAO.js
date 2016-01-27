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

(function () {

  var module = angular.module('pnc.common.restclient');

  module.value('BUILD_CONFIG_SET_RECORD_ENDPOINT', '/build-config-set-records/:recordId');

  /**
   * @ngdoc service
   */
  module.factory('BuildConfigurationSetRecordDAO', [
    '$resource',
    'BuildConfigurationSetDAO',
    'UserDAO',
    'cachedGetter',
    'REST_BASE_URL',
    'BUILD_CONFIG_SET_RECORD_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    function ($resource, BuildConfigurationSetDAO, UserDAO, cachedGetter,
              REST_BASE_URL, BUILD_CONFIG_SET_RECORD_ENDPOINT, PageFactory, qh) {

      var ENDPOINT = REST_BASE_URL + BUILD_CONFIG_SET_RECORD_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        recordId: '@id'
      }, {
        _getAll: {
          method: 'GET'
        },
        _getRunning: {
          method: 'GET',
          url: REST_BASE_URL + '/build-config-set-records?' +
            'q=' + qh.search(['buildConfigurationSet.name']) + ';status==\'BUILDING\''
        },
        _getFinished: {
          method: 'GET',
          url: REST_BASE_URL + '/build-config-set-records?' +
            'q=' + qh.search(['buildConfigurationSet.name']) + ';status!=\'BUILDING\''
        },
        _getByUser: {
          method: 'GET',
          url: ENDPOINT + '/?q=user.id==:userId'
        },
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');

      PageFactory.decorate(resource, '_getRunning', 'getPagedRunning');
      PageFactory.decorate(resource, '_getFinished', 'getPagedFinished');
      PageFactory.decorate(resource, '_getByUser', 'getPagedByUser');

      resource.prototype.getConfigurationSet = cachedGetter(
        function (record) {
          return BuildConfigurationSetDAO.get({configurationSetId: record.buildConfigurationSetId});
        }
      );

      resource.prototype.getUser = cachedGetter(
        function (record) {
          return UserDAO.get({userId: record.userId});
        }
      );

      return resource;
    }
  ]);

})();
