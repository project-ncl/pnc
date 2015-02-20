'use strict';

(function () {
  var module = angular.module('pnc.BuildConfig');

  var baseUrl = '/pnc-web/rest';

  module.factory('PncRestClient', ['$resource',
    function($resource) {
      return {
        Product: $resource(baseUrl + '/product/:productId', {
            productId: '@productId'
        }),

        Version: $resource(baseUrl + '/product/:productId/version/:versionId', {
            productId: '@productId',
            versionId: '@versionId'
        }),

        Project: $resource(baseUrl + '/project/:projectId', {
            projectId: '@projectId',
          },{
            getForProductVersion: {
              method: 'GET',
              url: baseUrl +
                '/project/product/:productId/version/:versionId',
              isArray: true,
              params: {
                productId: '@productId',
                versionId: '@versionId'
              }
            }
          }),

        Configuration: $resource(baseUrl + '/configuration/:configurationId', {
            configurationId: '@configurationId'
          },{
            update: {
              method: 'PUT',
              params: {
                configurationId: '@id'
              }
            },
            build: {
              method: 'POST',
              url: '/configuration/:configurationId/build ',
              isArray: false,
              params: {
                configurationId: '@configurationId'
              }
            },
            getForProject: {
               method: 'GET',
               url: baseUrl + '/configuration/project/:projectId',
               isArray: true,
               params: {
                 projectId: '@projectId'
               }
            },
            getForProduct: {
               method: 'GET',
               url: baseUrl + '/configuration/product/:productId',
               isArray: true,
               params: {
                 productId: '@productId'
               }
            },
            getForProductVersion: {
               method: 'GET',
               url: baseUrl + '/configuration/product/:productId/version/:versionId',
               isArray: true,
               params: {
                 productId: '@productId',
                 versionId: '@versionId'
               }
            }
        })
      };
    }
  ]);

  // module.factory('Product', ['$resource',
  //   function($resource) {
  //     return $resource(baseUrl + '/product/:productId');
  //   }
  // ]);

  // module.factory('Version', ['$resource',
  //   function($resource) {
  //     return $resource(baseUrl + '/product/:productId/version/:versionId', {
  //       productId: '@productId',
  //       versionId: '@versionId'
  //     });
  //   }
  // ]);

  // module.factory('Project', ['$resource',
  //   function($resource) {
  //     return $resource(baseUrl + '/project/:projectId');
  //   }
  // ]);

  // module.factory('Configuration', ['$resource',
  //   function($resource) {
  //     return $resource(baseUrl +
  //       'project/:projectId/configuration/:configurationId');
  //   }
  // ]);

})();
