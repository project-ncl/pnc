// Initialize Datatables
$(document).ready(function() {
  var prodTable = $('#products').dataTable( {
    'ajax': {
      'url': PNC_REST_BASE_URL + 'configuration',
      'type': 'GET',
      'dataSrc': ''
    }, 
    'columns': [
      { 'data': 'id' },
      { 'data': 'identifier' },
      { 'data': 'projectName' },
      { 'data': 
        function(json) {
          return '<button class="build btn btn-block btn-danger" value="' + json.id + '">build</button>';
        }
      }
    ]
  });

  $('#products tbody').on( 'click', 'button', function (event) {
    event.preventDefault();
    $.post(PNC_REST_BASE_URL + 'configuration/' + $(this).attr('value') + '/build').
        done(function() {
          $('#content').prepend('<br/><div class="alert alert-success" role="alert">Build successfully triggered</div>');
          console.log('success');
        }).fail(function() {
          $('#content').prepend('<br/><div class="alert alert-danger" role="alert">Error attempting to trigger build</div>');
          console.log('failure');
        });
  });
} );