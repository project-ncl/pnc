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
"use strict";
'use strict';

angular.module('pnc-ui-extras', ['pnc-ui-extras.templates', 'pnc-ui-extras.combobox', 'pnc-ui-extras.uiBreadcrumbs']);

angular.module('pnc-ui-extras.templates', []);
'use strict';

angular.module('pnc-ui-extras.combobox', ['pnc-ui-extras.templates']);
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var ComboboxController = function () {
  function ComboboxController($log, $scope, $element, $timeout) {
    _classCallCheck(this, ComboboxController);

    this.$log = $log;
    this.$scope = $scope;
    this.$element = $element;
    this.$timeout = $timeout;

    this.options = []; // List of options for the user to select from
    this.showDropDown = false;
    this.modelOptions = {}; // Values for ng-model-options directive
  }

  _createClass(ComboboxController, [{
    key: '$onInit',
    value: function $onInit() {
      var _this = this;

      if (!this.optionTemplateUrl) {
        var DEFAULT_OPTION_TEMPLATE_URL = 'pnc-ui-extras/combobox/combobox-option.template.html';
        this.optionTemplateUrl = DEFAULT_OPTION_TEMPLATE_URL;
      }
      if (this.ngModel) {
        var editable = this.editable === true || this.editable === 'true';

        this.ngModel.$parsers.push(function (viewValue) {
          var option = _this.getOptionFromViewValue(viewValue);

          if (angular.isDefined(option)) {
            return _this.getModelValue(option);
          } else if (editable) {
            return viewValue;
          }
        });

        this.ngModel.$validators.isValidOption = function (modelValue, viewValue) {
          return angular.isDefined(_this.getOptionFromModelValue(modelValue)) || editable;
        };

        this.ngModel.$render = function () {
          return _this.inputModel = _this.ngModel.$viewValue;
        };

        this.ngModel.$formatters.push(function (modelValue) {
          var transformed = _this.getViewValue(modelValue);

          if (angular.isDefined(transformed)) {
            return transformed;
          }

          var option = _this.getOptionFromModelValue(modelValue);

          if (angular.isDefined(option)) {
            return _this.getViewValue(option);
          } else if (editable) {
            return modelValue;
          }
        });

        this.$scope.$watch(function () {
          return _this.inputModel;
        }, function () {
          _this.ngModel.$setViewValue(_this.inputModel);
          _this.loadOptions(_this.inputModel);
        });

        if (this.debounceMs) {
          this.modelOptions.debounce = parseInt(this.debounceMs);
        }
      } else {
        this.loadOptions();
      }
    }
  }, {
    key: '$postLink',
    value: function $postLink() {
      var _this2 = this;

      if (this.ngModel) {
        this.$timeout(function () {
          _this2.$element.find('input').on('blur', function () {
            _this2.$scope.$applyAsync(function () {
              return _this2.ngModel.$setTouched();
            });
          });
        });
      }
    }
  }, {
    key: '$onDestroy',
    value: function $onDestroy() {
      if (this.ngModel) {
        this.$element.find('input').off('blur');
      }
    }
  }, {
    key: 'loadOptions',
    value: function loadOptions(viewValue) {
      var _this3 = this;

      return this.pxExpression.getOptions(viewValue).then(function (options) {
        _this3.$log.debug('ComboboxController::loadOptions() scopeId = %d | options = %O', _this3.$scope.$id, options);
        _this3.$scope.$applyAsync(function () {
          return _this3.options = options;
        });
        return options;
      });
    }
  }, {
    key: 'getViewValue',
    value: function getViewValue(option) {
      return this.pxExpression.getViewValue(option);
    }
  }, {
    key: 'getModelValue',
    value: function getModelValue(option) {
      return this.pxExpression.getModelValue(option);
    }
  }, {
    key: 'getOptionFromViewValue',
    value: function getOptionFromViewValue(viewValue) {
      var _this4 = this;

      if (!angular.isArray(this.options)) {
        return;
      }
      return this.options.find(function (option) {
        return _this4.getViewValue(option) === viewValue;
      });
    }
  }, {
    key: 'getOptionFromModelValue',
    value: function getOptionFromModelValue(modelValue) {
      var _this5 = this;

      if (!angular.isArray(this.options)) {
        return;
      }
      if (this.getViewValue(modelValue)) {
        return modelValue;
      }
      return this.options.find(function (option) {
        return _this5.getModelValue(option) === modelValue;
      });
    }
  }, {
    key: 'setShowDropDown',
    value: function setShowDropDown(value) {
      var _this6 = this;

      this.$scope.$applyAsync(function () {
        return _this6.showDropDown = value;
      });
      if (!value) {
        this.highlighted = undefined;
      }
    }
  }, {
    key: 'openDropDown',
    value: function openDropDown() {
      this.setShowDropDown(true);
    }
  }, {
    key: 'closeDropDown',
    value: function closeDropDown() {
      this.setShowDropDown(false);
    }
  }, {
    key: 'toggleDropDown',
    value: function toggleDropDown() {
      this.setShowDropDown(!this.showDropDown);
    }
  }, {
    key: 'select',
    value: function select(option) {
      this.inputModel = this.getViewValue(option);
      this.closeDropDown();
    }
  }, {
    key: 'clear',
    value: function clear() {
      this.inputModel = undefined;
      this.closeDropDown();
    }
  }, {
    key: 'setHighlighted',
    value: function setHighlighted(index) {
      this.highlighted = index;
    }
  }, {
    key: 'isHighlighted',
    value: function isHighlighted(index) {
      return this.highlighted === index;
    }
  }, {
    key: 'highlightNext',
    value: function highlightNext() {
      if (!this.showDropDown || !this.options || this.options.length < 1) {
        return;
      }

      if (angular.isUndefined(this.highlighted)) {
        this.setHighlighted(0);
      } else if (this.highlighted === this.options.length - 1) {
        return;
      } else {
        this.setHighlighted(this.highlighted + 1);
      }
    }
  }, {
    key: 'highlightPrevious',
    value: function highlightPrevious() {
      if (!this.showDropDown || !this.options || this.options.length < 1) {
        return;
      }

      if (angular.isUndefined(this.highlighted) || this.highlighted === 0) {
        return;
      } else {
        this.setHighlighted(this.highlighted - 1);
      }
    }
  }, {
    key: 'onKey',
    value: function onKey($event) {
      $event.stopPropagation();
      $event.preventDefault();
      switch ($event.key) {
        case 'ArrowDown':
          if (!this.showDropDown) {
            this.openDropDown();
          } else {
            this.highlightNext();
          }
          break;
        case 'ArrowUp':
          this.highlightPrevious();
          break;
        case 'Enter':
          this.select(this.options[this.highlighted]);
          break;
        case 'Escape':
          this.closeDropDown();
          break;
      }
    }
  }]);

  return ComboboxController;
}();

