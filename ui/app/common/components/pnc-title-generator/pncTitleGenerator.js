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

  var TITLE_SUFFIX = ' | Project Newcastle';
  var DEFAULT_TILE = 'Project Newcastle';

  angular.module('pnc.common.components').directive('pncTitleGenerator', [
    '$q',
    '$transitions',
    '$interpolate',
    function ($q, $transitions, $interpolate) {
      return {
        restrict: 'A',
        link: function(scope, elem) {
          if (elem[0].tagName !== 'TITLE') {
            throw new Error('pnc-title-generator directive can only be attached to the <title> tag');
          }

          function setTitle(title) {
            elem[0].textContent = title;
          }
          
          function generateTitle(transition) {
            var titleTemplate = transition.to().data.title;

            if (!angular.isString(titleTemplate)) {
              setTitle(DEFAULT_TILE);
              return;
            }

            var context = {};

            transition.getResolveTokens().forEach(function (token) {
              context[token] = transition.injector().get(token);
            });

            setTitle($interpolate(titleTemplate)(context) + TITLE_SUFFIX);
          }

          $transitions.onSuccess({}, generateTitle);
        }
      };
    }
  ]);

})();
