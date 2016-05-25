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

  /**
   * @author Jakub Senko
   * @author Alex Creasy
   */
  module.factory('ProductImportDAO', [
    '$http',
    'restConfig',
    function ($http, restConfig) {

      var BCG_PATH = '/build-configuration/generate/product';
      var API_URL = restConfig.getDaImportUrl() +  BCG_PATH;

      var resource = {};

      resource.startProcess = function (data) {
        return $http.post(API_URL + '/start-process', data).then(function (r) {
          return r.data;
        });
      };

      resource.analyzeNextLevel = function (data) {
        return $http.post(API_URL + '/analyse-next-level', data).then(function (r) {
          return r.data;
        });
      };

      resource.finishProcess = function (data) {
        return $http.post(API_URL + '/finish-process', data).then(function (r) {
          return r.data;
        });
      };

      return resource;
    }
  ]);

})();
