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
(function () {
  'use strict';

  var module = angular.module('pnc.common.pnc-client.resources');

  /**
   *
   * @author Dennis Zhou
   */
  module.factory('GenericSetting', [
    '$http',
    'restConfig',
    function ($http, restConfig) {
      var ENDPOINT = restConfig.getPncUrl() + '/generic-setting';
      return {
        inMaintenanceMode: function () {
          return $http.get(ENDPOINT + '/in-maintenance-mode');
        },
        activateMaintenanceMode: function (reason) {
          return $http.post(ENDPOINT + '/activate-maintenance-mode', reason);
        },
        deactivateMaintenanceMode: function () {
          return $http.post(ENDPOINT + '/deactivate-maintenance-mode');
        },
        getAnnouncementBanner: function () {
          return $http.get(ENDPOINT + '/announcement-banner');
        }
      };
    }

  ]);

})();