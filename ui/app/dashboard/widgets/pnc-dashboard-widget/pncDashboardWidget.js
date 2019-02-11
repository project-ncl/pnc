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

(function () {
  'use strict';

  angular.module('pnc.dashboard').component('pncDashboardWidget', {
    transclude: {
      'title': 'widgetTitle',
      'body': 'widgetBody'
    },
    templateUrl: 'dashboard/widgets/pnc-dashboard-widget/pnc-dashboard-widget.html',
    controller: ['$window', '$element', Controller]
  });

  function Controller($window, $element) {
    const $ctrl = this;

    const onResizeFns = [];

    let resizeInProgress,
        panel;


    // -- Controller API --

    $ctrl.registerOnResize = registerOnResize;


    // --------------------


    $ctrl.$onInit = () => {
      panel = $element[0].firstElementChild;
    };

    $ctrl.$postLink = () => {
      $window.addEventListener('resize', onResize);
    };

    $ctrl.$onDestroy = () => {
      $window.removeEventListener('resize', onResize);
    };

    function registerOnResize(callbackFn) {
      onResizeFns.push(callbackFn);
      onResize();
    }

    function onResize() {
      if (resizeInProgress) {
        return;
      }

      resizeInProgress = true;

      $window.requestAnimationFrame(() => {
        onResizeFns.forEach(callbackFn => callbackFn(getSize()));
        resizeInProgress = false;
      });
    }

    function getSize() {
      return {
        width: panel.clientWidth
      }
    }

  }

})();
