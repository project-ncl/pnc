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
(function() {
  'use strict';

  angular.module('pnc.builds').component('pncLogStreamer', {
    bindings: {
      build: '<'
    },
    templateUrl: 'builds/directives/pnc-log-streamer/pnc-log-streamer.html',
    controller: ['pncProperties', Controller]
  });

  function Controller(pncProperties) {
    const $ctrl = this;

      // -- Controller API --

      // --------------------

      $ctrl.$onInit = () => {
        const bifrostUrl = new URL(pncProperties.externalBifrostUrl);
        $ctrl.bifrostHost = bifrostUrl.host;
        $ctrl.prefixFilters = 'loggerName.keyword:org.jboss.pnc._userlog_';
        $ctrl.matchFilters = `mdc.processContext.keyword:build-${$ctrl.build.id}`;
      };
  }
})();
