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

  // Toast notifications to be delivered to the user after UI bootstraps.
  // Push either a string or an object with the following properties, to the desired array:
  //
  // message [String]:
  //    The message to be displayed to the user
  // actionTitle [String (optional)]:
  //    The link text of the notification's action link if required.
  // actionCallback [Func (Required if actionTitle is specified)]:
  //    A callback function that will be executed when the action link is clicked
  // menuActions [Array] (optional):
  //    An array of objects with properties actionTitle and actionCallback as above
  //    These will be displayed on a kebab menu if present.
  var onBootNotifications = {
    error: [],
    warn: [],
    info: [],
    success: []
  };

  var DEFAULT_CONFIG = {
    isDefaultConfiguration: true,
    pncUrl: '/pnc-rest/rest',
    pncNotificationsUrl: 'ws://' + window.location.host + '/pnc-rest/ws/build-records/notifications'
  };

  /**
   * Returns truthy value when Keycloak is enabled, otherwise falsy.
   *
   * @param config - An object containing configuration parameters for the UI.
   */
  function isKeycloakEnabled(config) {
    return config && config.keycloak && config.keycloak.url;
  }

  /**
   * Entrypoint to the application. Initializes the UI with the supplied
   * configuration.
   *
   * @param config - An object containing configuration parameters for the UI.
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

      // Instantiate Keycloak when enabled, otherwise provide mock
      keycloak = isKeycloakEnabled(config) ? new Keycloak(config.keycloak) : {
        // Keycloak mock
        authenticated: false,
        login: function() {
          console.warn('Authentication is disabled, keycloak.login() ignored');
        },
        logout: function() {
          console.warn('Authentication is disabled, keycloak.logout() ignored');
        }
      };

      // Pass the keycloak object we just created to the keycloakProvider so
      // it can be injected into angular services.
      angular.module('pnc').config([
        'keycloakProvider',
        function(keycloakProvider) {
          keycloakProvider.setKeycloak(keycloak);
        }
      ]);

      // Makes the configuration injectible in services and allows UI to
      //properly display notifications from bootstrap to the user.
      angular.module('pnc.properties', [])
             .constant('pncProperties', config)
             .constant('onBootNotifications', onBootNotifications);

      // Bootstrap application
      if (isKeycloakEnabled(config)) {
        keycloak.init(kcInitParams).success(function () {
          angular.bootstrap(document, ['pnc']);
        });

      } else {
        angular.bootstrap(document, ['pnc']);
      }

    });
  }

  /**
   * A best practice is to load the JavaScript adapter directly from Keycloak Server
   * as it will automatically be updated when you upgrade the server.
   *
   * @param onload - Callback function being fired when keycloak is loaded or not available.
   * @param config - An object containing configuration parameters for the UI.
   */
  function loadServerKeycloak(onload, config) {
    if (isKeycloakEnabled(config)) {
      var handleKeycloakLoadFailure = function() {
        console.warn('Unable to load keycloak.js, authentication will be disabled');
        onBootNotifications.warn.push({
          message: 'Unable to load keycloak.js, authentication disabled. See the user guide for more information',
          actionTitle: 'User Guide',
          actionCallback: function () {
            window.open(config.userGuideUrl, '_blank');
          }
        });
        config.keycloak = false;
        onload(config);
      };
      var SERVER_KEYCLOAK_PATH = '/js/keycloak.js',
          script = document.createElement('script');

      script.onload = function() { onload(config); };
      script.addEventListener('error', handleKeycloakLoadFailure);
      script.src = config.keycloak.url + SERVER_KEYCLOAK_PATH;
      document.head.appendChild(script);
    } else {
      onload(config);
    }
  }

  loadServerKeycloak(bootstrap, pnc.config);

})(pnc);
