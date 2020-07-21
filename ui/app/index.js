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
if (angular.isUndefined(window.pnc)) {
  window.pnc = {};
}

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
    externalLegacyPncUrl: '/pnc-rest/rest',
    externalPncUrl: '/pnc-rest/rest-new',
    pncNotificationsUrl: 'ws://' + window.location.host + '/pnc-rest/notifications'
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

    /*
     * Override PatternFly sidebar function computing min-height for sidebar and content part.
     * JavaScript is not needed anymore, CSS already provides sufficient and more robust way to do it.
     *
     * https://github.com/patternfly/patternfly/blob/master/src/js/patternfly-functions-sidebar.js
     */
    window.jQuery.fn.sidebar = angular.noop;

    angular.element(document).ready(function () {
      var keycloak;
      var kcInitParams = {
        onLoad: 'check-sso',
        responseMode: 'fragment'
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
          angular.bootstrap(document, ['pnc'], {
            strictDi: true
          });
        });

      } else {
        angular.bootstrap(document, ['pnc'], {
          strictDi: true
        });
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
        onBootNotifications.warn.push({
          message: 'Unable to load Keycloak: authentication disabled, check your certificates are installed properly or visit User Guide FAQ section.',
          actionTitle: 'User Guide',
          actionCallback: function () {
            window.open(config.userGuideUrl, '_blank');
          },
          persistent: true
        });
        onBootNotifications.info.push({
          message: `Unable to load Keycloak: as a workaround, you can visit Keycloak Server directly and reload this page.`,
          actionTitle: 'Keycloak Server',
          actionCallback: function () {
            window.open(script.src, '_blank');
          },
          persistent: true
        });
        config.keycloak = false;
        onload(config);
      };
      var SERVER_KEYCLOAK_PATH = '/js/keycloak.min.js',
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

})(window.pnc);
