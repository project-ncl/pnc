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
'use strict';

(function () {

  var module = angular.module('pnc.common.notifications');

  module.factory('pncNotify', ['$log', 'Notifications', function($log, Notifications) {
      var drawerNotifications = [];

      function logHelper(func, message, actionTitle, actionCallback, menuActions) {
       var persistent = false;

       // second parameter is header, fourth parameter is closeCallback
       func(message, undefined, persistent, undefined, actionTitle, actionCallback, menuActions);

       var lastItemIndex = Notifications.data.length - 1;
       var latestNotification = Notifications.data[lastItemIndex];
       latestNotification.unread = true;
       latestNotification.timeStamp = (new Date()).getTime();
       drawerNotifications.push(latestNotification);
      }

      return {
         info: function(message, actionTitle, actionCallback, menuActions) {
           logHelper(Notifications.info, message, actionTitle, actionCallback, menuActions);
         },
         success: function(message, actionTitle, actionCallback, menuActions) {
           logHelper(Notifications.success, message, actionTitle, actionCallback, menuActions);
         },
         error: function(message, actionTitle, actionCallback, menuActions) {
           logHelper(Notifications.error, message, actionTitle, actionCallback, menuActions);
         },
         warn: function(message, actionTitle, actionCallback, menuActions) {
           logHelper(Notifications.warn, message, actionTitle, actionCallback, menuActions);
         },
         toastNotifications: function() {
           return Notifications.data;
         },
         drawerNotifications: function() {
           return drawerNotifications;
         },
         clearDrawerNotifications: function() {
           drawerNotifications = [];
         },
         remove: function(data) {
           Notifications.remove(data);

           // find index of data in drawerNotifications
           var index = drawerNotifications.indexOf(data);

           // remove that notification in the drawer
           if (index > -1) {
             drawerNotifications[index].unread = false;
           }
         }
      };
   }]);
})();
