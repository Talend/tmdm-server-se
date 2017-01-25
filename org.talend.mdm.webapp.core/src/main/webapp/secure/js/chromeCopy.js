document.onselectstart = function(event) {
	if (!isChrome()) {
		return;
	}

	if (window.event) {
		event = window.event;
	}
	try {
		var the = event.srcElement;
		if (the.nodeName == '#text') {
			if (the.parentNode.className != undefined
					&& the.parentNode.className.indexOf("grid") != -1) {
				event.returnValue = false;
				return;
			} else {
				event.returnValue = true;
				return;
			}
		}
		if (the.className != undefined && the.className.indexOf("grid") != -1) {
			event.returnValue = false;
		} else {
			event.returnValue = true;
		}
		event.preventDefault();
	} catch (e) {
		event.returnValue = false;
	}
}

function match(browser) {
	var i = navigator.userAgent.toLowerCase().indexOf(browser);
	return i >= 0;
}

function isIe() {
	return match("msie");
}

function isChrome() {
	return match("chrome");
}
