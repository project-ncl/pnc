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

  var filteredResults = [];
  var buildConfigIdentifier = '';
  var buildConfigScript = '';

  $.when(
     $.ajax({
         url: PNC_REST_BASE_URL + '/result',
         method: "GET",
         success: function (data) {
           $.each(data, function(entryIndex, entry){
             if (entry['projectBuildConfigurationId'] == configurationId) {
                filteredResults.push(entry);
             }
           });
         },
         error: function (data) {
             console.log(JSON.stringify(data));
         }
     }),
     $.ajax({
         url: PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + version.id + '/project/' + project.id + '/configuration',
         method: "GET",
         success: function (data) {

           buildConfigIdentifier = data[0].identifier;
           buildConfigScript = data[0].buildScript;
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
         { "sWidth": "10%", "data": "status" },
         { "bSortable": false, "sWidth": "60%", "data":
            function(json) {
              return '<div class="divrep" id="divLog"><h2>...</h2></div><button class="logs btn btn-default" value="' + json.id + '">View Logs</button>';
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
            var resourceContent = data;
            var preStyle = 'style="height: 30pc; overflow-y: scroll; overflow-x: scroll; width: ' + $("#divLog").width() + 'px;"';
            data = '<pre ' + preStyle + '>' + data + '</pre>';
            $("#divLog").html(data);
        }
    });
  });

} );
