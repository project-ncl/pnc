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

  var module = angular.module('pnc.remote.restClient', ['ngResource']);

  module.constant('REST_DEFAULTS', {
    'BASE_URL': '/pnc-rest/rest'
  });

  /**
   * @ngdoc service
   * @name pnc.remote.restClient:PncRestClient
   * @description
   * Client service for PNC REST API.
   * @author Alex Creasy
   *
   */
  module.factory('PncRestClient', [
    '$resource',
    'REST_DEFAULTS',
    function($resource, REST_DEFAULTS) {

      function convertStringResponseToJson(data) {
        // ng-resource expects a JSON object or array of JSON objects,
        // however the rest end point returns a raw string, we need to
        // convert it to an object.
        var response = {};
        response.payload = data;
        return response;
      }

      return {
        Product: $resource(REST_DEFAULTS.BASE_URL + '/products/:productId', {
          productId: '@id'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Version: $resource(REST_DEFAULTS.BASE_URL + '/products/:productId/product-versions/:versionId', {
          versionId: '@id',
          productId: '@productId'
        },{
          update: {
            method: 'PUT',
          },
          getAllBuildConfigurationSets: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/products/:productId/product-versions/:versionId/build-configuration-sets',
            isArray: true
          },
        }),

        Milestone: $resource(REST_DEFAULTS.BASE_URL + '/product-milestones/:milestoneId', {
          milestoneId: '@id'
        },{
          update: {
            method: 'PUT',
          },
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/product-milestones/product-versions/:versionId',
            isArray: true
          },
          saveForProductVersion: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/product-milestones/product-versions/:versionId'
          }
        }),

        Release: $resource(REST_DEFAULTS.BASE_URL + '/product-releases/:releaseId', {
          releaseId: '@id'
        },{
          update: {
            method: 'PUT',
          },
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/product-releases/product-versions/:versionId',
            isArray: true
          },
          saveForProductVersion: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/product-releases/product-versions/:versionId'
          },
          getAllSupportLevel: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/product-releases/support-level',
            isArray: true
          }
        }),

        Project: $resource(REST_DEFAULTS.BASE_URL + '/projects/:projectId', {
          projectId: '@id'
        },{
          update: {
            method: 'PUT',
          },
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL +
            '/projects/products/:productId/product-versions/:versionId',
            isArray: true,
          }
        }),

        Environment: $resource(REST_DEFAULTS.BASE_URL + '/environments/:environmentId', {
          environmentId: '@id'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Configuration: $resource(REST_DEFAULTS.BASE_URL + '/build-configurations/:configurationId', {
          configurationId: '@id'
        },{
          update: {
            method: 'PUT'
          },
          clone: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/build-configurations/:configurationId/clone',
            isArray: false,
          },
          build: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/build-configurations/:configurationId/build',
            isArray: false,
          },
          getAllForProduct: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-configurations/products/:productId',
            isArray: true,
          },
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL +
            '/build-configurations/products/:productId/product-versions/:versionId',
            isArray: true,
          },
          getAllForProject: {
           method: 'GET',
           url: REST_DEFAULTS.BASE_URL + '/build-configurations/projects/:projectId',
           isArray: true,
          },
        }),

        Record: $resource(REST_DEFAULTS.BASE_URL + '/build-records/:recordId', {
          recordId: '@id'
        }, {
          getLog: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-records/:recordId/log',
            isArray: false,
            transformResponse: convertStringResponseToJson
          },
          getArtifacts: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-records/:recordId/artifacts',
            isArray: true,
          },
          getAllForConfiguration: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-records/build-configurations/:configurationId',
            isArray: true,
          },
          getAllForProject: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + 'record/projects/:projectId',
            isArray: true,
          },
          getLatestForConfiguration: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-records/build-configurations/:configurationId?pageIndex=0&pageSize=1&sort==desc=id',
            isArray: true,
          },
        }),

        Running: $resource(REST_DEFAULTS.BASE_URL + '/running-build-records/:recordId', {
          recordId: '@id'
        }, {
          getLog: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/running-build-records/:recordId/log',
            isArray: false,
            transformResponse: convertStringResponseToJson
          },
        }),
        ConfigurationSet: $resource(REST_DEFAULTS.BASE_URL + '/build-configuration-sets/:configurationSetId', {
          'configurationSetId': '@id'
        },{
          update: {
            method: 'PUT'
          },
          getConfigurations: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-configuration-sets/:configurationSetId/build-configurations',
            isArray: true
          },
          build: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/build-configuration-sets/:configurationSetId/build',
            isArray: false,
          },
          removeConfiguration: {
            method: 'DELETE',
            url: REST_DEFAULTS.BASE_URL + '/build-configuration-sets/:configurationSetId/build-configurations/:configurationId',
            isArray: false,
          },
          addConfiguration: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/build-configuration-sets/:configurationSetId/build-configurations',
            isArray: false,
          },
          getRecords: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-configuration-sets/:configurationSetId/build-records',
            isArray: true,
          },
        }),

        RecordSet: $resource(REST_DEFAULTS.BASE_URL + '/build-record-sets/:recordsetId', {
          recordsetId: '@id'
        },{
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-record-sets/product-versions/:versionId',
            isArray: true
          },
          getRecords: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/build-record-sets/build-records/:recordId',
            isArray: true
          }
        })
      };
    }
  ]);

})();

