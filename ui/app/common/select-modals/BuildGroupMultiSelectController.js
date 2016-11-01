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

  angular.module('pnc.common.select-modals').controller('BuildGroupMultiSelectController', [
    '$log',
    'modalConfig',
    'BuildConfigurationSet',
    'rsqlQuery',
    function ($log, modalConfig, BuildConfigurationSet, rsqlQuery) {
      var ctrl = this;

      console.log('modalConfig == %O', modalConfig);
      ctrl.title = modalConfig.title;
      ctrl.selected = angular.copy(modalConfig.selected);
      ctrl.removed = [];

      function addedIndexOf(item) {
        return ctrl.selected.findIndex(function (x) {
          return x.id === item.id;
        });
      }

      function removedIndexOf(item) {
        return ctrl.removed.findIndex(function (x) {
          return x.id === item.id;
        });
      }

      ctrl.addGroup = function (group) {
        var addedIndex = addedIndexOf(group);
        var removedIndex = removedIndexOf(group);

        // Prevent adding duplicates
        if (addedIndex > -1) {
          return;
        }

        if (removedIndex > -1) {
          ctrl.removed.splice(removedIndex, 1);
        }

        ctrl.selected.push(group);
      };

      ctrl.removeGroup = function (group) {
        ctrl.selected.splice(addedIndexOf(group), 1);
        ctrl.removed.push(group);
      };

      ctrl.save = function () {
        ctrl.$close(ctrl.selected);
      };

      ctrl.close = function () {
        ctrl.$dismiss();
      };

      ctrl.onSelect = function ($item) {
        ctrl.addGroup($item);
        ctrl.input = undefined;
      };

      ctrl.fetchGroups = function ($viewValue) {

        function getId(group) {
          return group.id;
        }

        var outIds = ctrl.selected.map(getId);
        var inIds = ctrl.removed.map(getId);

        var q = rsqlQuery().where('name').like($viewValue + '%').and().where('id').out(outIds)
            .and().brackets(
                rsqlQuery().where('productVersion').isNull().or().where('id').in(inIds).end()
            ).end();

        return BuildConfigurationSet.query( {
            q: q
        }).$promise.then(function (page) {
          return page.data;
        });
      };

      ctrl.config = {
       selectItems: false,
       multiSelect: false,
       dblClick: false,
       selectionMatchProp: 'id',
       showSelectBox: false,
      };

      ctrl.actionButtons = [
        {
          name: 'Remove',
          title: 'Remove this Build Group',
          actionFn: function (action, object) {
            if (action.name === 'Remove') {
              ctrl.removeGroup(object);
            }
          }
        }
      ];
    }
  ]);

})();
