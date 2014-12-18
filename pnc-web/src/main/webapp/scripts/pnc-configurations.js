// Initialize Datatables
$(document).ready(function() {

  // CONSTANT VALUES
  var MAX_POLLS = 200
  var POLL_INTERVAL = 10000 //ms

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

  var prodTable = $('#configuration').dataTable( {
    stateSave: true,
    'ajax': {
      'url': PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + version.id + '/project/' + project.id + '/configuration',
      'type': 'GET',
      'dataSrc': ''
    }, 
    'columns': [
      { 'data': 'id' },
      { 'data': 'identifier' },
      { 'data': 'buildScript' },
      { 'data': 'scmUrl' },
      { 'data': 'patchesUrl' },
      { 'data': 
        function(json) {
          if (json.creationTime == null) {
            return '';
          }
          return new Date(json.creationTime).toLocaleString();;
        }
      },
      { 'data': 
        function(json) {
          if (json.creationTime == null) {
            return '';
          }
          return new Date(json.lastModificationTime).toLocaleString();;
        } 
      },
      { 'data':
        function(json) {
          return '<button class="build btn btn-block btn-danger" id="btn-trigger-build-' + json.id + '" data-configuration-id="' + json.id + '">Build</button>';
        }
      }
    ]
  });

  /*
   *
   * Triggering a build
   *
   */

  function postTriggerBuild(configId, pollUrl) {
    console.log('postTriggerBuild()');
    $('#btn-trigger-build-' + configId).parents('td').html(
      '<p><span id="in-progress-build-' + configId + '" class="spinner spinner-xs spinner-inline"></span> Building</p>');

    // poll counter
    var polls = 0;
    // Self executing polling function
    (function poll(){
      setTimeout(
        function(){
          console.log('SetTimeOutFunction()');
          $.ajax(
            { url: pollUrl, 
              complete: 
                function(data, textStatus) {
                  if (polls > MAX_POLLS) {
                    throw new Error('Maximum number of polls exceeded');
                  }

                  console.log('Poll #%d Result: data={%O}, textStatus{%O}', polls++, data, textStatus);

                  // Action the result of the poll
                  switch (data.status) {
                    case 200:
                      if (data.responseJSON.status === 'BUILDING') {
                        console.log('BUILD IN PROGRESS');
                        // Contiune polling
                        poll();
                      } else {
                        throw new Error('HTTP response 200 but JSON status other than BUILDING returned');
                      }
                      break;
                    case 204:
                      console.log('BUILD COMPLETED');
                      buildCompleted(configId, pollUrl);
                      break;
                    default:
                      throw new Error('Unrecognised HTTP response received: ' + data.responseJSON.status);
                  }
                } 
            }
          );
        }, 
        POLL_INTERVAL
      );
    })();
  }

  function buildCompleted(configId, pollUrl) {
    console.log('buildCompleted()');
    sessionStorage.setItem('configurationId', configId);
    $('#in-progress-build-' + configId).parents('td').html('<a class="btn btn-success" href="results.html">COMPLETED</a>');
    $('#alert-space').empty();
  }


  $('#configuration tbody').on( 'click', 'button.build', 
    function (event) {
      event.preventDefault();
      console.log('trigger build click registered');

      var configId = $(this).data("configuration-id");

      $.post(PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + version.id + '/project/' + project.id + '/configuration/' + configId + '/build')
        .done(
          function(data, text, xhr) {
            $('#alert-space').prepend('<br/><div class="alert alert-success" role="alert">Build successfully triggered</div>');
            console.log('Trigger build successful: data={%O}, text={%O}, xhr={%O}', data, text, xhr);
            postTriggerBuild(configId, data);
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


  /*
   *
   * Creating new configuration
   *
   */


  $('#configuration_content').on( 'click', 'button.addConfiguration', function (event) {
    event.preventDefault();
    $(location).attr('href',"configuration_add.html");
  });

  $('#configuration_content_add').on( 'click', 'button.cancelConfiguration', function (event) {
    event.preventDefault();
    $(location).attr('href',"configurations.html");
  });

  $('#configuration_content_add').on( 'click', 'button.saveConfiguration', function (event) {

       event.preventDefault();

       var identifier = $('#addConfIdentifier').val();
       var buildScript = $('#addConfBuildScript').val();
       var scmUrl = $('#addConfScmUrl').val();
       var patchesUrl = $('#addConfPatchesUrl').val();

       var JSONObj = {
            "identifier": identifier,
            "buildScript": buildScript,
            "scmUrl": scmUrl,
            "patchesUrl": patchesUrl
       };

       var data = JSON.stringify(JSONObj);
       console.log('Creating new build configuration: ' + data);

       $.ajax({
               url: PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + version.id + '/project/' + project.id + '/configuration',
               type: 'POST',
               dataType : 'json',
               data: data,
               contentType: "application/json; charset=utf-8",
               success: function(data) {
                  console.log('build configuration creation was successful');
               },
               failure: function(errMsg) {
                 console.log('build configuration creation was NOT successful: ' + errMsg);
               },
               complete: function(xhr, status) {
                  console.log('build configuration creation complete!!!!');
                  $(location).attr('href',"configurations.html");
               }
       });
  });

} );
