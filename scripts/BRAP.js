////////////////////////////////////////////////
//SECTION 0: Assign Ids
 var attId=1;
  jQuery('*').each( function() { 
   jQuery(this).attr('attid', attId);
   attId=attId+1;
 });

 
////////////////////////////////////////////////
//SECTION 1: Handle conflicts
//jquery.min.js is 10.1 so, get a reference to that and release $
var jq101 = jQuery.noConflict();
console.log("jQuery injected by BRAP is: "+jq101.fn.jquery);

////////////////////////////////////////////////
//http://stackoverflow.com/questions/2360655/jquery-event-handlers-always-execute-in-order-they-were-bound-any-way-around-t
//SECTION 2: Define handle First 
//DEFINE : bindFirst
//[name] is the name of the event "click", "mouseover", .. 
//same as you'd pass it to bind()
//[fn] is the handler function
jq101.fn.bindFirst = function(name, fn) {
 // bind as you normally would
 // don't want to miss out on any jQuery magic
 this.on(name, fn);

 // Thanks to a comment by @Martin, adding support for
 // namespaced events too.
 this.each(function() {
     var handlers = jq101._data(this, 'events')[name.split('.')[0]];
     console.log(handlers);
     // take out the handler we just inserted from the end
     var handler = handlers.pop();
     // move it at the beginning
     handlers.splice(0, 0, handler);
 });
};


////////////////////////////////////////////////
//SECTION 3: Bind Events
// for focus
var mapElements = {"previous":10};
jq101(":input:not(:button):not(input[type='image'])").bindFirst('keyup', function() {
	var message = getMessage(event);
	console.log("Click event is fired:tag:"+event.target.tagName+",Msg:" + message);
	var mapAttributes = {};
	mapAttributes['attid'] = jq101(event.target).attr('attid');
	mapAttributes['url'] = jq101(location).attr('href');
	mapAttributes['type'] = event.target.type;
	mapAttributes['id'] = event.target.id;
	mapAttributes['name'] = event.target.name;
	mapAttributes['event'] = event.type;
	mapAttributes['value'] = event.target.value;
	mapElements["previous"]=mapAttributes;
	//var cashedObj=mapElements["previous"];
	//message ="attid:"+cashedObj['attid']+"TAB"+ "url:"+cashedObj['url']+"TAB"+"type:"+mapAttributes['type']+"TAB"+"id:"+mapAttributes['id']+"TAB"+"name:"+event.target.name+"TAB"+"event:"+mapAttributes['event']+"TAB"+"value:"+mapAttributes['value'];
	//console.log("Rebuilt Message is:"+message);
	
	logEvent(event);
});

// ///////////////
jq101("select,radio,checkbox,a,input[type='radio'],input[type='checkbox']").bindFirst('change',function()
{ 
	logEvent(event);

});

jq101("a,submit,button,input[type='image'],input[type='submit']").bindFirst('click',function()
{ 
	logEvent(event);
});

jq101(":button").bindFirst('click',function()
{ 
	logEvent(event);
}); 

jq101("input[type='text'], textarea, input[type='password']").bindFirst('blur',function()
{ 
		logEvent(event);	
});

//jQuery("input:disabled").each( function() { 
//	jq101(this).bindFirst('click',function() { 
//		logEvent(event);	
//	});
//	jq101(this).prop('disabled', true);
//});

function getPosition(event){
	var position ="top:"+ jq101(event.target).position().top+"TAB"+"left:"+ jq101(event.target).position().left+"TABwidth:"+jq101(event.target).width()+"TABheight:"+jq101(event.target).height()+"TAB"+"tag:"+jq101(event.target).prop('tagName')+"TAB";
	return position;
}

function logEvent(event){
	//$("#noticeboard").append("<p>record("+event.target.name+","+event.target.id+","+event.target.type+","+event.target.val()+")</p>");
	var message="";
	var isCheckbox = jq101(event.target).is(":checkbox");
	var isRadio = jq101(event.target).is(":radio");
	var isText = jq101(event.target).is(":text");
	var isImage = jq101(event.target).is(":image");
	var isButton = jq101(event.target).is(":button");
	var isSubmit = jq101(event.target).is(":submit");
	var isSelect = event.target.tagName=="SELECT" || event.target.tagName=="select" || event.target.tagName=="select-one";
	var isAnchor = jq101(event.target).is('a');				
	var type="type:"+event.target.type+"TAB"; 
	//For position
	var position =getPosition(event);
	console.log("The position is :"+position); 


	message = "id:"+event.target.id+"TAB"+"name:"+event.target.name+"TAB"+"event:"+event.type+"TAB";
	if(isCheckbox){
		message+="value:"+event.target.checked;
	}
	else if(isRadio){
		message+="value:"+event.target.checked;
	}
	else if(isSubmit || isButton || isImage){
		message+="value:"+jq101(event.target).attr("value");
	}
	else if(isAnchor){
		type="type:a"+"TAB";
		message+="value:"+jq101(event.target).text()+"TAB"+"href:"+jq101(event.target).attr('href');
	}else if(isSelect){
		message+="value:"+ jq101("option:selected", event.target).text()+"("+event.target.value+")";	
		options="";
	    if(event.target.tagName){
	        selector=event.target.tagName+"[name='"+event.target.name+"']";
	    }else if (event.target.id){
	        selector="#"+event.target.id+" ";
	    }
	    jq101(selector+" option").each(function () {
	            options += jq101(this).text() + ";";
	  	});
		if(options){
			message+="TAB options:"+options;
		}
	}	
	else{
	    message+= "value:"+event.target.value;		    
	    textElementName=event.target.name;
	    textEvents.push(event);	
	    return;	 //will be handled by timer above, so no need to send msg   
	}
	message= position+"attid:"+jq101(event.target).attr('attid')+"TAB"+"url:"+jq101(location).attr('href')+"TAB"+type+message;
	sendEventToJavaProgram(message);
	//wait for 1 sec if it is button. This gives some time to record before form is submitted. Crucial for submit buttons 
	if(isButton || isSubmit){
		//Bad delay function but have to use coz there is no blocking delay in JS
		ineffectiveDelay(300);
	}
	//alert("Hi, Inside Log Event: "+message+",isButton:"+isButton+",isSubmit:"+isSubmit);

}

