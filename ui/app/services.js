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

  var app = angular.module('pnc');

   app.factory('unwrapPageResponseInterceptor', function() {
     return {
       response: function(response) {
         if(response.data.content && !_.isArray(response.data.content)) {
            response.data = response.data.content;
         }
         return response;
       }
     };
   });

  app.factory('httpResponseInterceptor', [
    '$q',
    '$log',
    'pncNotify',
    'keycloak',
    function($q, $log, pncNotify, keycloak) {

      function defaultSuccessNotification(response) {
        if (response.config.method !== 'GET') {
          $log.debug('HTTP response: %O', response);
          pncNotify.success('Request successful');
        }
      }

      function handleError(rejection) {
        var MAX_NOTIFICATION_LENGTH = 120;

        var error;

        if (rejection && rejection.data && rejection.data.errorMessage) {
          error = rejection.data;
        } else {
          pncNotify.error('PNC REST Api returned an error in an invalid format: ' + rejection.status + ' ' + rejection.statusText);
          $log.error('PNC REST Api returned an error in an invalid format: response: %O', rejection);
          return rejection;
        }

        if (error.errorMessage.length > MAX_NOTIFICATION_LENGTH) {
          error.errorMessage = error.errorMessage.substring(0, MAX_NOTIFICATION_LENGTH -1) + ' ...';
        }

        pncNotify.error(error.errorMessage);
        $log.error('PNC REST API returned the following error: type: "%s", message: "%s", details: "%s"',
            error.errorType, error.errorMessage, error.details);

      }

      return {

        response: function(response) {
          var notify = response.config.successNotification;

          if (angular.isUndefined(notify)) {
            defaultSuccessNotification(response);
          } else if (angular.isFunction(notify)) {
            notify(response);
          } else if (angular.isString(notify)) {
            pncNotify.success(notify);
          }

          return response;
        },

        responseError: function(rejection) {
          switch(rejection.status) {
            case 0:
              pncNotify.error('Unable to connect to server');
              break;
            case 401:
              keycloak.login();
              break;
            case 404:
              pncNotify.error('Requested resource not found');
              break;
            default:
              handleError(rejection);
              break;
          }
          return $q.reject(rejection);
        }

      };
    }
  ]);

})();
