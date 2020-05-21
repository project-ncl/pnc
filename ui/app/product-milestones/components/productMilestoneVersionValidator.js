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

    var module = angular.module('pnc.product-milestones');

    module.directive('productMilestoneVersionValidator',['$timeout', '$q', 'ProductMilestoneResource', function ($timeout, $q, ProductMilestoneResource) {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                errorMessages: '='
            },
            link: function (scope, element, attrs, ngModel) {

                let pendingValidation;
                
                ngModel.$asyncValidators.productMilestoneVersionValidator = function (modelValue, viewValue) {
                    let deferred = $q.defer();

                    if (pendingValidation) {
                        $timeout.cancel(pendingValidation);
                    }

                    pendingValidation = $timeout(() => {
                        let milestoneVersion = modelValue || viewValue,
                            productVersionId = attrs.productVersionId,
                            productVersion = attrs.productVersion;

                        if (milestoneVersion && productVersionId && productVersion) {
                
                            ProductMilestoneResource.validateVersion({
                                productVersionId: productVersionId,
                                version: productVersion + '.' + milestoneVersion
                            }).$promise.then((response) => {
                                if (response.isValid) {
                                    deferred.resolve();
                                } else {
                                    scope.errorMessages = response.hints;
                                    deferred.reject();
                                }
                            }).catch(() => {
                                scope.errorMessages = ['An unexpected error occurred, please try again later'];
                                deferred.reject();
                            });
                        }

                        return deferred.promise;
                    }, 500);

                    return pendingValidation;
                };
            }
        };
    }
    ]);

}());
