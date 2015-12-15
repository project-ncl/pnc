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

  var module = angular.module('pnc.common.loader');

  /**
   * Load an html snippet after a promise is resolved and put the loaded data into scope.data variable.
   * Automatically show loading spinner icon and failure icon.
   * Can be nested, however, because of the way angular compilation works,
   * html snippet inside the outermost <pnc-loader> tag must be commented out.
   *
   * Example:
   * <pnc-loader loader="record.getBC()">
   *   <!--
   *     <pnc-loader loader="data.getProject()">
   *       {{ data.name }}
   *     </pnc-loader>
   *   -->
   * </pnc-loader>
   *
   * @author Jakub Senko
   */
  module.directive('pncLoader', [
    '$log',
    '$q',
    '$compile',
    '$timeout',
    function ($log, $q, $compile, $timeout) {
      /* jshint unused: false */
      return {
        restrict: 'E',//terminal: true,
        templateUrl: 'common/loader/pnc-loader.html',
        transclude: true,
        scope: {
          loader: '&'
        },
        link: function(scope, element, attrs, ctrl, transclude) {

          var content = '';

          /**
           * Prepare the content inside comment tags for $compile.
           * Mostly consists of moving the comment tags around.
           */
          var parse = function(data) {
            // Remove comment tags
            data = data.trim();
            if(data.indexOf('<!--') !== 0 || data.indexOf('-->') !== data.length - 3) {
              $log.error('Entire code inside <pnc-loader> must be commented out (except nested): ', data);
            }
            data = data.replace(/^<!--/, '');
            data = data.replace(/-->$/, '');
            // Comment the nested loader contents (if any)
            data = data.replace(/(<pnc-loader[^>]*>)/, function(e) { return e + '<!--'; });
            data = data.replace(/(<\/pnc-loader>)(.|[\r\n])*?$/, function(e) { return '-->' + e; });
            // Make sure that angular expressions ({{ foo }}) are inside
            // some tag, or wrap them around <span> (or else the compiler complains).
            data = data.replace(/^([^<]*)({{.*?}})/g, function(e) {
              return e.replace(/({{.*?}})/g, function(e) { return '<span>' + e + '</span>'; });
            });
            data = data.replace(/({{.*?}})([^<]*)$/g, function(e) {
              return e.replace(/({{.*?}})/g, function(e) { return '<span>' + e + '</span>'; });
            });
            return data.trim();
          };

          transclude(function(clone) {
            // Get the content inside <pnc-loader> tag.
            // It must be commented out, because angular would compile it before we get a chance to get the original.
            // Save it so it can be dynamically added after the loader resolves.
            content = angular.element('<div/>').append(clone).html();
          });

          $q.when(scope.loader()).then(function(data) {
            // compile and add the nested content
            $timeout(function() {
              scope.data = data;
              scope.loaded = true;
              element.append($compile(parse(content))(scope));
            });
          }).catch(function(error) {
            $log.error('Could not load <pnc-loader>' + content + '</pnc-loader> because: ', error);
            element.append('<span class="fa fa-exclamation-triangle" title="Sorry, could not load the data." style="color: #ec7a08;"></span>');
            throw error;
          });
        }
      };
    }
  ]);
})();
