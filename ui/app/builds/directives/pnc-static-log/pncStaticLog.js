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

  angular.module('pnc.builds').component('pncStaticLog', {
    bindings: {
      /**
       * string: Log content
       */
      log: '<'
    },
    templateUrl: 'builds/directives/pnc-static-log/pnc-static-log.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;

    var LOCAL_STORAGE_LOG_WRAP = 'logWrap';
    var LOG_WRAP_DEFAULT = true;
    
    $ctrl.logWrap = getLocalStorageItem(LOCAL_STORAGE_LOG_WRAP);

    // -- Controller API --

    $ctrl.handleModelChange = handleModelChange;

    // --------------------

    function getLocalStorageItem(itemName) {
      var item = localStorage.getItem(itemName);
      if (item === null) {
        return LOG_WRAP_DEFAULT;
      } 
      return JSON.parse(item);
    }

    function handleModelChange() {
      localStorage.setItem(LOCAL_STORAGE_LOG_WRAP, $ctrl.logWrap);
    }

  }

})();
