<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href='http://cdn.jsdelivr.net/yasqe/2.11.10/yasqe.min.css' rel='stylesheet' type='text/css'></link>
<link href='http://cdn.jsdelivr.net/yasr/2.10.8/yasr.min.css' rel='stylesheet' type='text/css'></link>
<title>Open Data Euskadi, el portal de datos abiertos del Gobierno Vasco</title>
</head>
<body>
<div id='yasgui'></div>
  <div id="yasr"></div>
<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
 <script src='http://cdn.jsdelivr.net/yasqe/2.2/yasqe.bundled.min.js'></script>
  <script src='http://cdn.jsdelivr.net/yasr/2.4/yasr.bundled.min.js'></script>
<script type="text/javascript">
  	$.ajaxSetup({
			type:"POST",
			cache : false,
			contentType:'application/x-www-form-urlencoded; charset=UTF-8',
			beforeSend : function(xhr) {
				xhr.setRequestHeader("Access-Control-Allow-Origin", "*");
				xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");

	    	},
	    	crossDomain: false
		});
	var yasqe = YASQE(document.getElementById("yasgui"),{
		backdrop: true,
		persistent: null,
		sparql: {
			endpoint: "${url}",
			showQueryButton: true
		}
	});
	var yasr = YASR(document.getElementById("yasr"), {
		//this way, the URLs in the results are prettified using the defined prefixes in the query
		getUsedPrefixes: yasqe.getPrefixesFromQuery
	});
	//link both together
	yasqe.options.sparql.handlers.success =  function(data, status, response) {
		yasr.setResponse({response: data, contentType: response.getResponseHeader("Content-Type")});
	};
	yasqe.options.sparql.handlers.error = function(xhr, textStatus, errorThrown) {
		yasr.setResponse({exception: textStatus + ": " + errorThrown});
	};
	yasqe.options.sparql.callbacks.complete = yasr.setResponse;
	yasr.options.getUsedPrefixes = yasqe.getPrefixesFromQuery;
</script>
</body>
</html>