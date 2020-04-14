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

  /**
   * Used for displaying inline notifications, see the complementary notifyInline service.
   * This is a thin abstraction over the patternfly pf-inline-notification component
   * that, in conjunction with the notifyInline service, removes the majority of
   * the boilerplate code required to use it.
   */
  angular.module('pnc.common.components').component('pncInlineNotification', {
    bindings: {
      /**
       * String: The name of the topic for this component. This is used with the
       * notifyInline service to display a specific inline notification.
       */
      name: '@'
    },
    templateUrl: 'common/components/pnc-inline-notification/pnc-inline-notification.html',
    controller: ['$log', '$scope', 'notifyInline', Controller]
  });

  function Controller($log, $scope, notifyInline) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.notification = {
      visible: false,
      type: null,
      message: null,
      header: null,
      persistent: false,
      remove: function () {
        $ctrl.notification.visible = false;
      }
    };

    // --------------------


    $ctrl.$postLink = function () {
      notifyInline.registerComponent($ctrl.name, notify);
    };

    function notify(type, header, message, isPersistent) {
      $log.debug('Inline Notification: %O', arguments);
      $scope.$applyAsync(function () {
        $ctrl.notification.type = type;
        $ctrl.notification.header = header;
        $ctrl.notification.message = message;
        $ctrl.notification.persistent = isPersistent;
        $ctrl.notification.visible = true;
      });
    }
  }
})();
