window.setAutocomplete = function($ele, url) {
	//console.log($ele,url);
    $ele.autocomplete({
        minLength: 3,
        delay: 500,
        source: function (request, response) {
        	requestData ={"term":request.term};
        	 $.getJSON(url, requestData, function (data) {
                 response($.map(data, function (item) {
                     return {
                         label: item.name+" - "+item.nric,
                         value: item.id,
                     };
                 }));
             });
     	},
        select: function(event, ui) {
       	 	event.preventDefault();
   		 	var $this = $(this);
   		 	setAutocompleteValue($this,ui);
        },
        focus: function(event, ui){
            event.preventDefault();
            var $this = $(this);
            if(ui.item.value != null){
            	$this.val(ui.item.label);
            }
        },
         response: function(event, ui) {
            if (!ui.content.length) {
                var noResult = { 
                     value: "", 
                     label: "No results found" 
				 };
                 ui.content.push(noResult);                    
             }
        },
    })
}