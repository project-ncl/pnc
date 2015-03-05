'use strict';

(function() {

  var module = angular.module('pnc.remote.restClient', ['ngResource']);

  module.constant('REST_DEFAULTS', {
    'BASE_URL': '/pnc-web/rest'
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

      return {
        Product: $resource(REST_DEFAULTS.BASE_URL + '/product/:productId', {
          productId: '@id'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Version: $resource(REST_DEFAULTS.BASE_URL + '/product/:productId/version/:versionId', {
          versionId: '@id',
          productId: '@productId'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Project: $resource(REST_DEFAULTS.BASE_URL + '/project/:projectId', {
          projectId: '@id'
        },{
          update: {
            method: 'PUT',
          },
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL +
            '/project/product/:productId/version/:versionId',
            isArray: true,
          }
        }),

        Environment: $resource(REST_DEFAULTS.BASE_URL + '/environment/:environmentId', {
          environmentId: '@id'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Configuration: $resource(REST_DEFAULTS.BASE_URL + '/configuration/:configurationId', {
          configurationId: '@id'
        },{
          update: {
            method: 'PUT'
          },
          clone: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/configuration/:configurationId/clone',
            isArray: false,
          },
          build: {
            method: 'POST',
            url: REST_DEFAULTS.BASE_URL + '/configuration/:configurationId/build',
            isArray: false,
          },
          getAllForProduct: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/configuration/product/:productId',
            isArray: true,
          },
          getAllForProductVersion: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL +
            '/configuration/product/:productId/version/:versionId',
            isArray: true,
          },
          getAllForProject: {
           method: 'GET',
           url: REST_DEFAULTS.BASE_URL + '/configuration/project/:projectId',
           isArray: true,
          },
        }),

        Record: $resource(REST_DEFAULTS.BASE_URL + '/record/:recordId', {}, {
          getLog: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/record/:recordId/log',
            isArray: false
          },
          getAllForConfiguration: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/record/configuration/:configurationId',
            isArray: true,
          },
          getAllForProject: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + 'record/project/:projectId',
            isArray: true
          }
        }),

        Running: $resource(REST_DEFAULTS.BASE_URL + '/record/running/:recordId', {}, {
          getLog: {
            method: 'GET',
            url: REST_DEFAULTS.BASE_URL + '/record/running/:recordId/log',
            isArray: false
          }
        })
      };
    }
  ]);

})();
