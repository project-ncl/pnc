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

  angular.module('pnc.dashboard').component('pncGrafanaWidget', {
    bindings: {
      widgetType: '@'
    },
    templateUrl: 'dashboard/widgets/pnc-grafana-widget/pnc-grafana-widget.html',
    controller: ['pncProperties', Controller]
  });

  function Controller(pncProperties) {
    const $ctrl = this;

    const widgetTypes = {
      TRAFFIC_LIGHTS: {
        title: 'Service Status',
        url:  pncProperties.grafana.trafficLightsUrl
      },
      STATUS_MAP: {
        title: 'Service Status Timeline',
        url: pncProperties.grafana.statusMapUrl
      }
    };


    // -- Controller API --



    // --------------------


    $ctrl.$onInit = () => {
      if (!widgetTypes[$ctrl.widgetType]) {
        throw new Error(`<pnc-grafana-widget>: Invalid property for binding 'widget-type': '${$ctrl.widgetType}', valid types are: ${Object.keys(widgetTypes)}`);
      }
      $ctrl.title = getWidgetProperty('title');
      $ctrl.url = getWidgetProperty('url');
    };


    function getWidgetProperty(property) {
      return widgetTypes[$ctrl.widgetType][property];
    }
  }

})();