////////////////////////////////////////////////
//SECTION 5: Record Events
var textFlag=0;
var textElementName="";
setInterval(function(){clock()},1000);
var textEvents=new Array(); 
function clock()
  {
	while(textEvents.length>0){
		var event=textEvents.pop();
		//if we have some value, then we send, otherwise just ignore.
		//if(event.target.value){
			//For position
			var position = getPosition(event);
			console.log("The position is :"+position); 

			var message =position+"attid:"+jq101(event.target).attr('attid')+"TAB"+ "url:"+jq101(location).attr('href')+"TAB"+"type:"+event.target.type+"TAB"+"id:"+event.target.id+"TAB"+"name:"+event.target.name+"TAB"+"event:"+event.type+"TAB"+"value:"+event.target.value;		
			sendEventToJavaProgram(message);
		//}
	}
      
  }
 function sendEventToJavaProgram(message){
	 //jq101.get("http://localhost:4444/"+message);
	 //percentage chars are replaced by PERCENTAGE
	 message=message.replace(/%/g, 'PERCENTAGE');
 
	 console.log('Sending Event: '+message);
	 
	 jq101.ajax({
		    type: "post", url: "http://localhost:4444/"+message,
		    success: function (data, text) {
		    	//
		    },
		    error: function (request, status, error) {
		        //alert(request.responseText);
		    	console.log(request.responseText);
		    	//Try one more time
		    	console.log("Trying to resend one more time");
		    	jq101.get("http://localhost:4444/"+message);
		    }
		});
}
 
 function getMessage(event){	
		var message="";
		var isCheckbox = jq101(event.target).is(":checkbox");
		var isRadio = jq101(event.target).is(":radio");
		var isText = jq101(event.target).is(":text");
		var isImage = jq101(event.target).is(":image");
		var isButton = jq101(event.target).is(":button");
		var isSubmit = jq101(event.target).is(":submit");
		var isSelect = event.target.tagName=="SELECT" || event.target.tagName=="select" || event.target.tagName=="select-one";
		var isAnchor = jq101(event.target).is('a');				
		var type="type:"+event.target.type+"TAB";
		var position =getPosition(event);

		console.log("The position is :"+position); 
		message = "id:"+event.target.id+"TAB"+"name:"+event.target.name+"TAB"+"event:"+event.type+"TAB";
		
		if(isCheckbox){
			message+="value:"+event.target.checked;
		}
		else if(isRadio){
			message+="value:"+event.target.checked;
		}
		else if(isSubmit || isButton || isImage){
			message+="value:"+jq101(event.target).attr("value");
		}
		else if(isAnchor){
			type="type:a"+"TAB";
			message+="value:"+jq101(event.target).text()+"TAB"+"href:"+jq101(event.target).attr('href');
		}else if(isSelect){
			message+="value:"+ jq101("option:selected", event.target).text()+"("+event.target.value+")";	
			options="";
		    if(event.target.tagName){
		        selector=event.target.tagName+"[name='"+event.target.name+"']";
		    }else if (event.target.id){
		        selector="#"+event.target.id+" ";
		    }
		    jq101(selector+" option").each(function () {
		            options += jq101(this).text() + ";";
		  	});
			if(options){
				message+="TAB options:"+options;
			}
		}	
		else{
			var message =position+"attid:"+jq101(event.target).attr('attid')+"TAB"+ "url:"+jq101(location).attr('href')+"TAB"+"type:"+event.target.type+"TAB"+"id:"+event.target.id+"TAB"+"name:"+event.target.name+"TAB"+"event:"+event.type+"TAB"+"value:"+event.target.value;		
		    return message;	    
		}
		message=position+ "attid:"+jq101(event.target).attr('attid')+"TAB"+"url:"+jq101(location).attr('href')+"TAB"+type+message;
		
	 return message;
}
 
 //Very ineffective blocking wait
 function ineffectiveDelay(ms) {
	    var start = +(new Date());
	    while (new Date() - start < ms);
	}

