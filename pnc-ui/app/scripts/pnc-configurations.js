/*global $:false */
/*global PNC_REST_BASE_URL:false */

'use strict';

// Initialize Datatables
$(document).ready(function() {

  // CONSTANT VALUES
  var POLL_INTERVAL = 10000; //ms

  // Clear all sessionStorage except productId,versionId and projectId
  var product = $.parseJSON(sessionStorage.getItem('product'));
  var version = $.parseJSON(sessionStorage.getItem('version'));
  var project = $.parseJSON(sessionStorage.getItem('project'));
  sessionStorage.clear();
  sessionStorage.setItem('product', JSON.stringify(product));
  sessionStorage.setItem('version', JSON.stringify(version));
  sessionStorage.setItem('project', JSON.stringify(project));

  $('#productInfoName').html(product.name);
  $('#productInfoDesc').html(product.description);
  $('#productInfoVersion').html(version.version);
  $('#projectInfoName').html(project.name);
  $('#projectInfoDesc').html(project.description);
  $('#projectInfoProjectUrl').html(project.projectUrl);
  $('#projectInfoIssueTrackerUrl').html(project.issueTrackerUrl);

  var configurationsWithResults = [];


  // Holds timeout id's so they can be cancelled
  var timeouts = {};

  // Holds action column's that need to be drawn once the table is initialized.
  var cellsToDraw = [];

  $.when(
     $.ajax({
         url: PNC_REST_BASE_URL + '/result',
         method: 'GET',
         success: function (data) {
           console.log('Querying for existing builds, result: %O', data);
           $.each(data, function(entryIndex, entry){
              if ($.inArray(entry.buildConfigurationId, configurationsWithResults) === -1) {
                console.log('Found existing build: configId={%d}', entry.buildConfigurationId);
                configurationsWithResults.push(entry.buildConfigurationId);
             }
            console.log('configurations with results: %O', configurationsWithResults);
           });
         },
         error: function (data) {
             console.log(JSON.stringify(data));
         }
     })
  ).then( function(){
      $('#configuration').dataTable( {
        stateSave: true,
        'ajax': {
          'url': PNC_REST_BASE_URL + '/project/' + project.id + '/configuration',
          'type': 'GET',
          'dataSrc': ''
        },
        'columns': [
          { 'data': 'id' },
          { 'data': 'name' },
          { 'data': 'buildScript' },
          { 'data': 'scmUrl' },
          { 'data': 'patchesUrl' },
          { 'data':
            function(json) {
              if (json.creationTime === null) {
                return '';
              }
              return new Date(json.creationTime).toLocaleString();
            }
          },
          { 'data':
            function(json) {
              if (json.creationTime === null) {
                return '';
              }
              return new Date(json.lastModificationTime).toLocaleString();
            }
          },
          { 'defaultContent': ''}
      ],
      'columnDefs': [{
        'targets': 7,
        'createdCell':
          function (td, cellData, rowData) {
              $(td).attr('id', 'action-cell-config-id-' + rowData.id);
              cellsToDraw.push(rowData.id);
          }
        }],
     });
  });


  $('#configuration_content').on( 'click', 'button.results', function(event) {
    event.preventDefault();
    sessionStorage.setItem('configurationId', $(this).attr('value'));
    console.log('Stored in sessionStorage: configurationId ' + $(this).attr('value'));
    $(location).attr('href','results.html');
  });

  $('#configuration').on( 'init.dt', function () {
    $.each(cellsToDraw, function(index, obj) {
      drawActionColumn(obj);
    });
  });

  /*
   *
   * Triggering a build
   *
   */



  $('#configuration_content').on( 'click', 'button.build',
    function (event) {
      event.preventDefault();
      console.log('trigger build click registered');

      var configId = $(this).data('configuration-id');

      $.post(PNC_REST_BASE_URL + '/project/' + project.id + '/configuration/' + configId + '/build')
        .done(
          function(data, text, xhr) {
            $('#alert-space').prepend('<br/><div class="alert alert-success" role="alert">Build successfully triggered</div>');
            console.log('Trigger build successful: data={%O}, text={%O}, xhr={%O}', data, text, xhr);
            drawActionColumn(configId);
            startPolling(configId, buildCompleted);
          }
        )
        .fail(
          function(data, text, xhr) {
            $('#alert-space').prepend('<br/><div class="alert alert-danger" role="alert">Error attempting to trigger build</div>');
            console.log('Trigger build failed: data={%O}, text={%O}, xhr={%O}', data, text, xhr);
          }
        );
    }
  );

  function buildCompleted(configId) {
    console.log('buildCompleted(configId=%d)', configId);
    configurationsWithResults.push(configId);
    drawActionColumn(configId);
    $('#alert-space').html('<br/><div class="alert alert-info" role="alert">Build of configuration #' + configId + ' completed</div>');
  }

  function hasExistingBuilds(configId) {
    return ($.inArray(configId, configurationsWithResults) !== -1);
  }

  function getBuildStatusPromise(configId) {
    return $.get(PNC_REST_BASE_URL + '/result/running/' + configId)
      .fail( function (data, text, xhr) {
        throw new Error('Error in HTTP request getting build status xhr=' + xhr);
      });
  }

  function isBuildInProgress(jqXHR) {
    console.log('isBuildInProgress:: jqXHR={%O}', jqXHR);

    if (jqXHR.status === 204) {
      return false;
    } else if (jqXHR.status === 200 && jqXHR.responseJSON.status === 'BUILDING') {
      return true;
    }
    throw new Error('Unexpected outcome of HTTP request: jqXHR=' + jqXHR);
  }

  function drawActionColumn(configId) {
    var cell = $('#action-cell-config-id-' + configId);
    console.log('cell=%O', cell);
    cell.empty();

    $.when(getBuildStatusPromise(configId))
      .then(function(data, textStatus, jqXHR) {
        console.log('drawActionColumn(%d):: data={%O}, textStatus={%O}, jqXHR={%O}', configId, data, textStatus, jqXHR);

        var html;

        if (isBuildInProgress(jqXHR)) {
          html = '<p><span class="spinner spinner-xs spinner-inline"></span> Building</p>';
          if (!isPolling(configId)) {
            startPolling(configId, buildCompleted);
          }
        } else {
          html = '<button class="build btn btn-block btn-danger" data-configuration-id="' + configId + '">Build</button>';
        }

        if (hasExistingBuilds(configId)) {
          html += '<button class="results btn btn-block btn-default" value="' + configId + '">View Results</button>';
        }

        cell.html(html);
      }
    );
  }

  function startPolling(configId, fnSuccess) {
    // poll counter
    var polls = 0;

    (function poll(){
      timeouts[configId] = setTimeout(
        function(){
          $.when(getBuildStatusPromise(configId))
            .then(function(data, textStatus, jqXHR) {
              console.log('Poll #%d for configId: %d Result: data={%O}, textStatus{%O}, jqXHR={%O}', polls++, configId, data, textStatus, jqXHR);
              if (! isBuildInProgress(jqXHR)) {
                stopPolling(configId);
                fnSuccess(configId);
              }
            }
          );
        poll();
        },
        POLL_INTERVAL
      );
    })();
  }

  function stopPolling(configId) {
    clearTimeout(timeouts[configId]);
  }

  function isPolling(configId) {
    return timeouts.hasOwnProperty(configId);
  }

  /*
   *
   * Creating new configuration
   *
   */


  $('#configuration_content').on( 'click', 'button.addConfiguration', function (event) {
    event.preventDefault();
    $(location).attr('href','configuration_add.html');
  });

  $('#configuration_content_add').on( 'click', 'button.cancelConfiguration', function (event) {
    event.preventDefault();
    $(location).attr('href','configurations.html');
  });

  $('#configuration_content_add').on( 'click', 'button.saveConfiguration', function (event) {

       event.preventDefault();

       var name = $('#addConfName').val();
       var description = $('#addConfDescription').val();
       var buildScript = $('#addConfBuildScript').val();
       var scmUrl = $('#addConfScmUrl').val();
       var patchesUrl = $('#addConfPatchesUrl').val();

       var JSONObj = {
            'name': name,
            'description': description,
            'buildScript': buildScript,
            'scmUrl': scmUrl,
            'patchesUrl': patchesUrl
       };

       var data = JSON.stringify(JSONObj);
       console.log('Creating new build configuration: ' + data);

       $.ajax({
               url: PNC_REST_BASE_URL + '/project/' + project.id + '/configuration',
               type: 'POST',
               dataType : 'json',
               data: data,
               contentType: 'application/json; charset=utf-8',
               success: function() {
                  console.log('build configuration creation was successful');
               },
               failure: function(errMsg) {
                 console.log('build configuration creation was NOT successful: ' + errMsg);
               },
               complete: function() {
                  console.log('build configuration creation complete!!!!');
                  $(location).attr('href','configurations.html');
               }
       });
  });

});
