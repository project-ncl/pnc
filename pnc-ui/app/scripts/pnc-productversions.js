/*global $:false */
/*global PNC_REST_BASE_URL:false */
'use strict';

// Initialize Datatables
$(document).ready(function() {

  // Clear all sessionStorage except productId
  var product = $.parseJSON(sessionStorage.getItem('product'));
  sessionStorage.clear();
  sessionStorage.setItem('product', JSON.stringify(product));

  $('#productInfoName').html(product.name);
  $('#productInfoDesc').html(product.description);

  $('#productversion').dataTable( {
    stateSave: true,
    'ajax': {
      'url': PNC_REST_BASE_URL + '/product/' + product.id + '/version',
      'type': 'GET',
      'dataSrc': ''
    }, 
    'columns': [
      { 'data': 'id' },
      { 'data': 'version' },
      { 'data':
        function(json) {
          return '<button class="projects btn btn-default" value="' + json.id + '">View Projects</button>';
        }
      }
    ]
  });
  
  $('#productversion tbody').on( 'click', 'button.projects', function (event) {
    event.preventDefault();

    $.ajax({
        url: PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + $(this).attr('value'),
        method: 'GET',
        success: function( data ) {
            sessionStorage.setItem('version', JSON.stringify(data));
            console.log('Stored in sessionStorage: version ' + JSON.stringify(data));
            $(location).attr('href','project.html');
        }
    });
  });

});
