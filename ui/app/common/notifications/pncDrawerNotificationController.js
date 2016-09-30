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

  module.controller('pncDrawerNotificationController', ['$scope', '$log', 'pncNotify', function($scope, $log, pncNotify) {

      $scope.groups = [
          {
            heading: 'Events',
            open: true,
            notifications: []
          }
      ];

      $scope.hideDrawer = true;

      $scope.toggleShowDrawer = function () {
        $scope.hideDrawer = !$scope.hideDrawer;

        if (!$scope.hideDrawer) {
          // make sure we update the notifications every time we open the drawer
          $scope.groups[0].notifications = pncNotify.drawerNotifications();
        }
      };

      $scope.hasDrawerNotifications = function() {
        return pncNotify.drawerNotifications().length !== 0;
      };

      $scope.markAllAsRead = function(group) {
        group.notifications.forEach(function(nextNotification) {
          nextNotification.unread = false;
        });
      };

      $scope.customScope = {};

      $scope.customScope.getNotficationStatusIconClass = function (notification) {
        var retClass = '';
        if (notification && notification.type) {
          if (notification.type === 'info') {
            retClass = 'pficon pficon-info';
          } else if (notification.type === 'danger') {
            retClass = 'pficon pficon-error-circle-o';
          } else if (notification.type === 'warning') {
            retClass = 'pficon pficon-warning-triangle-o';
          } else if (notification.type === 'success') {
            retClass = 'pficon pficon-ok';
          }
        }
        return retClass;
      };

      $scope.customScope.markRead = function(notification) {
        notification.unread = false;
      };

      $scope.customScope.clearAll = function (group) {
        group.notifications = [];
        pncNotify.clearDrawerNotifications();
      };

  }]);
})();
