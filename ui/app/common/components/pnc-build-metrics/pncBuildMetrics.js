/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
   * @param {Object[]} builds - List of Builds.
   * @param {string} chartType - Possible values: line, horizontalBar
   * @param {string} componentId - Unique component id.
   */
  angular.module('pnc.common.components').component('pncBuildMetrics', {
    bindings: {
      builds: '<',
      chartType: '@?',
      componentId: '@?'
    },
    templateUrl: 'common/components/pnc-build-metrics/pnc-build-metrics.html',
    controller: ['BuildResource', Controller]
  });

  function Controller(BuildResource) {
    var $ctrl = this;
    var chart = null;
    var canvasElement = null;
    
    // Single Controller level instance is required as Chart is pointing to it and providing later updates based on it's changes.
    var chartConfig = {};

    $ctrl.BUILDS_DISPLAY_LIMIT = 20;
    $ctrl.BUILDS_DISPLAY_LIMIT_EXAMPLE = 5;
    $ctrl.metricsTooltip = null;
   

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

        // purple
        case 'WAITING_FOR_DEPENDENCIES': 
          return { 
            color: '#a18fff', 
            label: 'Waiting',
            description: 'Waiting for dependencies'
          };
        
        // light-green
        case 'ENQUEUED': 
          return { 
            color: '#c8eb79', 
            label: 'Enqueued',
            description: 'Waiting to be started, the metric ends with the BPM process being started from PNC Orchestrator'
          };

        // cyan
        case 'SCM_CLONE': 
          return { 
            color: '#7dbdc3', 
            label: 'SCM Clone',
            description: 'Cloning / Syncing from Gerrit' 
          };

        // orange
        case 'ALIGNMENT_ADJUST': 
          return { 
            color: '#f7bd7f', 
            label: 'Alignment',
            description: 'Alignment only' 
          };

        // blue
        case 'BUILD_ENV_SETTING_UP': 
          return { 
            color: '#7cdbf3', 
            label: 'Starting Environment',
            description: 'Requesting to start new Build Environment in OpenShift' 
          };
        case 'REPO_SETTING_UP': 
          return { 
            color: '#00b9e4',
            label: 'Artifact Repos Setup',
            description: 'Creating per build artifact repositories in Indy'
          };
        case 'BUILD_SETTING_UP': 
          return { 
            color: '#008bad',
            label: 'Building',
            description: 'Uploading the build script, running the build, downloading the results (logs)'
          };

        // black
        case 'COLLECTING_RESULTS_FROM_BUILD_DRIVER': 
          return { 
            color: 'black',
            label: 'Collecting Results From Build Driver',
            description: '',
            skip: true
          };

        // purple
        case 'COLLECTING_RESULTS_FROM_REPOSITORY_MANAGER': 
          return { 
            color: '#703fec',
            label: 'Promotion',
            description: 'Downloading the list of built artifact and dependencies from Indy, promoting them to shared repository in Indy'
          };

        // green
        case 'FINALIZING_BUILD': 
          return { 
            color: '#3f9c35', 
            label: 'Finalizing',
            description: 'Completing all other build execution tasks, destroying build environments, invoking the BPM'
          };

        // gray
        case 'OTHER': 
          return { 
            color: 'silver', 
            label: 'Other',
            description: 'Other tasks from the time when the build was submitted to the time when the build ends'
          };

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

      var SECOND_MS = 1000;
      var MINUTE_MS = 60 * SECOND_MS;
      var HOUR_MS = 60 * MINUTE_MS;
      var DAYS_MS = 24 * HOUR_MS;

      var time = {
        milliseconds: metricValue % SECOND_MS,
        seconds: Math.floor((metricValue / SECOND_MS) % 60),
        minutes: Math.floor((metricValue / MINUTE_MS) % 60),
        hours: Math.floor((metricValue / HOUR_MS) % 24),
        days: Math.floor(metricValue / DAYS_MS)
      };
      
      // days
      if (metricValue >= DAYS_MS) {
        return time.days + 'd ' + (time.hours ? (time.hours + 'h') : '');
      }
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
    var filterBuilds = function(array, nth, max) {
      max = typeof max !== 'undefined' ? max : $ctrl.BUILDS_DISPLAY_LIMIT;

      var result = [];
      for (var i = 0; i < array.length; i = i + nth) {
        result.push(array[i]);
      }

      if ($ctrl.chartType === 'line') {
        return result.slice(0, max).reverse();
      } else if ($ctrl.chartType === 'horizontalBar') {
        return result.slice(0, max);
      }
    };


    
    /**
     * Load Build Metrics for specified builds.
     *
     * @param {Object[]} builds - List of Builds.
     */
    var loadBuildMetrics = function(builds) {
      var buildIds = builds.map(function(build) {
        return build.id.toString();
      });

      return BuildResource.getBuildMetrics(buildIds).then(function(buildMetricsDatasetsResult) {
        canvasElement = document.getElementById($ctrl.componentId);
        var adaptedMetric;

        if (!buildMetricsDatasetsResult.data.length) {
          $ctrl.noDataAvailable = true;

        } else {

          // skip specific metrics
          buildMetricsDatasetsResult.data = buildMetricsDatasetsResult.data.filter(function(item) {
            return !adaptMetric(item.name).skip;
          });

          var buildMetricsData = {
            labels: buildIds,
            datasets: buildMetricsDatasetsResult.data
          };

          // sum individual metrics for given build
          var buildMetricsSum = new Array(buildIds.length).fill(0);
          
          for (var m = 0; m < buildMetricsData.datasets.length; m++) {
            for (var n = 0; n < buildMetricsData.datasets[m].data.length; n++) {
              buildMetricsSum[n] += buildMetricsData.datasets[m].data[n];
            }
          }

          // compute Other metric
          var metricOthersData = [];

          for (var k = 0; k < builds.length; k++) {
            var metricOther = builds[k].endTime - builds[k].submitTime - buildMetricsSum[k];
            metricOthersData.push(metricOther > 0 ? metricOther : 0);
          }

          buildMetricsData.datasets.push({
            name: 'OTHER',
            data: metricOthersData
          });

          // generate tooltip content
          $ctrl.metricsTooltip = buildMetricsData.datasets.map(function(item) {
            adaptedMetric = adaptMetric(item.name);
            return { 
              label: adaptedMetric.label,
              description: adaptedMetric.description
            };
          });
  
          if ($ctrl.chartType === 'line') {
            for (var i = 0; i < buildMetricsData.datasets.length; i++) {
              adaptedMetric = adaptMetric(buildMetricsData.datasets[i].name);

              Object.assign(buildMetricsData.datasets[i], {
                label: adaptedMetric.label,
                fill: false, 
  
                // lines
                borderColor: adaptedMetric.color,
                borderWidth: 4,
  
                // points
                pointBackgroundColor: adaptedMetric.color,
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
  
            for (var j = 0; j < buildMetricsData.datasets.length; j++) {
              adaptedMetric = adaptMetric(buildMetricsData.datasets[j].name);
              Object.assign(buildMetricsData.datasets[j], {
                label: adaptedMetric.label,
                backgroundColor: adaptedMetric.color
              });
            }
  
            chartConfig.options = {
              tooltips: {
                position: 'nearest'
              }, 
              animation: {
                duration: 0  // disable animation
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

          var isSingleBuild = buildMetricsData.datasets[0].data.length === 1;
  
          Object.assign(chartConfig.options, {
            layout: {
              padding: {
                top: 20,
                bottom: 20
              }
            },
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
                  if (tooltipItem.value !== 'NaN' && tooltipItem.value > 1000) {
                    label += '  (' + tooltipItem.value + ' ms)';
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
                this.height = this.height + 25;
              };
            }
          }];

          var heightTmp = 0;
          var MIN_HEIGHT = 290;
          var MIN_HEIGHT_SINGLE_BUILD = 400;
  
          if ($ctrl.chartType === 'horizontalBar') {
            heightTmp = buildMetricsData.datasets[0].data.length * 30;
            canvasElement.parentElement.style.height = ((heightTmp < MIN_HEIGHT) ? (isSingleBuild ? MIN_HEIGHT_SINGLE_BUILD : MIN_HEIGHT) : heightTmp) + 'px';
            if (isSingleBuild) {
              chartConfig.options.layout.padding.bottom = 100;
            }
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
     * @param {Object[]} builds - List of Builds.
     * @param {number} displayEveryNthItem - Number specifying every Nth metric will be displayed.
     */
    var processBuildMetrics = function(builds, displayEveryNthItem) {
      $ctrl.loadingError = false;
      $ctrl.noDataAvailable = false;

      /*
       * If chart does not exist yet, eg. no data states was fired on initial load.
       * Otherwise we want user to see transition from one chart to another.
       */
      if (!chart) {
        $ctrl.isLoading = true;
      }

      loadBuildMetrics(filterBuilds(builds, displayEveryNthItem)).catch(function() {
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
        processBuildMetrics($ctrl.builds, item.id);
      };
      processBuildMetrics($ctrl.builds, $ctrl.navigationSelected.id);

    };

  }

})();