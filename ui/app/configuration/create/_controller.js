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

  var module = angular.module('pnc.configuration');

  module.controller('ConfigurationCreateController', [
    '$state',
    '$log',
    '$filter',
    'PncRestClient',
    'Notifications',
    'environments',
    'projects',
    'products',
    'configurations',
    function($state, $log, $filter, PncRestClient, Notifications, environments,
             projects, products, configurations) {

      var that = this;

      this.data = new PncRestClient.Configuration();
      this.environments = environments;
      this.projects = projects;


      this.submit = function() {
        // The REST API takes integer Ids so we need to extract them from
        // our collection of objects first and attach them to our data object
        // for sending back to the server.
        that.data.productVersionIds = gatherIds(that.productVersions.selected);
        that.data.dependencyIds = gatherIds(that.dependencies.selected);

        that.data.$save().then(
          function(result) {
            Notifications.success('Configuration created');
            $state.go('configuration.detail', {
              configurationId: result.id
            });
          },
          function(response) {
            $log.error('Create configuration failed: response: %O', response);
            Notifications.error('Configuration creation failed');
          }
        );
      };


      // Filtering and selection of linked ProductVersions.
      this.products = {
        all: products,
        selected: null
      };

      this.productVersions = {
        selected: [],
        all: [],

        update: function() {
          that.productVersions.all = PncRestClient.Product.getVersions({
            productId: that.products.selected.id
          });
        },
        getItems: function($viewValue) {
          return $filter('filter')(that.productVersions.all, {
            version: $viewValue
          });
        }
      };

     // Selection of dependencies.
      this.dependencies = {
        selected: [],

        getItems: function($viewValue) {
          return $filter('filter')(configurations, {
            name: $viewValue
          });
        }
      };
    }
  ]);

  function gatherIds(array) {
    var result = [];
    for (var i = 0; i < array.length; i++) {
      result.push(array[i].id);
    }
    return result;
  }

})();
