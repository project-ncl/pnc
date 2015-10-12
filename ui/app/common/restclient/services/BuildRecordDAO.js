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
   *
   */
  module.factory('BuildRecordDAO', [
    '$resource',
    'cachedGetter',
    'REST_BASE_URL',
    'BUILD_RECORD_ENDPOINT',
    'BuildConfigurationDAO',
    'UserDAO',
    'PageFactory',
    'QueryHelper',
    function($resource, cachedGetter, REST_BASE_URL, BUILD_RECORD_ENDPOINT,
             BuildConfigurationDAO, UserDAO, PageFactory, qh) {
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
        _getArtifacts: {
          method: 'GET',
          url: ENDPOINT + '/artifacts'
        },
        _getByConfiguration: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records/build-configurations/:configurationId' +
            qh.searchOnly(['buildConfigurationAudited.name'])
        },
        _getByUser: {
          method: 'GET',
          url: ENDPOINT + '/?q=user.id==:userId'
        },
        _getAllForProject: {
          method: 'GET',
          url: REST_BASE_URL + 'record/projects/:projectId'
        },
        _getLatestForConfiguration: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records/build-configurations/:configurationId?pageIndex=0&pageSize=1&sort==desc=id'
        },
        getAuditedBuildConfiguration: {
          method: 'GET',
          url: ENDPOINT + '/build-configuration-audited'
        },
        _getByBCSetRecord: {
          method: 'GET',
          url: REST_BASE_URL + '/build-records' +
          '?q=' + qh.search(['buildConfigSetRecord.buildConfigurationSet.name']) +
          ';buildConfigSetRecord.id==:bcSetRecordId'
        }
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getArtifacts', 'getArtifacts');
      PageFactory.decorateNonPaged(resource, '_getByConfiguration', 'getByConfiguration');
      PageFactory.decorateNonPaged(resource, '_getAllForProject', 'getAllForProject');
      PageFactory.decorateNonPaged(resource, '_getLatestForConfiguration', 'getLatestForConfiguration');

      PageFactory.decorate(resource, '_getAll', 'getPaged');
      PageFactory.decorate(resource, '_getByConfiguration', 'getPagedByConfiguration');
      PageFactory.decorate(resource, '_getByBCSetRecord', 'getPagedByBCSetRecord');
      PageFactory.decorate(resource, '_getByUser', 'getPagedByUser');

      resource.prototype.getBuildConfiguration = cachedGetter(
        function(buildRecord) {
          return BuildConfigurationDAO.get({ configurationId: buildRecord.buildConfigurationId });
        }
      );

      resource.prototype.getUser = cachedGetter(
        function(record) {
          return UserDAO.get({ userId: record.userId });
        }
      );

      return resource;
    }

  ]);


})();
