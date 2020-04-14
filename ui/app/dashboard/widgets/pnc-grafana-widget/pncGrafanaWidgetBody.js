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

  angular.module('pnc.dashboard').component('pncGrafanaWidgetBody', {
    bindings: {
      url: '<'
    },
    require: {
      'widget': '^pncDashboardWidget'
    },
    templateUrl: 'dashboard/widgets/pnc-grafana-widget/pnc-grafana-widget-body.html',
    controller: ['$element', '$sce', Controller]
  });

  function Controller($element, $sce) {
    const $ctrl = this;

    let iFrame;


    // -- Controller API --



    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.iFrameUrl = $sce.trustAsResourceUrl($ctrl.url);
    };

    $ctrl.$postLink = () => {
      iFrame = $element[0].firstElementChild;
      $ctrl.widget.registerOnResize(onResize);
    };
    

    function onResize(payload) {
      iFrame.width = payload.width;
    }
  }

})();
