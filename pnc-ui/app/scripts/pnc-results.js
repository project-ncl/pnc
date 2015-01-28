/*global $:false */
/*global PNC_REST_BASE_URL:false */
'use strict';

// Initialize Datatables
$(document).ready(function() {

  var product = $.parseJSON(sessionStorage.getItem('product'));
  var version = $.parseJSON(sessionStorage.getItem('version'));
  var project = $.parseJSON(sessionStorage.getItem('project'));
  //var configuration = $.parseJSON(sessionStorage.getItem('configuration'));
  var configurationId = parseInt(sessionStorage.getItem('configurationId'));
  sessionStorage.clear();
  sessionStorage.setItem('product', JSON.stringify(product));
  sessionStorage.setItem('version', JSON.stringify(version));
  sessionStorage.setItem('project', JSON.stringify(project));
  sessionStorage.setItem('configurationId', configurationId);
  console.log('Loading results for configurationId: %d', configurationId);

  $('#productInfoName').html(product.name);
  $('#productInfoDesc').html(product.description);
  $('#productInfoVersion').html(version.version);
  $('#projectInfoName').html(project.name);
  $('#projectInfoDesc').html(project.description);
  $('#projectInfoProjectUrl').html(project.projectUrl);
  $('#projectInfoIssueTrackerUrl').html(project.issueTrackerUrl);

  var filteredResults = [];
  var buildConfigIdentifier = '';

  $.when(
     $.ajax({
         url: PNC_REST_BASE_URL + '/project/' + project.id + '/configuration/' + configurationId + '/result',
         method: 'GET',
         success: function (data) {
           $.each(data, function(entryIndex, entry){
             console.log('checking /result entry: %O', entry);
             console.log('found entry for configurationId=%d', configurationId);
             filteredResults.push(entry);
           });
         },
         error: function (data) {
             console.log(JSON.stringify(data));
         }
     }),
     $.ajax({
         url: PNC_REST_BASE_URL + '/project/' + project.id + '/configuration/' + configurationId,
         method: 'GET',
         success: function (data) {

           buildConfigIdentifier = data.name;
         },
         error: function (data) {
             console.log(JSON.stringify(data));
         }
     })
  ).then( function(){
      loadDataTable(filteredResults);
  });

  function loadDataTable(filteredResults) {

     $('#results').dataTable( {
       stateSave: true,
       'bAutoWidth': false,
       'aaData': filteredResults,
       'aoColumns': [
         { 'sWidth': '5%', 'data': 'id' },
         { 'sWidth': '10%', 'data':
            function(json) {
              if (json.status === 'SUCCESS') {
                return '<a class="label label-success">SUCCESS</a>';
              }
              else if (json.status === 'FAILED') {
                return '<a class="label label-danger">FAILED</a>';
              }
              return '<a class="label label-warning">' + json.status + '</a>';
            }
         },
         { 'bSortable': false, 'sWidth': '60%', 'data':
            function(json) {
              return '<div class="divrep" id="divLog' + json.id  + '"><h2>...</h2></div><button class="logs btn btn-default" value="' + json.id + '">View Logs</button>';
            }
         },
         { 'sWidth': '10%', 'data':
            function(json) {
              console.log(json);
              return buildConfigIdentifier;
            }
         },
         { 'sWidth': '15%', 'data':
            function(json) {
              return json.buildScript;
            }
         }
       ]
     });
  }

  $('#results').on( 'click', 'button.logs', function (event) {
    event.preventDefault();
    var resultId = $(this).attr('value');

    $.ajax({
        url: PNC_REST_BASE_URL + '/result/' + resultId + '/log',
        method: 'GET',
        async: false,
        cache: false,
        dataType: 'text',
        success: function( data ) {
            var divLogId = '#divLog' + resultId;
            var preStyle = 'style="height: 30pc; overflow-y: scroll; overflow-x: scroll; width: ' + $(divLogId).width() + 'px;"';
            data = '<pre ' + preStyle + '>' + data + '</pre>';
            $(divLogId).html(data);
        }
    });
  });

});
