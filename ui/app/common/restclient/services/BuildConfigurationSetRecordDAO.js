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

  var module = angular.module('pnc.common.restclient');

  module.value('BUILD_CONFIG_SET_RECORD_ENDPOINT', '/build-config-set-records/:recordId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('BuildConfigurationSetRecordDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'BUILD_CONFIG_SET_RECORD_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function ($resource, $injector, REST_BASE_URL, BUILD_CONFIG_SET_RECORD_ENDPOINT, PageFactory, qh,
              PncCacheUtil) {

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

      PncCacheUtil.decorateIndexId(resource, 'BuildConfigurationSetRecord', 'get');

      _([['_getAll'],
         ['_getByUser']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'BuildConfigurationSetRecord', e[0]);
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'getAll');

      _([['_getRunning', 'getPagedRunning'],
         ['_getFinished', 'getPagedFinished'],
         ['_getByUser', 'getPagedByUser']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getConfigurationSet = function () {
        return $injector.get('BuildConfigurationSetDAO').get({ configurationSetId: this.buildConfigurationSetId });
      };

      resource.prototype.getUser = function () {
        return $injector.get('UserDAO').get({ userId: this.userId });
      };

      return resource;
    }
  ]);

})();
