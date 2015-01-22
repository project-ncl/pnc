'use strict';

(function () {
  var pncui = angular.module('pncui');
  
  var baseUrl = '/pnc-web/rest';

  pncui.factory('Product', ['$resource',
    function($resource) {
      return $resource(baseUrl + '/product/:productId');
    }
  ]);

  pncui.factory('Version', ['$resource', 
    function($resource) {
      return $resource(baseUrl + '/product/:productId/version/:versionId');
    }
  ]);

  pncui.factory('Project', ['$resource', 
    function($resource) {
      return $resource(baseUrl + '/product/{productId}/version/{versionId}/project/:projectId');
    }
  ]);

  pncui.factory('Configuration', ['$resource', 
    function($resource) {
      return $resource(baseUrl + '/product/{productId}/version/:versionId/project/:projectId');
    }
  ]);


})();