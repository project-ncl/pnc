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

  module.value('BUILD_RECORD_ENDPOINT', '/build-records/:recordId');

  /**
   * DAO methods MUST return the same resource type they are defined on.
   *
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('BuildRecordDAO', [
    '$resource',
    '$injector',
    'REST_BASE_URL',
    'BUILD_RECORD_ENDPOINT',
    'PageFactory',
    'QueryHelper',
    'PncCacheUtil',
    function($resource, $injector, REST_BASE_URL, BUILD_RECORD_ENDPOINT,
              PageFactory, qh, PncCacheUtil) {
      var ENDPOINT = REST_BASE_URL + BUILD_RECORD_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        recordId: '@id',
        q: '@q'
      }, {
        _getAll: {
          method: 'GET',
          url: ENDPOINT + qh.searchOnly(['buildConfigurationAudited.name', 'buildConfigurationAudited.project.name'])
        },
        getLog: {
          method: 'GET',
          url: ENDPOINT + '/log',
          transformResponse: function(data) { return { payload: data }; }
        },
        _getByBC: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records/build-configurations/:configurationId' +
            qh.searchOnly(['buildConfigurationAudited.name'])
        },
        _getByUser: {
          method: 'GET',
          url: ENDPOINT + '/?q=user.id==:userId'
        },
        _getByProject: {
          method: 'GET',
          url: REST_BASE_URL + 'record/projects/:projectId'
        },
        getLatestByBC: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configurations/:configurationId/build-records/latest'
        },
        getAuditedBuildConfiguration: {
          method: 'GET',
          url: ENDPOINT + '/build-configuration-audited'
        },
        getCompletedOrRunning: {
          method: 'GET',
          url: ENDPOINT + '/completed-or-running'
        },
        _getByBCSetRecord: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records' +
          '?q=' + qh.search(['buildConfigSetRecord.buildConfigurationSet.name']) +
          ';buildConfigSetRecord.id==:bcSetRecordId'
        },
        _getByBCSet: {
          method: 'GET',
          url: REST_BASE_URL + '/build-configuration-sets/:configurationSetId/build-records'
        }
      });


      _([['get']]).each(function(e) {
        PncCacheUtil.decorateIndexId(resource, 'BuildRecord', e[0]);
      });

      _([['_getAll'],
         ['_getByBC'],
         ['_getByBCSet'],
         ['_getByBCSetRecord'],
         ['_getByProject'],
         ['_getByUser'],
         ['_getLatestByBC']]).each(function(e) {
        PncCacheUtil.decorate(resource, 'BuildRecord', e[0]);
      });

      _([['_getAll', 'getAll'],
         ['_getByBC', 'getByBC'],
         ['_getByBCSet', 'getByBCSet'],
         ['_getByProject', 'getByProject']]).each(function(e) {
        PageFactory.decorateNonPaged(resource, e[0], e[1]);
      });

      _([['_getAll', 'getAllPaged'],
         ['_getByBC', 'getPagedByBC'],
         ['_getByBCSetRecord', 'getPagedByBCSetRecord'],
         ['_getByUser', 'getPagedByUser']]).each(function(e) {
        PageFactory.decorate(resource, e[0], e[1]);
      });

      resource.prototype.getArtifacts = function() {
        return $injector.get('ArtifactDAO').getByBuildRecord({ recordId: this.id });
      };

      resource.prototype.getUser = function() {
        return $injector.get('UserDAO').get({ userId: this.userId });
      };

      resource.prototype.getBC = function() {
        return $injector.get('BuildConfigurationDAO').get({ configurationId: this.buildConfigurationId });
        //.then(function(e) { throw 'fooo'; });
      };

      return resource;
    }

  ]);


})();
