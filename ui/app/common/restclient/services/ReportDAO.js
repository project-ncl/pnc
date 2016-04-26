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
  module.factory('ReportDAO', [
    '$http',
    'Configuration',
    function ($http, Configuration) {

      var resource = {};

      resource.getWhitelistProducts = function () {
        return Configuration.then(function (config) {
          return $http.get(config.dependencyAnalyzerReportsURL + config.daReportsWhitelistProductsEndpoint);
        }).then(function (r) {
          return r.data;
        });
      };

      resource.getWhitelistProductArtifacts = function (product) {
        return Configuration.then(function (config) {
          var reportEndpoint = config.daReportsWhitelistProductArtifactsEndpoint.replace(':productName', product.name).replace(':productVersion', product.version);
          return $http.get(config.dependencyAnalyzerReportsURL + reportEndpoint);
        }).then(function (r) {
          return r.data;
        });
      };

      resource.getProductsByGAV = function (groupId, artifactId, version) {
        return Configuration.then(function (config) {
          return $http.get(config.dependencyAnalyzerReportsURL + '/v-0.4/listings/whitelist/artifacts/gav', {
            params: {
              groupid:    groupId,
              artifactid: artifactId,
              version:    version
            }
          });
        }).then(function (r) {
          return r.data;
        });
      };

      return resource;
    }
  ]);

})();
