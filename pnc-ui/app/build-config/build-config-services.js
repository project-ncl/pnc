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
            },
            build: {
              method: 'POST',
              url: '/configuration/:configurationId/build ',
              isArray: false,
              params: {
                configurationId: '@configurationId'
              }
            },
            // getForProject: {
            //   method: 'GET',
            //   url: '/configuration/project/{projectId:int}',
            //   isArray: true,
            //   params: {
            //     projectId: '@projectId'
            //   }
            // },
            // getForProduct: {
            //   method: 'GET',
            //   url: '/configuration/product/{productId:int}',
            //   isArray: true,
            //   params: {
            //     productId: '@productId'
            //   }
            // },
            // getForProductVersion: {
            //   method: 'GET',
            //   url: '/configuration/product/{id}/version/{id}',
            //   isArray: true,
            //   params: {
            //     productId: '@productId',
            //     versionId: '@versionId'
            //   }
            // }
        })
      };
    }
  ]);

  /*
  GET    /configuration                                                                   Gets all current Build Configurations
  POST   /configuration                                                                 Creates new Build Configuration
  GET    /configuration/{id}                                                             Gets a specific Build Configuration
  PUT    /configuration/{id}                                                             Updates an existing Build Configuration
  DELETE /configuration/{id}                                                         Deletes a Build Configuration
  POST   /configuration/{id}/build                                                   Triggers build for a Build Configurations
  POST   /configuration/{id}/clone                                                  Clones a Build Configuration
  GET    /configuration/project/{projectId}                                      Gets all current Build Configurations of a Project
  GET    /configuration/configurationset/{configurationsetId}         Gets all Build Configurations of a Build Configuration Set (see below * )
  GET  /configuration/product/{id}                                                  Gets all current Build Configurations of a Product
  GET  /configuration/product/{id}/version/{id}                               Gets all current Build Configurations of a ProductVersion
*/

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
