/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

  var module = angular.module('pnc.import');

  /**
   * @author Jakub Senko
   */
  module.directive('createProjectModal', [
    'ProjectDAO',
    'Notifications',
    function (ProjectDAO, Notifications) {
      return {
        restrict: 'E',
        templateUrl: 'import/product/directive/CreateProjectModal/create-project-modal.html',
        scope: {},
        link: function (scope) {
          scope.data = {};

          scope.submit = function () {
            if (scope.projectForm.$valid) {
              new ProjectDAO(_(scope.data).clone()).$save().then(function (result) {
                if (_(result).has('errorMessage')) {
                  Notifications.error(result.errorMessage);
                } else {
                  Notifications.success('Project \'' + result.name + '\' created.');
                  $('#projectCreateModal').modal('hide');
                  scope.$emit('projectCreated', {project: result});
                }
              });
            } else {
              _(scope.projectForm).each(function (field) {
                if (_(field).has('$dirty') && field.$pristine) {
                  field.$dirty = true;
                }
              });
            }
          };
        }
      };
    }
  ]);

})();