ComboboxController.$inject = ['$log', '$scope', '$element', '$timeout'];

var pxCombobox = {
  templateUrl: 'pnc-ui-extras/combobox/combobox.template.html',
  controller: ComboboxController,
  require: {
    pxExpression: '?pxExpression',
    ngModel: '?ngModel'
  },
  bindings: {
    placeholder: '@',
    editable: '<',
    debounceMs: '@',
    optionTemplateUrl: '@'
  }
};

angular.module('pnc-ui-extras.combobox').component('pxCombobox', pxCombobox);
'use strict';

/**
 * The work in this file is entirely the work of the AngularUI team, with only
 * extremely minor of modifications. Copyright attribution and license:
 *
 * The MIT License
 *
 * Copyright (c) 2012-2017 the AngularUI Team, https://github.com/organizations/angular-ui/teams/291112
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
var pxExpressionParser = function pxExpressionParser($parse) {
  var TYPEAHEAD_REGEXP = /^\s*([\s\S]+?)(?:\s+as\s+([\s\S]+?))?\s+for\s+(?:([\$\w][\$\w\d]*))\s+in\s+([\s\S]+?)$/;
  return {
    parse: function parse(input) {
      var match = input.match(TYPEAHEAD_REGEXP);
      if (!match) {
        throw new Error('Expected typeahead specification in form of "_modelValue_ (as _label_)? for _item_ in _collection_"' + ' but got "' + input + '".');
      }

      return {
        itemName: match[3],
        source: $parse(match[4]),
        viewMapper: $parse(match[2] || match[1]),
        modelMapper: $parse(match[1])
      };
    }
  };
};

pxExpressionParser.$inject = ['$parse'];

angular.module('pnc-ui-extras.combobox').factory('pxExpressionParser', pxExpressionParser);
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var PxExpressionController = function () {
  function PxExpressionController($log, $q, $scope, $attrs, pxExpressionParser) {
    _classCallCheck(this, PxExpressionController);

    this.$log = $log;
    this.$q = $q;
    this.$scope = $scope;
    this.$attrs = $attrs;
    this.pxExpressionParser = pxExpressionParser;

    this.parsed = pxExpressionParser.parse($attrs.pxExpression);

    this.$log.debug('PxExpressionController::constructor() with scope id = ' + $scope.$id);
    this.$log.debug('px-expression = ' + $attrs.pxExpression, $scope);
  }

  _createClass(PxExpressionController, [{
    key: 'getViewValue',
    value: function getViewValue(item) {
      var locals = {};
      locals[this.parsed.itemName] = item;
      return this.parsed.viewMapper(this.$scope, locals);
    }
  }, {
    key: 'getModelValue',
    value: function getModelValue(item) {
      var locals = {};
      locals[this.parsed.itemName] = item;
      return this.parsed.modelMapper(this.$scope, locals);
    }
  }, {
    key: 'getOptions',
    value: function getOptions(viewValue) {
      return this.$q.when(this.parsed.source(this.$scope, { $viewValue: viewValue }));
    }
  }]);

  return PxExpressionController;
}();

PxExpressionController.$inject = ['$log', '$q', '$scope', '$attrs', 'pxExpressionParser'];

var pxExpression = function pxExpression() {
  return {
    restrict: 'A',
    scope: false,
    controller: PxExpressionController
  };
};

angular.module('pnc-ui-extras.combobox').directive('pxExpression', pxExpression);
'use strict';

// jshint ignore:start
//
// CUSTOMIZED IMPLEMENTATION TO MAKE IT COMPATIBLE WITH ui-router 1.0
//
/**
 * uiBreadcrumbs automatic breadcrumbs directive for AngularJS & Angular ui-router.
 *
 * https://github.com/michaelbromley/angularUtils/tree/master/src/directives/uiBreadcrumbs
 *
 * Copyright 2014 Michael Bromley <michael@michaelbromley.co.uk>
 */

