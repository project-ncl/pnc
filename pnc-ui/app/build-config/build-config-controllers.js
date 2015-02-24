'use strict';

(function() {

  var module = angular.module('pnc.BuildConfig');

  module.controller('BuildConfigController',
    ['$scope', '$state', 'PncRestClient',
    function($scope, $state, PncRestClient) {

      // TODO: Refactor into service.

      /* Creates new column object for use with the column-browse-column
       * directive the column browse UI element directive. The object will
       * be wired up so as to automatically keep child and parent objects
       * in sync.
       *
       * param: navigateFn - a function which should navigate to the
       *        desired UI state when an item in the column is clicked.
       *        The function will be passed the clicked item as the sole
       *        parameter.
       *
       * param: updateListFn - a function that should return the list of
       *         items to display.
       *
       * param: parent - the column's parent column.
       */
      var newColumn = function(navigateFn, updateListFn, parent) {
        var columnPrototype = {
          parent: null,
          child: null,
          list: [],
          selected: null,

          setSelected: function(item) {
            var that = this;
            (function() {
              that.selected = item;
              if (that.child) {
                that.child.updateList();
              }
            })();
          },

          clearSelected: function() {
            var that = this;
            (function() {
              that.selected = null;
              if (that.child) {
                that.child.clearSelected();
              }
            })();
          },

          clearLists: function() {
            var that = this;
            (function() {
              that.list = null;
              if (that.child) {
                that.child.clearLists();
              }
            })();
          }
        };

        var newCol = Object.create(columnPrototype);

        if (parent) {
          newCol.parent = parent;
          parent.child = newCol;
        }

        newCol.click = function(clickedItem) {
          console.log('Click >> clickedItem: %O', clickedItem);
          // newCol.selected = clickedItem;
          // if (newCol.child) {
          //   newCol.child.updateList();
          // }
          navigateFn(clickedItem);
        };

        newCol.updateList = function() {
          newCol.clearSelected();
          newCol.clearLists();
          newCol.list = updateListFn();
        };

        return newCol;
      };

      /*
       * Create columns for GUI.
       */

      var productCol = newColumn(
        function(product) {
          $state.go('build-config.product.show', {
            productId: product.id });
        },
        function() {
          return PncRestClient.Product.query();
        }
      );

      var versionCol = newColumn(
        function(version) {
          console.log('versionCol >> selected: %O', version);
            $state.go('build-config.product.show.version.show', {
              versionId: version.id,
              productId: productCol.selected.id });
        },
        function() {
          console.log('versionCol.updateList >> productCol = %O', productCol);
          return PncRestClient.Version.query({ productId:
            productCol.selected.id });
        },
        productCol
      );

      var projectCol = newColumn(
        function(project) {
          console.log('projectCol >> selected: %O', project);
          $state.go('build-config.product.show.version.show.project.show', {
            projectId: project.id,
            versionId: versionCol.selected.id,
            productId: versionCol.parent.selected.id });
        },
        function() {
          console.log('projectCol.updateList >> versionCol = %O', versionCol);
          return PncRestClient.Project.getAllForProductVersion({
            productId: versionCol.parent.selected.id,
            versionId: versionCol.selected.id,
          });
        },
        versionCol
      );

      var configurationCol = newColumn(
        function(buildConfig) {
          console.log('configurationCol >> selected: %O', buildConfig);
          $state.go('build-config.product.show.version.show.project.show.configuration.show', {
            configurationId: buildConfig.id,
            projectId: projectCol.selected.id,
            versionId: projectCol.parent.selected.id,
            productId: projectCol.parent.parent.selected.id });
        },
        function() {
          console.log('configurationCol.updateList >> projectCol = %O', projectCol);
          return PncRestClient.Configuration.getAllForProject({
            projectId: projectCol.selected.id
          });
        },
        projectCol
      );

      // Add columns to scope so can be accessed in the view and
      // from inherriting controllers.
      $scope.columnBrowse = {
        products: productCol,
        versions: versionCol,
        projects: projectCol,
        configurations: configurationCol
      };

      // Initialise the first column with values.
      $scope.columnBrowse.products.updateList();
    }
  ]);

  module.controller('ProductListController',
    ['$scope','productList',
    function($scope, productList) {
      console.log('ProductListController >> scope=%O, productList=%O', $scope, productList);
      $scope.products = productList;
    }
  ]);

  module.controller('ProductShowController', ['$scope', '$stateParams', 'productDetails',
    function ($scope, $stateParams, productDetails) {
      console.log('ProductShowController::productDetails=%O', productDetails);
      $scope.product = productDetails;
      $scope.columnBrowse.products.setSelected(productDetails);
    }
  ]);

  module.controller('VersionListController',
    ['$scope', '$stateParams', 'versionList',
    function ($scope, $stateParams, versionList) {
      console.log('VersionListController::versionList=%O', versionList);
      console.log('VersionListController::$stateParams=%O', $stateParams);
      $scope.versions = versionList;
    }
  ]);

  module.controller('VersionShowController',
    ['$scope', '$stateParams', '$state', 'productDetails', 'versionDetails',
    function ($scope, $stateParams, $state, productDetails, versionDetails) {
      console.log('VersionController::versionDetails=%O', versionDetails);
      console.log('VersionController::$stateParams=%O', $stateParams);
      console.log('VersionController::$state=%O', $state);
      console.log('VersionController::$scope=%O', $scope);
      $scope.product = productDetails;
      $scope.version = versionDetails;
      $scope.columnBrowse.products.setSelected(productDetails);
      $scope.columnBrowse.versions.setSelected(versionDetails);
    }
  ]);

  module.controller('ProjectListController',
    ['$scope', '$stateParams', 'projectList',
    function ($scope, $stateParams, projectList) {
      console.log('ProjectListController::versionList=%O', projectList);
      console.log('ProjectListController::$stateParams=%O', $stateParams);
      $scope.projects = projectList;
    }
  ]);

  module.controller('ProjectShowController',
    ['$scope',
    '$stateParams',
    '$state',
    'projectDetails',
    'versionDetails',
    'productDetails',
    function($scope, $stateParams, $state, projectDetails, versionDetails, productDetails) {

      $scope.project = projectDetails;
      $scope.version = versionDetails;
      $scope.product = productDetails;

      $scope.columnBrowse.products.setSelected(productDetails);
      $scope.columnBrowse.versions.setSelected(versionDetails);
      $scope.columnBrowse.projects.setSelected(projectDetails);
    }
  ]);

  module.controller('ConfigurationListController',
    ['$scope', '$state', 'configurationList',
    function($scope, $state, configurationList) {
      console.log('ConfigurationListController >> scope=%O, configurationList=%O', $scope, configurationList);
      $scope.configurations = configurationList;
      $scope.showConfiguration = function(configuration) {
        $state.go('build-config.configuration.show', {
          configurationId: configuration.id
        });
      };
    }
  ]);

  module.controller('ConfigurationShowController', [
    '$scope',
    '$stateParams',
    '$state',
    'PncRestClient',
    'projectDetails',
    'environmentDetails',
    'configurationDetails',
    function($scope, $stateParams, $state, PncRestClient, projectDetails,
             environmentDetails, configurationDetails) {
      $scope.project = projectDetails;
      $scope.environment = environmentDetails;
      $scope.buildConfig = configurationDetails;

      if ($scope.columnBrowse) {
         $scope.columnBrowse.configurations.setSelected(configurationDetails);
      }

      $scope.updateConfiguration = function() {
        console.log('form update: %O', $scope.buildConfig);
        var error;
        $scope.buildConfig.$update().then(
          // Success
          function(result) {
            console.log(result);
            $scope.configForm.setAlert(alertStates.success);
          },
          // Failure
          function(response) {
            console.log(response);
            $scope.configForm.setAlert(alertStates.failure);
            error = 'error';
          }
        );
        return error;
      };

      $scope.cloneConfig = function() {
        configurationDetails.$clone().then(function() {
          $state.go('build-config.product.show.version.show.project.show.' +
                    'configuration.show');
        });
      };


      var alertStates = { none: 0, success: 1, failure: 2 };
      var alert = alertStates.none;

      $scope.configForm = {};
      $scope.configForm.cancelAlert = function() {
        alert = alertStates.none;
      };
      $scope.configForm.hasAlert = function() {
        return alert !== alertStates.none;
      };
      $scope.configForm.showFailure = function() {
        return alert === alertStates.failure;
      };
      $scope.configForm.showSuccess = function() {
        return alert === alertStates.success;
      };
      $scope.configForm.setAlert = function(status) {
        alert = status;
      };

      $scope.deleteConfig = function() {
        $scope.buildConfig.$delete().then(
          // Success
          function (result) {
            console.log('Successfully deteled: result=%O', result);
          },
          // Failure
          function (response) {
            console.log('Failed to delete: response=%O', response);
          }
        );
      };
    }
  ]);

  module.controller('ConfigurationCreateController',
    ['$scope', '$state', 'PncRestClient', 'environments', 'projects',
      function($scope, $state, PncRestClient, environments, projects) {
        $scope.createConfigForm = {};
        $scope.createConfigForm.data = new PncRestClient.Configuration();
        $scope.createConfigForm.environments = environments;
        $scope.createConfigForm.projects = projects;

        $scope.createConfigForm.submit = function() {
          console.log('FORM SUBMITTED: %O', $scope.createConfigForm.data);
          $scope.createConfigForm.data.$save().then(
            function(result) {
              console.log('SUCCESS: %O', result);
              console.log('result.id='+result.id);
              $state.go('build-config.configuration.show', {
                configurationId: result.id
              });
            },
            function(response) {
              console.log('ERROR: response: %O', response);
            }
          );
        };
      }
    ]
  );
})();
