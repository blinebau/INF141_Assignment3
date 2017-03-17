$(document).ready(function () {
	
	var options = {
		success : handleResult,
	}
	
	$('#searchForm').ajaxForm(options);
	
});


function handleResult(response, xhr, $form) {
	var resultTable = '';
	$('#results tbody').empty();
	
	$.each(response.resultDocs, function(i, url) {
		resultTable += '<tr><td><a href="http://' + url + '">' + url + '</a></td></tr>';
	});
	
	$('#results').append(resultTable);
};