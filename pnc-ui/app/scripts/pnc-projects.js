/*global $:false */
/*global PNC_REST_BASE_URL:false */
'use strict';

// Initialize Datatables
$(document).ready(function() {

  // Clear all sessionStorage except productId and versionId
  var product = $.parseJSON(sessionStorage.getItem('product'));
  var version = $.parseJSON(sessionStorage.getItem('version'));
  sessionStorage.clear();
  sessionStorage.setItem('product', JSON.stringify(product));
  sessionStorage.setItem('version', JSON.stringify(version));

  $('#productInfoName').html(product.name);
  $('#productInfoDesc').html(product.description);
  $('#productInfoVersion').html(version.version);
  
  $('#projects').dataTable( {
    stateSave: true,
    'ajax': {
      'url': PNC_REST_BASE_URL + '/product/' + product.id + '/version/' + version.id + '/project',
      'type': 'GET',
      'dataSrc': ''
    }, 
    'columns': [
      { 'data': 'id' },
      { 'data': 'name' },
      { 'data': 'description' },
      { 'data': 'issueTrackerUrl' },
      { 'data': 'projectUrl' },            
      { 'data':
        function(json) {
          return '<button class="configurations btn btn-default" value="' + json.id + '">View Configurations</button>';
        }
      }
    ]
  });
  
  $('#projects tbody').on( 'click', 'button.configurations', function (event) {
    event.preventDefault();
    $.ajax({
        url: PNC_REST_BASE_URL + '/project/' + $(this).attr('value'),
        method: 'GET',
        success: function( data ) {
            sessionStorage.setItem('project', JSON.stringify(data));
            console.log('Stored in sessionStorage: project ' + JSON.stringify(data));
            $(location).attr('href','configurations.html');
        }
    });
  });

});
