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

(function () {
  'use strict';

  angular.module('pnc.common.directives').component('pncProjectTypeahead', {
    bindings: {
      onSelect: '&',
      input: '=?ngModel'
    },
    require: {
      ngModel: '?ngModel'
    },
    templateUrl: 'common/directives/pnc-project-typeahead/pnc-project-typeahead.html',
    controller: ['$scope', 'Project', 'rsqlQuery', Controller]
  });

  function Controller($scope, Project, rsqlQuery) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.search = search;
    $ctrl.select = select;
    $ctrl.setDirty = setDirty;
    $ctrl.setTouched = setTouched;

    // --------------------


    $ctrl.$onInit = function () {
      if ($ctrl.ngModel) {
        $ctrl.ngModel.$validators.isValidProject = function (modelValue, viewValue) {
          return !viewValue || (angular.isObject(modelValue) && modelValue.id);
        };
      }
    };

    function search($viewValue) {
      var q;

      q = rsqlQuery().where('name').like($viewValue + '%').end();

      return Project.query({ q: q }).$promise.then(function (page) { return page.data; });
    }

    function select($item) {
      $ctrl.onSelect()($item);
    }

    function setDirty() {
      if ($ctrl.ngModel) {
        $ctrl.ngModel.$setDirty();
      }
    }

    function setTouched() {
      if ($ctrl.ngModel) {
        $ctrl.ngModel.$setTouched();
      }
    }

  }

})();