(function () {

    /**
     * Config
     */
    var moduleName = 'pnc-ui-extras.uiBreadcrumbs';
    var _templateUrl = 'pnc-ui-extras/ui-breadcrumbs/uiBreadcrumbs.tpl.html';

    /**
     * Module
     */
    var module;
    try {
        module = angular.module(moduleName);
    } catch (err) {
        // named module does not exist, so create one
        module = angular.module(moduleName, ['ui.router']);
    }

    module.directive('uiBreadcrumbs', ['$interpolate', '$state', '$transitions', function ($interpolate, $state, $transitions) {
        return {
            restrict: 'E',
            templateUrl: function templateUrl(elem, attrs) {
                return attrs.templateUrl || _templateUrl;
            },
            scope: {
                displaynameProperty: '@',
                abstractProxyProperty: '@?'
            },
            link: function link(scope) {
                var transition;

                scope.breadcrumbs = [];
                if ($state.$current.name !== '') {
                    updateBreadcrumbsArray();
                }
                $transitions.onSuccess({}, function (_transition) {
                    transition = _transition;
                    updateBreadcrumbsArray();
                });

                /**
                 * Start with the current state and traverse up the path to build the
                 * array of breadcrumbs that can be used in an ng-repeat in the template.
                 */
                function updateBreadcrumbsArray() {
                    var workingState;
                    var displayName;
                    var breadcrumbs = [];
                    var currentState = $state.$current;

                    while (currentState && currentState.name !== '') {
                        workingState = getWorkingState(currentState);
                        if (workingState) {
                            displayName = getDisplayName(workingState);

                            if (displayName !== false && !stateAlreadyInBreadcrumbs(workingState, breadcrumbs)) {
                                breadcrumbs.push({
                                    displayName: displayName,
                                    route: workingState.name
                                });
                            }
                        }
                        currentState = currentState.parent;
                    }
                    breadcrumbs.reverse();
                    scope.breadcrumbs = breadcrumbs;
                }

                /**
                 * Get the state to put in the breadcrumbs array, taking into account that if the current state is abstract,
                 * we need to either substitute it with the state named in the `scope.abstractProxyProperty` property, or
                 * set it to `false` which means this breadcrumb level will be skipped entirely.
                 * @param currentState
                 * @returns {*}
                 */
                function getWorkingState(currentState) {
                    var proxyStateName;
                    var workingState = currentState;
                    if (currentState.abstract === true) {
                        if (typeof scope.abstractProxyProperty !== 'undefined') {
                            proxyStateName = getObjectValue(scope.abstractProxyProperty, currentState);
                            if (proxyStateName) {
                                workingState = angular.copy($state.get(proxyStateName));
                                if (workingState) {
                                    workingState.locals = currentState.locals;
                                }
                            } else {
                                workingState = false;
                            }
                        } else {
                            workingState = false;
                        }
                    }
                    return workingState;
                }

                /**
                 * Resolve the displayName of the specified state. Take the property specified by the `displayname-property`
                 * attribute and look up the corresponding property on the state's config object. The specified string can be interpolated against any resolved
                 * properties on the state config object, by using the usual {{ }} syntax.
                 * @param currentState
                 * @returns {*}
                 */
                function getDisplayName(currentState) {
                    var interpolationContext;
                    var propertyReference;
                    var displayName;

                    if (!scope.displaynameProperty) {
                        // if the displayname-property attribute was not specified, default to the state's name
                        return currentState.name;
                    }
                    propertyReference = getObjectValue(scope.displaynameProperty, currentState);

                    if (propertyReference === false) {
                        return false;
                    } else if (typeof propertyReference === 'undefined') {
                        return currentState.name;
                    } else {
                        // use the $interpolate service to handle any bindings in the propertyReference string.
                        // see https://ui-router.github.io/ng1/docs/latest/classes/transition.transition-1.html#getresolvetokens
                        interpolationContext = {};
                        transition.getResolveTokens().forEach(function (token) {
                            if (angular.isString(token) && !token.startsWith('$')) {
                                interpolationContext[token] = transition.injector().get(token);
                            }
                        });

                        displayName = $interpolate(propertyReference)(interpolationContext);
                        return displayName;
                    }
                }

                /**
                 * Given a string of the type 'object.property.property', traverse the given context (eg the current $state object) and return the
                 * value found at that path.
                 *
                 * @param objectPath
                 * @param context
                 * @returns {*}
                 */
                function getObjectValue(objectPath, context) {
                    var i;
                    var propertyArray = objectPath.split('.');
                    var propertyReference = context;

                    for (i = 0; i < propertyArray.length; i++) {
                        if (angular.isDefined(propertyReference[propertyArray[i]])) {
                            propertyReference = propertyReference[propertyArray[i]];
                        } else {
                            // if the specified property was not found, default to the state's name
                            return undefined;
                        }
                    }
                    return propertyReference;
                }

                /**
                 * Check whether the current `state` has already appeared in the current breadcrumbs array. This check is necessary
                 * when using abstract states that might specify a proxy that is already there in the breadcrumbs.
                 * @param state
                 * @param breadcrumbs
                 * @returns {boolean}
                 */
                function stateAlreadyInBreadcrumbs(state, breadcrumbs) {
                    var i;
                    var alreadyUsed = false;
                    for (i = 0; i < breadcrumbs.length; i++) {
                        if (breadcrumbs[i].route === state.name) {
                            alreadyUsed = true;
                        }
                    }
                    return alreadyUsed;
                }
            }
        };
    }]);
})();
'use strict';

