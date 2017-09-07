methodChange = function(_method){
	method = _method;
	resetPanel();
};

resetPanel = function(){
	switch(method){
		case 'debitcard':{
			document.getElementById('prepaid_panel').style.display = 'none';
			document.getElementById('debitcard_panel').style.display = 'block';
			document.getElementById('creditcard_panel').style.display = 'none';
			document.getElementById('ali_panel').style.display = 'none';
		}break;
		case 'creditcard':{
			document.getElementById('prepaid_panel').style.display = 'none';
			document.getElementById('debitcard_panel').style.display = 'none';
			document.getElementById('creditcard_panel').style.display = 'block';
			document.getElementById('ali_panel').style.display = 'none';
		}break;
		case 'ali':{
			document.getElementById('prepaid_panel').style.display = 'none';
			document.getElementById('debitcard_panel').style.display = 'none';
			document.getElementById('creditcard_panel').style.display = 'none';
			document.getElementById('ali_panel').style.display = 'block';
		}break;
		default:{
			document.getElementById('prepaid_panel').style.display = 'block';
			document.getElementById('debitcard_panel').style.display = 'none';
			document.getElementById('creditcard_panel').style.display = 'none';
			document.getElementById('ali_panel').style.display = 'none';
		}break;
	}
};

setHiddenValue = function(hiddenId, spanId, price, subjectId, month){
	document.getElementById(hiddenId).value = price;
	document.getElementById(spanId).innerHTML = price + '元';
	document.getElementById(subjectId).value = '购买VIP:' + month + '个月';
};

submitForm = function(button, formid){
	button.value='提交请求中...';button.disabled=true;
	document.getElementById(formid).submit();
}