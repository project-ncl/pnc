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

 var pnc = pnc || {}; // jshint ignore: line

(function(pnc) {
  'use strict';

  var DEFAULT_CONFIG = {
    pncUrl: '/pnc-rest/rest',
    pncNotificationsUrl: 'ws://' + window.location.host + '/pnc-rest/ws/build-records/notifications'
  };

  /**
   * Entrypoint to the application. Initializes the UI with the supplied
   * configuration.
   *
   * @param config - An object containing configuration paramaters for the UI.
   */
  function bootstrap(config) {

    if (!config) {
      console.warn('No UI configuration provided: using defaults');
      config = DEFAULT_CONFIG;
    }

    console.info('Starting UI with configuration: ' + JSON.stringify(config, null, 2));

    angular.element(document).ready(function () {
      var keycloak;
      var kcInitParams = {
        onLoad: 'check-sso',
        responseMode: 'query'
      };

      // In case PNC is running with authentication disabled we give the
      // keycloak library just enough paramaters to function. That way the UI
      // can run as normal without further modification.
      if (!config.keycloak || !config.keycloak.url) {
        config.keycloak = {
          url: 'none',
          clientId: 'none',
          realm: 'none'
        };
        // Stops the keycloak library trying to "phone home" after loading,
        // since there is no server to call.
        kcInitParams = undefined;
      }

      keycloak = new Keycloak(config.keycloak);

      // Prevents redirect to a 404 when attempting to login and
      // authentication is disabled on the PNC backend.
      if(config.keycloak.url === 'none') {
        keycloak.login = function () {
          console.warn('Authentication is disabled, keycloak.login ignored');
        };
      }

      // Pass the keycloak object we just created to the keycloakProvider so
      // it can be injected into angular services.
      angular.module('pnc').config([
        'keycloakProvider',
        function(keycloakProvider) {
          keycloakProvider.setKeycloak(keycloak);
        }
      ]);

      // Makes the configuration injectible in services.
      angular.module('pnc.properties', []).constant('pncProperties', config);

      keycloak.init(kcInitParams).success(function () {
        angular.bootstrap(document, ['pnc']);
      });
    });
  }

  bootstrap(pnc.config);

})(pnc);
