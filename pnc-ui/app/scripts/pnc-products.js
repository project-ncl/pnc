/*global $:false */
/*global PNC_REST_BASE_URL:false */
'use strict';

// Initialize Datatables
$(document).ready(function() {

  // Clear all sessionStorage
  sessionStorage.clear();

  $('#products').dataTable( {
    'bAutoWidth': false,
    stateSave: true,
    'ajax': {
      'url': PNC_REST_BASE_URL + '/product',
      'type': 'GET',
      'dataSrc': ''
    },
    'columns': [
      { 'data': 'id' },
      { 'data': 'name' },
      { 'data': 'description' },
      { 'data':
        function(json) {
          return '<button class="versions btn btn-default" value=' + json.id + '>View Versions</button>';
        }
      }
    ]
  });

  $('#products tbody').on( 'click', 'button.versions', function (event) {
    event.preventDefault();

    $.ajax({
        url: PNC_REST_BASE_URL + '/product/' + $(this).attr('value'),
        method: 'GET',
        success: function( data ) {
            sessionStorage.setItem('product', JSON.stringify(data));
            console.log('Stored in sessionStorage: product ' + JSON.stringify(data));
            $(location).attr('href','productversions.html');
        }
    });
  });

});
