/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

    module.directive('versionUnique',['$q','versionFactory',

    function ($q, versionFactory) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                ngModel.$asyncValidators.unique = function (modelValue, viewValue) {
                    var deferred = $q.defer(),
                        milestoneVersion = modelValue || viewValue,
                        productVersionId = attrs.productVersionId,
                        productVersion = attrs.productVersion;
                    if (milestoneVersion && productVersionId && productVersion) {
                        versionFactory.checkUniqueValue(productVersionId, milestoneVersion, productVersion)
                        .then(function (unique) {
                            if (unique) {
                                deferred.resolve();
                            }
                            else {
                                deferred.reject();
                            }
                        });
                    }
                    else {
                        deferred.resolve();
                     }

                    return deferred.promise;
                };
            }
        };
    }
    ]);

}());
