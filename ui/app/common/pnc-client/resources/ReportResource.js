/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
(function () {
  'use strict';

  var module = angular.module('pnc.common.pnc-client.resources');

  module.factory('ReportResource', [
    '$http',
    'restConfig',
    function($http, restConfig) {
      var ENDPOINT = restConfig.getDaUrl();

      var resource = {};

      resource.getWhitelistProducts = function () {
        return $http.get(ENDPOINT + '/listings/whitelist/products').then(function (r) {
          return r.data;
        });
      };

      resource.getWhitelistProductArtifacts = function (product) {
        return $http.get(ENDPOINT + '/listings/whitelist/artifacts/product', {
          params: {
            name: product.name,
            version: product.version
          }
        }).then(function (r) {
          return r.data;
        });
      };

      resource.getProductsByGAV = function (groupId, artifactId, version) {
        return $http.get(ENDPOINT + '/listings/whitelist/artifacts/gav', {
          params: {
            groupid: groupId,
            artifactid: artifactId,
            version: version
          }
        }).then(function (r) {
          return r.data;
        });
      };

      resource.getBlacklistedArtifactsInProject = function (scmUrl, revision, pomPath, additionalRepos) {
        return $http.post(ENDPOINT  + '/reports/align', {
          products: [],
          searchUnknownProducts: false,
          scmUrl: scmUrl,
          revision: revision,
          pomPath: pomPath,
          additionalRepos: additionalRepos
        }).then(function (r) {
          return r.data.blacklisted;
        });
      };

      resource.getDifferentArtifactsInProducts = function (product1, product2) {
        return $http.get(ENDPOINT + '/products/diff', {
          params: {
            leftProduct: product1.id,
            rightProduct: product2.id
          }
        }).then(function (r) {
          return r.data;
        });
      };

      resource.getBuiltArtifactsInProject = function (scmUrl, revision, pomPath, additionalRepos) {
        return $http.post(ENDPOINT + '/reports/built', {
          scmUrl: scmUrl,
          revision: revision,
          pomPath: pomPath,
          additionalRepos: additionalRepos
        }).then(function (r) {
          return r.data;
        });
      };

      resource.diffProjectProduct = function (data) {
        return $http.post(ENDPOINT + '/reports/align', data).then(function (r) {
          return r.data;
        });
      };

      return resource;
    }

  ]);

})();