angular.module('pnc-ui-extras.templates').run(['$templateCache', function ($templateCache) {
  $templateCache.put('pnc-ui-extras/combobox/combobox-option.template.html', '<a ng-click="$ctrl.select(option)" href>{{ $ctrl.getViewValue(option) }}</a>\n');
  $templateCache.put('pnc-ui-extras/combobox/combobox.template.html', '<style>\n.px-search-clear {\n  position: absolute;\n  z-index: 100;\n  right: 18px;\n  top: 2px;\n  height: 14px;\n  margin: auto;\n  color: inherit;\n  cursor:  pointer;\n}\n\n.px-search-clear > a:hover {\n  background-color: inherit;\n  color: inherit;\n  cursor:  pointer;\n}\n\n.px-combobox-dropdown {\n  display: block;\n}\n.px-combobox-active a,a:hover {\n  background-color: #def3ff;\n}\n.px-combobox-dropdown > .px-combobox-option a,a:hover {\n  border-width: 0px;\n}\n\n.px-combobox-dropdown > .px-combobobox-option {\n  whitespace: normal !important;\n  overflow-wrap: break-word !important;\n}\n\n</style>\n<div class="combobox-container" ng-keydown="$event.stopPropagation()">\n  <div class="input-group">\n    <input type="text" autocomplete="off" id="combobox-{{::$id}}" ng-keyup="$ctrl.onKey($event)" placeholder="{{ ::$ctrl.placeholder }}" class="combobox form-control" ng-focus="$ctrl.openDropDown()" ng-model="$ctrl.inputModel" ng-model-options="$ctrl.modelOptions" pf-focused="$ctrl.showDropDown">\n    <div class="px-search-clear"><a class="px-search-clear" ng-show="$ctrl.inputModel" ng-click="$ctrl.clear()"><span class="pficon pficon-close"></span></a></div>\n    <ul class="typeahead typeahead-long dropdown-menu px-combobox-dropdown" ng-if="$ctrl.options.length > 0 && $ctrl.showDropDown">\n      <li ng-repeat="option in $ctrl.options" ng-include="$ctrl.optionTemplateUrl" class="px-combobox-option" ng-mouseover="$ctrl.setHighlighted($index)" ng-class="{ \'px-combobox-active\': $ctrl.isHighlighted($index) }">\n      </li>\n      <li data-value="spinner" class="text-center" ng-show="$ctrl.isLoading()">\n        <span class="spinner spinner-xs spinner-inline"></span>\n      </li>\n    </ul>\n    <span class="input-group-addon dropdown-toggle" ng-class="{ \'dropup\': $ctrl.showDropDown }" data-dropdown="dropdown" role="button" ng-click="$ctrl.toggleDropDown()">\n      <span class="caret"></span>\n    </span>\n  </div>\n</div>\n');
  $templateCache.put('pnc-ui-extras/ui-breadcrumbs/uiBreadcrumbs.tpl.html', '<ol class="breadcrumb">\n  <li ng-repeat="crumb in breadcrumbs"\n      ng-class="{ active: $last }"><a ui-sref="{{ crumb.route }}" ng-if="!$last">{{ crumb.displayName }}&nbsp;</a><span ng-show="$last">{{ crumb.displayName }}</span>\n  </li>\n</ol>');
}]);
//# sourceMappingURL=pnc-ui-extras.js.map
