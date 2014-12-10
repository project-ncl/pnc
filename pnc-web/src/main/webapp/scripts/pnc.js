// Initialize Datatables
$(document).ready(function() {
  $('#projects').dataTable( {
    'ajax': {
      'url': 'http://localhost:8080/pnc-web/rest/configuration',
      'type': 'GET',
      'dataSrc': ''
    }, 
    'columns': [
      { 'data': 'id' },
      { 'data': 'identifier' },
      { 'data': 'projectName' },
      { 'data': function(json) {
                  return '<a class="btn btn-success" href="' + PNC_REST_BASE_URL + 'configuration/' + json.id + '/build">build</a>'
                } 
      }
    ]
  } );
} );