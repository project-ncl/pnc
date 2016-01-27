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
   */
  module.factory('ProductImportDAO', [
    '$http',
    'Configuration',
    function ($http, Configuration) {

      var resource = {};

      resource.startProcess = function (data) {
        return Configuration.then(function (config) {
          console.log(config.dependencyAnalyzerURL + '/start-process');
          return $http.post(config.dependencyAnalyzerURL + '/start-process', data);
        }).then(function (r) {
          return r.data;
        });
      };

      resource.analyzeNextLevel = function (data) {
        return Configuration.then(function (config) {
          return $http.post(config.dependencyAnalyzerURL + '/analyse-next-level', data);
        }).then(function (r) {
          return r.data;
        });
      };

      resource.finishProcess = function (data) {
        return Configuration.then(function (config) {
          return $http.post(config.dependencyAnalyzerURL + '/finish-process', data);
        }).then(function (r) {
          return r.data;
        });
      };

      return resource;
    }
  ]);

})();
