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

  var module = angular.module('pnc.common.restclient');

  module.value('MILESTONE_ENDPOINT', '/product-milestones/:milestoneId');

  /**
   * @author Alex Creasy
   * @author Jakub Senko
   */
  module.factory('ProductMilestoneDAO', [
    '$resource',
    'REST_BASE_URL',
    'MILESTONE_ENDPOINT',
    'PageFactory',
    function($resource, REST_BASE_URL, MILESTONE_ENDPOINT, PageFactory) {
      var ENDPOINT = REST_BASE_URL + MILESTONE_ENDPOINT;

      var resource = $resource(ENDPOINT, {
        milestoneId: '@id'
      },{
        _getAll: {
          method: 'GET',
          isArray: false
        },
        update: {
          method: 'PUT'
        },
        _getByProductVersion: {
          method: 'GET',
          url: REST_BASE_URL + '/product-milestones/product-versions/:versionId'
        },
        save: {
          method: 'POST'
        }
      });

      PageFactory.decorateNonPaged(resource, '_getAll', 'query');
      PageFactory.decorateNonPaged(resource, '_getByProductVersion', 'getAllForProductVersion');

      PageFactory.decorate(resource, '_getByProductVersion', 'getPagedByProductVersion');

      return resource;
    }
  ]);

})();
