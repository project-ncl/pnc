// Initialize Datatables
$(document).ready(function() {

  var product = $.parseJSON(sessionStorage.getItem('product'));
  var version = $.parseJSON(sessionStorage.getItem('version'));
  var project = $.parseJSON(sessionStorage.getItem('project'));
  //var configuration = $.parseJSON(sessionStorage.getItem('configuration'));
  var configurationId = sessionStorage.getItem('configurationId');
  sessionStorage.clear();
  sessionStorage.setItem('product', JSON.stringify(product));
  sessionStorage.setItem('version', JSON.stringify(version));
  sessionStorage.setItem('project', JSON.stringify(project));
  sessionStorage.setItem('configurationId', configurationId);

  $('#productInfoName').html(product.name);
  $('#productInfoDesc').html(product.description);
  $('#productInfoVersion').html(version.version);
  $('#projectInfoName').html(project.name);
  $('#projectInfoDesc').html(project.description);
  $('#projectInfoProjectUrl').html(project.projectUrl);
  $('#projectInfoIssueTrackerUrl').html(project.issueTrackerUrl);

  var filteredResults = [];
  var buildConfigIdentifier = '';
  var buildConfigScript = '';

  $.when(
     $.ajax({
         url: PNC_REST_BASE_URL + '/result',
         method: "GET",
         success: function (data) {
           $.each(data, function(entryIndex, entry){
             if (entry['configurationIds'] == configurationId) {
                filteredResults.push(entry);
             }
           });
         },
         error: function (data) {
             console.log(JSON.stringify(data));
         }
     }),
     $.ajax({
         url: PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + version.id + '/project/' + project.id + '/configuration/' + configurationId,
         method: "GET",
         success: function (data) {

           buildConfigIdentifier = data.identifier;
           buildConfigScript = data.buildScript;
         },
         error: function (data) {
             console.log(JSON.stringify(data));
         }
     })
  ).then( function(){
      loadDataTable(filteredResults);
  });

  function loadDataTable(filteredResults) {

     var prodTable = $('#results').dataTable( {
       stateSave: true,
       "bAutoWidth": false,
       "aaData": filteredResults,
       "aoColumns": [
         { "sWidth": "5%", "data": "id" },
         { "sWidth": "10%", "data":
            function(json) {
              if (json.status == 'SUCCESS') {
                return '<a class="label label-success">SUCCESS</a>';
              }
              else if (json.status == 'FAILED') {
                return '<a class="label label-danger">FAILED</a>';
              }
              return '<a class="label label-warning">' + json.status + '</a>';
            }
         },
         { "bSortable": false, "sWidth": "60%", "data":
            function(json) {
              return '<div class="divrep" id="divLog' + json.id  + '"><h2>...</h2></div><button class="logs btn btn-default" value="' + json.id + '">View Logs</button>';
            }
         },
         { "sWidth": "10%", "data":
            function(json) {
              return buildConfigIdentifier;
            }
         },
         { "sWidth": "15%", "data":
            function(json) {
              return buildConfigScript;
            }
         }
       ]
     })
  };

  $('#results').on( 'click', 'button.logs', function (event) {
    event.preventDefault();
    var resultId = $(this).attr('value');

    $.ajax({
        url: PNC_REST_BASE_URL + '/result/' + resultId + '/log',
        method: "GET",
        async: false,
        cache: false,
        dataType: "text",
        success: function( data, textStatus, jqXHR ) {
            var divLogId = "#divLog" + resultId;
            var resourceContent = data;
            var preStyle = 'style="height: 30pc; overflow-y: scroll; overflow-x: scroll; width: ' + $(divLogId).width() + 'px;"';
            data = '<pre ' + preStyle + '>' + data + '</pre>';
            $(divLogId).html(data);
        }
    });
  });

} );
