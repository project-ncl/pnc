/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

(function() {
    var app = angular.module('pnc');

    app.controller('authenticationController', ['authService', function(authService) {
        this.username = authService.getPrinciple();
        this.isAuthenticated = authService.isAuthenticated;
        this.logout = authService.logout;
        this.login = authService.login;
    }]);

    app.controller('menuController', ['$state', '$scope', function($state, $scope) {
      $scope.state = $state;
    }]);

    app.controller('defaultConfigurationController', ['pncProperties', function(pncProperties) {

        var SESSION_STORAGE_NAME = 'defaultConfigurationModal';
        
        this.isDefaultConfiguration = pncProperties.isDefaultConfiguration;
        this.title = 'No UI configuration provided';

        if (pncProperties.isDefaultConfiguration && sessionStorage.getItem(SESSION_STORAGE_NAME) === null) {
            $('#defaultConfigurationModal').modal('show');
        }

        $('#defaultConfigurationModal').on('hidden.bs.modal', function() {
          sessionStorage.setItem(SESSION_STORAGE_NAME, 'hidden');
        });

    }]);

    app.controller('userGuideController', ['pncProperties', function(pncProperties) {
      var LOCAL_STORAGE_NAME = 'userGuidePopover';
      var $userGuideLink = $('#user-guide .user-guide-link');

      this.url = pncProperties.userGuideUrl;

      if (this.url && localStorage.getItem(LOCAL_STORAGE_NAME) === null) {
        $userGuideLink.popover('show');

        $userGuideLink.add('#user-guide .user-guide-close').click(function() {
          localStorage.setItem(LOCAL_STORAGE_NAME, 'displayed');
          $userGuideLink.popover('destroy');
        });
      }

    }]);

})();
