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

  var module = angular.module('pnc.common.pnc-client');

  /**
   * Provider for configuration of the rest client services.
   */
  module.provider('restConfig', function() {

    // Set the defaults initially, these can be overriden by injecting the
    // provider and invoking the corresponding mutator methods.
    var pncUrl = '/pnc-rest/rest';
    var pncRestUrl = '/pnc-rest-new/rest-new';
    var pncNotificationsUrl = 'ws://' + window.location.host + '/pnc-rest/ws/build-records/notifications';
    var daUrl;
    var kafkaStoreUrl;

    this.setPncUrl = function (url) {
      pncUrl = url;
    };

    this.setPncRestUrl = function (url) {
      pncRestUrl = url;
    };

    this.setPncNotificationsUrl = function (url) {
      pncNotificationsUrl = url;
    };

    this.setDaUrl = function (url) {
      daUrl = url;
    };

    this.setKafkaStoreUrl = function (url) {
      kafkaStoreUrl = url;
    };

    this.$get = function () {
      var restConfig = {};

      restConfig.getPncUrl = function () {
        return pncUrl;
      };

      restConfig.getPncRestUrl = function () {
        return pncRestUrl;
      };

      restConfig.getPncNotificationsUrl = function () {
        return pncNotificationsUrl;
      };

      restConfig.getDaUrl = function () {
        return daUrl;
      };

      restConfig.getKafkaStoreUrl = function() {
        return kafkaStoreUrl;
      };

      return restConfig;
    };
  });

})();
