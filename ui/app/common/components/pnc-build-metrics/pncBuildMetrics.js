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

  /**
   * The component representing Build Metric charts.
   * 
   * @param {string[]} buildIds - List of Build ids.
   * @param {string} chartType - Possible values: line, horizontalBar
   * @param {string} componentId - Unique component id.
   */
  angular.module('pnc.common.components').component('pncBuildMetrics', {
    bindings: {
      buildIds: '<',
      chartType: '@?',
      componentId: '@?'
    },
    templateUrl: 'common/components/pnc-build-metrics/pnc-build-metrics.html',
    controller: ['BuildRecord', Controller]
  });

  function Controller(BuildRecord) {
    var $ctrl = this;
    var chart = null;
    var canvasElement = null;
    
    // Single Controller level instance is required as Chart is pointing to it and providing later updates based on it's changes.
    var chartConfig = {};

    $ctrl.BUILDS_DISPLAY_LIMIT = 20;
    $ctrl.BUILDS_DISPLAY_LIMIT_EXAMPLE = 5;
   

    $ctrl.navigationOptions = [
      { id: 1,  name:'1st' },
      { id: 2,  name:'2nd' },
      { id: 3,  name:'3rd' },
      { id: 5,  name:'5th' },
      { id: 10, name:'10th'}
    ];
    $ctrl.navigationSelected = $ctrl.navigationOptions[0];


    /**
     * Return color and label for each metric.
     * 
     * Colors are based on patternfly.org/v3/styles/color-palette/
     * 
     * @param {string} metricName - Metric name coming from the REST API
     */
    var adaptMetric = function (metricName) {
      switch (metricName) {

        // light-green
        case 'Enqueued': 
          return { 
            color: '#c8eb79', 
            label: 'Enqueued' 
          };

        // blue
        case 'BUILD_ENV_SETTING_UP': 
          return { 
            color: '#7cdbf3', 
            label: 'Build Env Setting Up' 
          };
        case 'REPO_SETTING_UP': 
          return { 
            color: '#00b9e4',
            label: 'Repo Setting Up' 
          };
        case 'BUILD_SETTING_UP': 
          return { 
            color: '#008bad',
            label: 'Build Setting Up' 
          };

        // purple
        case 'COLLECTING_RESULTS_FROM_BUILD_DRIVER': 
          return { 
            color: '#a18fff',
            label: 'Collecting Results From Build Driver' 
          };
        case 'COLLECTING_RESULTS_FROM_REPOSITORY_MANAGER': 
          return { 
            color: '#703fec',
            label: 'Collecting Results From Repository Manager' 
          };

        // green
        case 'FINALIZING_EXECUTION': 
          return { 
            color: '#3f9c35', 
            label: 'Finalizing Execution' 
          };

        // gray
        default: 
          console.warn('adaptMetric: Unknown metric name: "' + metricName + '"', metricName); 
          return { 
            color: 'gray', 
            label: metricName 
          };
      }
    };

    
    var generateTimeTitle = function(metricValue) { 
      // Chart.js converts null values to NaN string
      if (metricValue === null || metricValue === 'NaN') {
        return 'Not Available';
      }

      var HOUR_MS = 3600000;
      var MINUTE_MS = 60000;
      var SECOND_MS = 1000;

      var time = {
        milliseconds: metricValue % SECOND_MS,
        seconds: Math.floor((metricValue / SECOND_MS) % 60),
        minutes: Math.floor((metricValue / MINUTE_MS) % 60),
        hours: Math.floor((metricValue / HOUR_MS) % 24)
      };
      
      // hours
      if (metricValue >= HOUR_MS) {
        return time.hours + 'h ' + (time.minutes ? (time.minutes + 'm') : '');
      }
      // minutes
      if (metricValue >= MINUTE_MS) {
        return time.minutes + 'm ' + (time.seconds ? (time.seconds + 's') : '');
      }
      // seconds
      if (metricValue >= SECOND_MS) {
        return time.seconds + (time.milliseconds ? ('.' + time.milliseconds + ' s') : ' s');
      }
      // ms
      return  time.milliseconds + ' ms'; 
    };

    var generateBuildTitle = function(buildId) { return  '#' + buildId; };


    /**
     * Filter array using Nth and max parameters.
     * 
     * @param {array} array - Full array to be filtered.
     * @param {number} nth - Returned array will contain only every Nth item.
     * @param {number} max [20] - Returned array max size.
     */
    var filterBuildIds = function(array, nth, max) {
      max = typeof max !== 'undefined' ? max : $ctrl.BUILDS_DISPLAY_LIMIT;

      var result = [];
      for (var i = 0; i < array.length; i = i + nth) {
        result.push(array[i]);
      }
      return result.slice(0, max);
    };


    
    /**
     * Load Build Metrics for specified buildIds.
     *
     * @param {string[]} buildIds - List of Build ids.
     */
    var loadBuildMetrics = function(buildIds) {
      return BuildRecord.getBuildMetrics(buildIds).then(function(buildMetricsDatasetsResult) {
        canvasElement = document.getElementById($ctrl.componentId);

        if (!buildMetricsDatasetsResult.data.length) {
          $ctrl.noDataAvailable = true;

        } else {

          var buildMetricsData = {
            labels: buildIds,
            datasets: buildMetricsDatasetsResult.data
          };

          var datasets = buildMetricsData.datasets;
  
          if ($ctrl.chartType === 'line') {
            for (var i = 0; i < datasets.length; i++) {
              Object.assign(datasets[i], {
                label: adaptMetric(datasets[i].name).label,
                fill: false, 
  
                // lines
                borderColor: adaptMetric(datasets[i].name).color,
                borderWidth: 4,
  
                // points
                pointBackgroundColor: adaptMetric(datasets[i].name).color,
                pointBorderColor: 'white',
                pointBorderWidth: 1.5,
                pointRadius: 4
              });
            }
  
            chartConfig.options = {
              maintainAspectRatio: false,
              spanGaps: false,
              scales: {
                x: {
                  ticks: {
                    callback: generateBuildTitle
                  }
                }, 
                y: {
                  type: 'logarithmic',
                  ticks: {
                    maxTicksLimit: 8,
                    beginAtZero: true,
                    callback: generateTimeTitle
                  },
                  scaleLabel: {
                    display: true,
                    labelString: 'Logarithmic scale'
                  }
                }
              }
            };
  
          } else if ($ctrl.chartType === 'horizontalBar') {
  
            for (var j = 0; j < datasets.length; j++) {
              Object.assign(datasets[j], {
                label: adaptMetric(datasets[j].name).label,
                backgroundColor: adaptMetric(datasets[j].name).color
              });
            }
  
            chartConfig.options = {
              tooltips: {
                position: 'average'
              }, 
              scales: {
                x: {
                  position: 'bottom',
                  ticks: {
                    min: 0,
                    maxTicksLimit: 30,
                    callback: generateTimeTitle
                  },
                  stacked: true,
                  scaleLabel: {
                    display: true,
                    labelString: 'Linear scale'
                  }
                },
                y: {
                  ticks: {
                    reverse: false,
                    callback: generateBuildTitle
                  },
                  stacked: true,
                }
              }
            };
  
          } else {
            console.warn('Unsupported chart type: ' + $ctrl.chartType);
          }
  
  
          Object.assign(chartConfig.options, {
            maintainAspectRatio: false,
            tooltips: {
              callbacks: {
                title: function(tooltipItems) {
                  return generateBuildTitle(tooltipItems[0].label);
                },
                label: function(tooltipItem, data) {
                  var label = data.datasets[tooltipItem.datasetIndex].label || '';

                  if (label) {
                      label += ': ' + generateTimeTitle(tooltipItem.value);
                  }
                  return label;
                }
              }
            }
          });
  
          chartConfig.type = $ctrl.chartType;
          chartConfig.data = buildMetricsData;
  
          // increase space between legend and chart
          chartConfig.plugins = [{
            beforeInit: function(chart) {
              chart.legend.afterFit = function() {
                this.height = this.height + 20;
              };
            }
          }];
  

          var heightTmp = 0;
          var MIN_HEIGHT = 290;
          var MIN_HEIGHT_SINGLE_BUILD = 220;
  
          if ($ctrl.chartType === 'horizontalBar') {
            var isSingleBuild = buildMetricsData.datasets[0].data.length === 1;
            heightTmp = buildMetricsData.datasets[0].data.length * 30;
            canvasElement.parentElement.style.height = ((heightTmp < MIN_HEIGHT) ? (isSingleBuild ? MIN_HEIGHT_SINGLE_BUILD : MIN_HEIGHT) : heightTmp) + 'px';
          } else {
            canvasElement.parentElement.style.height = '300px';
          }
  
          // Chart is pointing to single instance of chartConfig declared on the Controller level and providing later updates based on it's changes.
          if (chart) {
            chart.update();
          } else {
            var ctx = canvasElement.getContext('2d');
            chart = new Chart(ctx, chartConfig); // jshint ignore:line
          }
        }

      });
    };

    /**
     * Load Build Metrics and handle individual states when there are no datasets available or there was an error when loading them.
     * 
     * @param {string[]} buildIds - List of Build ids.
     * @param {number} displayEveryNthItem - Number specifying every Nth metric will be displayed.
     */
    var processBuildMetrics = function(buildIds, displayEveryNthItem) {
      $ctrl.loadingError = false;
      $ctrl.noDataAvailable = false;

      /*
       * If chart does not exist yet, eg. no data states was fired on initial load.
       * Otherwise we want user to see transition from one chart to another.
       */
      if (!chart) {
        $ctrl.isLoading = true;
      }

      loadBuildMetrics(filterBuildIds(buildIds, displayEveryNthItem)).catch(function() {
        $ctrl.loadingError = true;
        $ctrl.noDataAvailable = false;
      }).finally(function() {
        $ctrl.isUpdating = false;
        $ctrl.isLoading = false;
      });
    };



    $ctrl.$onInit = function() {

      $ctrl.navigationSelect = function (item) {
        $ctrl.isUpdating = true;
        processBuildMetrics($ctrl.buildIds, item.id);
      };
      processBuildMetrics($ctrl.buildIds, $ctrl.navigationSelected.id);

    };

  }

})();