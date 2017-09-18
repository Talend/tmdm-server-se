document.onselectstart = function(event) {
    if (getBrowserType() != "Chrome") {
        return;
    }

    // Fix Chrome can't select correct content.
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

// check browser type
function getBrowserType() {
    var userAgent = navigator.userAgent;
    if (isWindows10Edge = userAgent.indexOf("Windows NT 10.0;") > -1
            && userAgent.indexOf("Edge") > -1) {
        event.returnValue = true;
        return "Windows10Edge";
    } else if (userAgent.indexOf("Opera") > -1) {
        return "Opera";
    } else if (userAgent.indexOf("compatible") > -1
            && userAgent.indexOf("MSIE") > -1) {
        var reIE = new RegExp("MSIE (\\d+\\.\\d+);");
        reIE.test(userAgent);
        var fIEVersion = parseFloat(RegExp["$1"]);
        if (fIEVersion == 7) {
            return "IE7";
        } else if (fIEVersion == 8) {
            return "IE8";
        } else if (fIEVersion == 9) {
            return "IE9";
        } else if (fIEVersion == 10) {
            return "IE10";
        } else if (fIEVersion == 11) {
            return "IE11";
        } else {
            return "";
        }
    } else if (userAgent.indexOf("Windows NT 6.1; Trident/7.0;") > -1) {
        return "Edge";
    } else if (userAgent.indexOf("Firefox") > -1) {
        return "Firefox";
    } else if (userAgent.indexOf("Safari") > -1
            && userAgent.indexOf("Chrome") == -1) {
        event.returnValue = true;
        return "Safari";
    } else if (userAgent.indexOf("Chrome") > -1
            && userAgent.indexOf("Safari") > -1) {
        return "Chrome";
    } else {
        return "";
    }
}
