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

    var module = angular.module('pnc.milestone');

    module.factory('versionFactory', ['$http',

        function ($http) {
            var restUrl = '../pnc-rest/rest/product-milestones/product-versions/',
                factory = {};

            factory.checkUniqueValue = function (productVersionId, milestoneVersion, productVersion) {
                return $http.get(restUrl + productVersionId, {cache: true}).then(
                    function (results) {
                        for (var i =0; i < results.data.content.length; i++){
                            var milestone = results.data.content[i];
                            if(milestone.version === productVersion+'.'+milestoneVersion){
                                return false;
                            }
                        }
                        return true;
                    });
            };

            return factory;
        }
    ]);

}());
