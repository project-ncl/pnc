'use strict';

(function () {
  var module = angular.module('pnc.BuildConfig');
  
  var baseUrl = 'pnc-web/rest';

  module.factory('Product', ['$resource',
    function($resource) {
      return $resource(baseUrl + '/product/:productId');
    }
  ]);

  module.factory('Version', ['$resource', 
    function($resource) {
      return $resource(baseUrl + '/product/:productId/version/:versionId', {
        productId: '@productId',
        versionId: '@versionId'
      });
    }
  ]);

  module.factory('Project', ['$resource', 
    function($resource) {
      return $resource(baseUrl + '/product/{productId}/version/{versionId}/project/:projectId');
    }
  ]);

  module.factory('Configuration', ['$resource', 
    function($resource) {
      return $resource(baseUrl + '/product/{productId}/version/:versionId/project/:projectId');
    }
  ]);


})();