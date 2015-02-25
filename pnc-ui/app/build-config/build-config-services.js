'use strict';

(function () {
  var module = angular.module('pnc.BuildConfig');

  var DEFAULT_BASE_URL = '/pnc-web/rest';

  module.factory('PncRestClient', ['$resource',
    function($resource) {

      var baseUrl = DEFAULT_BASE_URL;

      return {
        getBaseUrl: function() {
          return baseUrl;
        },
        setBaseUrl: function(url) {
          baseUrl = url;
        },

        Product: $resource(baseUrl + '/product/:productId', {
            productId: '@id'
        },{
          update: {
              method: 'PUT',
          }
        }),

        Version: $resource(baseUrl + '/product/:productId/version/:versionId', {
          versionId: '@id',
          productId: '@productId'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Project: $resource(baseUrl + '/project/:projectId', {
          projectId: '@id'
        },{
          update: {
            method: 'PUT',
          },
          getAllForProductVersion: {
            method: 'GET',
            url: baseUrl +
              '/project/product/:productId/version/:versionId',
            isArray: true,
          }
        }),

        Environment: $resource(baseUrl + '/environment/:environmentId', {
          environmentId: '@id'
        },{
          update: {
            method: 'PUT',
          }
        }),

        Configuration: $resource(baseUrl + '/configuration/:configurationId', {
            configurationId: '@id'
          },{
            update: {
              method: 'PUT'
            },
            clone: {
              method: 'POST',
              url: baseUrl + '/configuration/:configurationId/clone',
              isArray: false,
            },
            build: {
              method: 'POST',
              url: baseUrl + '/configuration/:configurationId/build',
              isArray: false,
            },
            getAllForProduct: {
              method: 'GET',
              url: baseUrl + '/configuration/product/:productId',
              isArray: true,
            },
            getAllForProductVersion: {
              method: 'GET',
              url: baseUrl +
                '/configuration/product/:productId/version/:versionId',
              isArray: true,
            },
            getAllForProject: {
               method: 'GET',
               url: baseUrl + '/configuration/project/:projectId',
               isArray: true,
            },
        })
      };
    }
  ]);

})();
