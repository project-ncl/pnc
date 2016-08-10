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

    var module = angular.module('pnc.milestone');

    module.factory('versionFactory', ['ProductMilestoneDAO',

        function (ProductMilestoneDAO) {
            var factory = {};

            factory.checkUniqueValue = function (productVersionId, milestoneVersion, productVersion) {
               return ProductMilestoneDAO.getAllForProductVersion({versionId: productVersionId}).then(
                    function (results) {
                        for (var i =0; i < results.length; i++){
                            var milestone = results[i];
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
