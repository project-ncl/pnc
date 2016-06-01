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
    'restConfig',
    function ($http, restConfig) {

      var DA_URL = restConfig.getDaUrl();
      var WHITELIST_PRODUCT_ENDPOINT = DA_URL + '/listings/whitelist/products';
      var WHITELIST_PRODUCT_ARTIFACTS_ENDPOINT = DA_URL + '/listings/whitelist/artifacts/product';
      var PRODUCTS_BY_GAV_ENDPOINT = DA_URL + '/listings/whitelist/artifacts/gav';
      var DA_REPORTS_ALIGN = DA_URL + '/reports/align';
      var PRODUCTS_ARTIFACTS_DIFFERENCE_ENDPOINT = DA_URL + '/products/diff';
      var DA_REPORTS_BUILT = DA_URL + '/reports/built';

      var resource = {};

      resource.getWhitelistProducts = function () {
        return $http.get(WHITELIST_PRODUCT_ENDPOINT).then(function (r) {
          return r.data;
        });
      };

      resource.getWhitelistProductArtifacts = function (product) {
        return $http.get(WHITELIST_PRODUCT_ARTIFACTS_ENDPOINT, {
          params: {
            name: product.name,
            version: product.version
          }
        }).then(function (r) {
          return r.data;
        });
      };

      resource.getProductsByGAV = function (groupId, artifactId, version) {
          return $http.get(PRODUCTS_BY_GAV_ENDPOINT, {
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
        return $http.post(DA_REPORTS_ALIGN, {
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
          return $http.get(PRODUCTS_ARTIFACTS_DIFFERENCE_ENDPOINT, {
            params: {
              leftProduct: product1.id,
              rightProduct: product2.id
            }
          }).then(function (r) {
            return r.data;
          });
      };


      resource.getBuiltArtifactsInProject = function (scmUrl, revision, pomPath, additionalRepos) {
        return $http.post(DA_REPORTS_BUILT, {
          scmUrl: scmUrl,
          revision: revision,
          pomPath: pomPath,
          additionalRepos: additionalRepos
        }).then(function (r) {
          return r.data;
        });
      };

      resource.diffProjectProduct = function (data) {
        return Configuration.then(function (config) {
          return $http.post(DA_REPORTS_ALIGN, data);
        }).then(function (r) {
          return r.data;
        });
      };

      return resource;
    }
  ]);

})();
