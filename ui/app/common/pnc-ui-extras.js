'use strict'; // jshint ignore: start

angular.module('pnc-ui-extras', ['pnc-ui-extras.templates', 'pnc-ui-extras.combobox']);

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
    debounceMs: '@'
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

angular.module('pnc-ui-extras.templates').run(['$templateCache', function ($templateCache) {
  $templateCache.put('pnc-ui-extras/combobox/combobox.template.html', '<style>\n.px-search-clear {\n  position: absolute;\n  z-index: 100;\n  right: 18px;\n  top: 2px;\n  height: 14px;\n  margin: auto;\n  color: inherit;\n  cursor:  pointer;\n}\n\n.px-search-clear > a:hover {\n  background-color: inherit;\n  color: inherit;\n  cursor:  pointer;\n}\n\n.px-combobox-dropdown {\n  display: block;\n}\n.px-combobox-active > a, a:hover {\n  background-color: #def3ff;\n}\n.px-combobox-dropdown > li > a,a:hover {\n  border-width: 0px;\n}\n</style>\n<div class="combobox-container" ng-keydown="$event.stopPropagation()">\n  <div class="input-group">\n    <input type="text" autocomplete="off" id="combobox-{{::$id}}" ng-keyup="$ctrl.onKey($event)" placeholder="{{ ::$ctrl.placeholder }}" class="combobox form-control" ng-focus="$ctrl.openDropDown()" ng-model="$ctrl.inputModel" ng-model-options="$ctrl.modelOptions" pf-focused="$ctrl.showDropDown">\n    <div class="px-search-clear"><a class="px-search-clear" ng-show="$ctrl.inputModel" ng-click="$ctrl.clear()"><span class="pficon pficon-close"></span></a></div>\n    <ul class="typeahead typeahead-long dropdown-menu px-combobox-dropdown" ng-if="$ctrl.options.length > 0 && $ctrl.showDropDown">\n      <li ng-repeat="option in $ctrl.options" ng-mouseover="$ctrl.setHighlighted($index)" ng-class="{ \'px-combobox-active\': $ctrl.isHighlighted($index) }">\n        <a ng-click="$ctrl.select(option)" href>{{ $ctrl.getViewValue(option) }}</a>\n      </li>\n      <li data-value="spinner" class="text-center" ng-show="$ctrl.isLoading()">\n        <span class="spinner spinner-xs spinner-inline"></span>\n      </li>\n    </ul>\n    <span class="input-group-addon dropdown-toggle" ng-class="{ \'dropup\': $ctrl.showDropDown }" data-dropdown="dropdown" role="button" ng-click="$ctrl.toggleDropDown()">\n      <span class="caret"></span>\n    </span>\n  </div>\n</div>\n');
}]);
//# sourceMappingURL=pnc-ui-extras.js.map
