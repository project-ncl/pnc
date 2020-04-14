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
   * Use with pnc-inline-notification component to easily post patternfly
   * inline notifications.
   *
   * Example:
   *
   * -- HTML --
   * <pnc-inline-notification name="createPage"></pnc-inline-notification>
   *
   * -- JS --
   * var notify = notifyInline('createPage'); // Where createPage is the name param from above
   *
   * notify({
   *   type: 'success',     // Valid options: success | info | warning | danger
   *   header: 'Success',
   *   message: 'Widget succesfully created',
   *   persistent: true     // Is notification closeable by the user.
   * });
   */
  angular.module('pnc.common.components').factory('notifyInline', [
    function () {
      var components = {};

      function registerComponent(name, notifyFn) {
        components[name] = notifyFn;
      }

      function notify(component, type, header, message, isPersistent) {
        components[component](type, header, message, isPersistent);
      }

      function createNotifier(name) {
        return function (args) {
          notify(name, args.type, args.header, args.message, args.persistent);
        };
      }

      createNotifier.registerComponent = registerComponent;

      return createNotifier;
    }
  ]);


})();
