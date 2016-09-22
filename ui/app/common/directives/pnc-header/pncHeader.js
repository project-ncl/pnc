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

(function() {

  var module = angular.module('pnc.common.directives');

  /**
   * @ngdoc directive
   * @name pnc.common.directives:pncHeader
   * @author Alex Creasy
   * @restrict E
   * @description
   * This directive is used to create a standard pnc header with left
   * aligned heading text with a right aligned toolbar.
   * @example
   * <pnc-header>
   *   <pnc-header-title>Build Configurations</pnc-header-title>
   *   <pnc-header-buttons>
   *     <button ng-click="ctrl.create()">create</button>
   *     <button ng-click="ctrl.update()">update</button>
   *     <button ng-click="ctrl.delete()">delete</button>
   *   </pnc-header-buttons>
   * <pnc-header>
   */
  module.directive('pncHeader', function() {
    return {
      restrict: 'E',
      templateUrl: 'common/directives/pnc-header/pnc-header.html',
      transclude: true,
      link: function(scope, element, attrs, ctrl, transclude) {
        var subHeader = angular.isDefined(attrs.subHeader);

        transclude(scope.$new(), function(clone) {
          element.find('.header-title').append(clone.filter('pnc-header-title'));
        });

        transclude(scope.$new(), function(clone) {
          element.find('.btn-group').append(clone.filter('pnc-header-buttons'));
          element.find('pnc-header-buttons').addClass('btn-group');
          element.find('pnc-header-buttons').find('button').removeClass('btn-lg btn-sm').addClass('btn-lg');
        });

        if (subHeader) {
          element.find('h1').replaceWith(function () {
            return '<h3>' + angular.element(this).html() + '</h3>';
          });

          element.find('pnc-header-buttons').find('button').removeClass('btn-lg btn-sm').addClass('btn');
        }
      }
    };
  });

})();
