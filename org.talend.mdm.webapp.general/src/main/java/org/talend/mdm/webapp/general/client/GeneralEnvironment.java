/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client;


public class GeneralEnvironment {

    public static void loadAll() {
        loadExt();
        loadFormat();
        loadDomQuery();
        loadDomHelper();
        loadTemplate();
        loadXTemplate();
    }

    private native static void loadExt() /*-{
        var document = $doc;
        var window = $wnd;
        $wnd.GXT = {};
        $wnd.GXT.Ext = {};
        var Ext = $wnd.GXT.Ext;
        window["undefined"] = window["undefined"];
        Ext.apply = function(o, c, defaults){
        if(defaults){
        // no "this" reference for friendly out of scope calls
        Ext.apply(o, defaults);
        }
        if(o && c && typeof c == 'object'){
        for(var p in c){
        o[p] = c[p];
        }
        }
        return o;
        };
        var initload = function(){
        var idSeed = 0;
        Ext.apply(Ext, {
        emptyFn : function(){},
        applyIf : function(o, c){
        if(o && c){
        for(var p in c){
        if(typeof o[p] == "undefined"){ o[p] = c[p]; }
        }
        }
        return o;
        },

        isArray : function(v){
        return v && typeof v.pop == 'function';
        },
        extend : function(){
        // inline overrides
        var io = function(o){
        for(var m in o){
        this[m] = o[m];
        }
        };
        return function(sb, sp, overrides){
        if(typeof sp == 'object'){
        overrides = sp;
        sp = sb;
        sb = function(){sp.apply(this, arguments);};
        }
        var F = function(){}, sbp, spp = sp.prototype;
        F.prototype = spp;
        sbp = sb.prototype = new F();
        sbp.constructor=sb;
        sb.superclass=spp;
        if(spp.constructor == Object.prototype.constructor){
        spp.constructor=sp;
        }
        sb.override = function(o){
        Ext.override(sb, o);
        };
        sbp.override = io;
        Ext.override(sb, overrides);
        return sb;
        };
        }(),
        override : function(origclass, overrides){
        if(overrides){
        var p = origclass.prototype;
        for(var method in overrides){
        p[method] = overrides[method];
        }
        }
        },
        getDom : function(el){
        if(!el || !document){
        return null;
        }
        return el.dom ? el.dom : (typeof el == 'string' ? document.getElementById(el) : el);
        }
        });
        };
        initload();
        $wnd.GXT.Ext = Ext;
    }-*/;


    private native static void loadFormat() /*-{
        var Ext = $wnd.GXT.Ext;
        Ext.util = {};
        Ext.util.Format = function(){
        var trimRe = /^\s+|\s+$/g;
        return {
        ellipsis : function(value, len){
        if(value && value.length > len){
        return value.substr(0, len-3)+"...";
        }
        return value;
        },
        undef : function(value){
        return value !== undefined ? value : "";
        },
        defaultValue : function(value, defaultValue){
        return value !== undefined && value !== '' ? value : defaultValue;
        },
        htmlEncode : function(value){
        return !value ? value : String(value).replace(/&/g, "&amp;").replace(/>/g, "&gt;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
        },
        htmlDecode : function(value){
        return !value ? value : String(value).replace(/&amp;/g, "&").replace(/&gt;/g, ">").replace(/&lt;/g, "<").replace(/&quot;/g, '"');
        },
        trim : function(value){
        return String(value).replace(trimRe, "");
        },
        substr : function(value, start, length){
        return String(value).substr(start, length);
        },
        lowercase : function(value){
        return String(value).toLowerCase();
        },
        uppercase : function(value){
        return String(value).toUpperCase();
        },
        capitalize : function(value){
        return !value ? value : value.charAt(0).toUpperCase() + value.substr(1).toLowerCase();
        },
        call : function(value, fn){
        if(arguments.length > 2){
        var args = Array.prototype.slice.call(arguments, 2);
        args.unshift(value);
        return eval(fn).apply(window, args);
        }else{
        return eval(fn).call(window, value);
        }
        },
        usMoney : function(v){
        v = (Math.round((v-0)*100))/100;
        v = (v == Math.floor(v)) ? v + ".00" : ((v*10 == Math.floor(v*10)) ? v + "0" : v);
        v = String(v);
        var ps = v.split('.');
        var whole = ps[0];
        var sub = ps[1] ? '.'+ ps[1] : '.00';
        var r = /(\d+)(\d{3})/;
        while (r.test(whole)) {
        whole = whole.replace(r, '$1' + ',' + '$2');
        }
        v = whole + sub;
        if(v.charAt(0) == '-'){
        return '-$' + v.substr(1);
        }
        return "$" +  v;
        },


        date : function(v, format){
        if(!v){
        return "";
        }
        if(!(v instanceof Date)){
        v = new Date(Date.parse(v));
        }
        return @com.extjs.gxt.ui.client.util.DateWrapper::format(FLjava/lang/String;)(v.getTime(), format || "m/d/Y");
        },

        // private
        stripTagsRE : /<\/?[^>]+>/gi,


        stripTags : function(v){
        return !v ? v : String(v).replace(this.stripTagsRE, "");
        },

        stripScriptsRe : /(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)/ig,


        stripScripts : function(v){
        return !v ? v : String(v).replace(this.stripScriptsRe, "");
        },


        fileSize : function(size){
        if(size < 1024) {
        return size + " bytes";
        } else if(size < 1048576) {
        return (Math.round(((size*10) / 1024))/10) + " KB";
        } else {
        return (Math.round(((size*10) / 1048576))/10) + " MB";
        }
        },

        math : function(){
        var fns = {};
        return function(v, a){
        if(!fns[a]){
        fns[a] = new Function('v', 'return v ' + a + ';');
        }
        return fns[a](v);
        }
        }()
        };
        }();
    }-*/;

    private native static void loadDomQuery() /*-{
        var Ext = $wnd.GXT.Ext;
        Ext.DomQuery = function() {
        var cache      = {
        }, simpleCache = {
        }, valueCache  = {
        };
        var nonSpace = /\S/;
        var trimRe = /^\s+|\s+$/g;
        var tplRe = /\{(\d+)\}/g;
        var modeRe = /^(\s?[\/>+~]\s?|\s|$)/;
        var tagTokenRe = /^(#)?([\w-\*]+)/;
        var nthRe = /(\d*)n\+?(\d*)/, nthRe2 = /\D/;
        var document = $doc;
        function child(p, index) {
        var i = 0;
        var n = p.firstChild;
        while (n) {
        if (n.nodeType == 1) {
        if (++ i == index) {
        return n;
        }
        }
        n = n.nextSibling;
        }
        return null;
        }
        ;
        function next(n) {
        while ((n = n.nextSibling) && n.nodeType != 1);
        return n;
        }
        ;
        function prev(n) {
        while ((n = n.previousSibling) && n.nodeType != 1);
        return n;
        }
        ;
        function children(d) {
        var n = d.firstChild, ni = - 1;
        while (n) {
        var nx = n.nextSibling;
        if (n.nodeType == 3 && ! nonSpace.test(n.nodeValue)) {
        d.removeChild(n);
        } else {
        n.nodeIndex = ++ ni;
        }
        n = nx;
        }
        return this;
        }
        ;
        $wnd.GXT.__byClassName = function(c, a, v) {
        if (! v) {
        return c;
        }
        var r = [], ri = - 1, cn;
        for (var i = 0, ci; ci = c[i]; i++) {
        if ((' ' + ci.className + ' ').indexOf(v) != - 1) {
        r[++ ri] = ci;
        }
        }
        return r;
        };
        $wnd.GXT.__attrValue = function attrValue(n, attr) {
        if (!n.tagName && typeof n.length != "undefined") {
        n = n[0];
        }
        if (! n) {
        return null;
        }
        if (attr == "for") {
        return n.htmlFor;
        }
        if (attr == "class" || attr == "className") {
        return n.className;
        }
        return n.getAttribute(attr) || n[attr];
        };
        $wnd.GXT.__getNodes = function(ns, mode, tagName) {
        var result = [], ri = - 1, cs;
        if (! ns) {
        return result;
        }
        tagName = tagName || "*";
        if (typeof ns.getElementsByTagName != "undefined") {
        ns = [ns];
        }
        if (! mode) {
        for (var i = 0, ni; ni = ns[i]; i++) {
        cs = ni.getElementsByTagName(tagName);
        for (var j = 0, ci; ci = cs[j]; j++) {
        result[++ ri] = ci;
        }
        }
        } else if (mode == "/" || mode == ">") {
        var utag = tagName.toUpperCase();
        for (var i = 0, ni, cn; ni = ns[i]; i++) {
        cn = ni.children || ni.childNodes;
        for (var j = 0, cj; cj = cn[j]; j++) {
        if (cj.nodeName == utag || cj.nodeName == tagName || tagName == '*') {
        result[++ ri] = cj;
        }
        }
        }
        } else if (mode == "+") {
        var utag = tagName.toUpperCase();
        for (var i = 0, n; n = ns[i]; i++) {
        while ((n = n.nextSibling) && n.nodeType != 1);
        if (n && (n.nodeName == utag || n.nodeName == tagName || tagName == '*')) {
        result[++ ri] = n;
        }
        }
        } else if (mode == "~") {
        for (var i = 0, n; n = ns[i]; i++) {
        while ((n = n.nextSibling)
        && (n.nodeType != 1 || (tagName == '*' || n.tagName.toLowerCase() != tagName)));
        if (n) {
        result[++ ri] = n;
        }
        }
        }
        return result;
        };
        function concat(a, b) {
        if (b.slice) {
        return a.concat(b);
        }
        for (var i = 0, l = b.length; i < l; i++) {
        a[a.length] = b[i];
        }
        return a;
        }
        $wnd.GXT.__byTag = function(cs, tagName) {
        if (cs.tagName || cs == document) {
        cs = [cs];
        }
        if (! tagName) {
        return cs;
        }
        var r = [], ri = - 1;
        tagName = tagName.toLowerCase();
        for (var i = 0, ci; ci = cs[i]; i++) {
        if (ci.nodeType == 1 && ci.tagName.toLowerCase() == tagName) {
        r[++ ri] = ci;
        }
        }
        return r;
        };
        $wnd.GXT.__byId = function(cs, attr, id) {
        if (cs.tagName || cs == document) {
        cs = [cs];
        }
        if (! id) {
        return cs;
        }
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = cs[i]; i++) {
        if (ci && ci.id == id) {
        r[++ ri] = ci;
        return r;
        }
        }
        return r;
        };
        $wnd.GXT.__byAttribute = function(cs, attr, value, op, custom) {
        var r = [], ri = - 1, st = custom == "{";
        var f = Ext.DomQuery.operators[op];
        for (var i = 0, ci; ci = cs[i]; i++) {
        var a;
        if (st) {
        a = Ext.DomQuery.getStyle(ci, attr);
        } else if (attr == "class" || attr == "className") {
        a = ci.className;
        } else if (attr == "for") {
        a = ci.htmlFor;
        } else if (attr == "href") {
        a = ci.getAttribute("href", 2);
        } else {
        a = ci.getAttribute(attr);
        }
        if ((f && f(a, value)) || (! f && a)) {
        r[++ ri] = ci;
        }
        }
        return r;
        };
        $wnd.GXT.__byPseudo = function(cs, name, value) {
        return Ext.DomQuery.pseudos[name](cs, value);
        } ;
        // This is for IE MSXML which does not support expandos.
        // IE runs the same speed using setAttribute, however FF slows way down
        // and Safari completely fails so they need to continue to use expandos.
        var isIE = window.ActiveXObject ? true : false;
        // this eval is stop the compressor from
        // renaming the variable to something shorter
        eval("var batch = 30803;");
        var key = 30803;
        function nodupIEXml(cs) {
        var d = ++ key;
        cs[0].setAttribute("_nodup", d);
        var r = [cs[0]];
        for (var i = 1, len = cs.length; i < len; i++) {
        var c = cs[i];
        if (! c.getAttribute("_nodup") != d) {
        c.setAttribute("_nodup", d);
        r[r.length] = c;
        }
        }
        for (var i = 0, len = cs.length; i < len; i++) {
        cs[i].removeAttribute("_nodup");
        }
        return r;
        }
        $wnd.GXT.___nodup = function(cs) {
        if (! cs) {
        return [];
        }
        var len = cs.length, c, i, r = cs, cj, ri = - 1;
        if (! len || typeof cs.nodeType != "undefined" || len == 1) {
        return cs;
        }
        if (isIE && typeof cs[0].selectSingleNode != "undefined") {
        return nodupIEXml(cs);
        }
        var d = ++ key;
        cs[0]._nodup = d;
        for (i = 1; c = cs[i]; i++) {
        if (c._nodup != d) {
        c._nodup = d;
        } else {
        r = [];
        for (var j = 0; j < i; j++) {
        r[++ ri] = cs[j];
        }
        for (j = i + 1; cj = cs[j]; j++) {
        if (cj._nodup != d) {
        cj._nodup = d;
        r[++ ri] = cj;
        }
        }
        return r;
        }
        }
        return r;
        }
        function quickDiffIEXml(c1, c2) {
        var d = ++ key;
        for (var i = 0, len = c1.length; i < len; i++) {
        c1[i].setAttribute("_qdiff", d);
        }
        var r = [];
        for (var i = 0, len = c2.length; i < len; i++) {
        if (c2[i].getAttribute("_qdiff") != d) {
        r[r.length] = c2[i];
        }
        }
        for (var i = 0, len = c1.length; i < len; i++) {
        c1[i].removeAttribute("_qdiff");
        }
        return r;
        }
        function quickDiff(c1, c2) {
        var len1 = c1.length;
        if (! len1) {
        return c2;
        }
        if (isIE && c1[0].selectSingleNode) {
        return quickDiffIEXml(c1, c2);
        }
        var d = ++ key;
        for (var i = 0; i < len1; i++) {
        c1[i]._qdiff = d;
        }
        var r = [];
        for (var i = 0, len = c2.length; i < len; i++) {
        if (c2[i]._qdiff != d) {
        r[r.length] = c2[i];
        }
        }
        return r;
        }
        $wnd.GXT.__quickId = function quickId(ns, mode, root, id) {
        if (ns == root) {
        var d = root.ownerDocument || root;
        return d.getElementById(id);
        }
        ns = $wnd.GXT.__getNodes(ns, mode, "*");
        return $wnd.GXT.__byId(ns, null, id);
        }
        return {
        getStyle: function(el, name) {
        return @com.extjs.gxt.ui.client.core.El::fly(Lcom/google/gwt/dom/client/Element;)(el).@com.extjs.gxt.ui.client.core.El::getStyleAttribute(Ljava/lang/String;)(name);
        },
        compile: function(path, type) {
        type = type || "select";
        var fn = ["var f = function(root){\n var mode; ++batch; var n = root || document;\n"];
        var q = path, mode, lq;
        var tk = Ext.DomQuery.matchers;
        var tklen = tk.length;
        var mm;
        // accept leading mode switch
        var lmode = q.match(modeRe);
        if (lmode && lmode[1]) {
        fn[fn.length] = 'mode="' + lmode[1].replace(trimRe, "") + '";';
        q = q.replace(lmode[1], "");
        }
        // strip leading slashes
        while (path.substr(0, 1) == "/") {
        path = path.substr(1);
        }
        while (q && lq != q) {
        lq = q;
        var tm = q.match(tagTokenRe);
        if (type == "select") {
        if (tm) {
        if (tm[1] == "#") {
        fn[fn.length] = 'n = $wnd.GXT.__quickId(n, mode, root, "' + tm[2] + '");';
        } else {
        fn[fn.length] = 'n = $wnd.GXT.__getNodes(n, mode, "' + tm[2] + '");';
        }
        q = q.replace(tm[0], "");
        } else if (q.substr(0, 1) != '@') {
        fn[fn.length] = 'n = $wnd.GXT.__getNodes(n, mode, "*");';
        }
        } else {
        if (tm) {
        if (tm[1] == "#") {
        fn[fn.length] = 'n = $wnd.GXT.__byId(n, null, "' + tm[2] + '");';
        } else {
        fn[fn.length] = 'n = $wnd.GXT.__byTag(n, "' + tm[2] + '");';
        }
        q = q.replace(tm[0], "");
        }
        }
        while (!(mm = q.match(modeRe))) {
        var matched = false;
        for (var j = 0; j < tklen; j++) {
        var t = tk[j];
        var m = q.match(t.re);
        if (m) {
        fn[fn.length] = t.select.replace(tplRe, function(x, i) {
        return m[i];
        } );
        q = q.replace(m[0], "");
        matched = true;
        break;
        }
        }
        // prevent infinite loop on bad selector
        if (! matched) {
        throw 'Error parsing selector, parsing failed at "' + q + '"';
        }
        }
        if (mm[1]) {
        fn[fn.length] = 'mode="' + mm[1].replace(trimRe, "") + '";';
        q = q.replace(mm[1], "");
        }
        }
        fn[fn.length] = "return $wnd.GXT.___nodup(n);\n}";
        eval(fn.join(""));
        return f;
        },
        select: function(path, root, type) {
        if (! root || root == document) {
        root = document;
        }
        if (typeof root == "string") {
        root = document.getElementById(root);
        }
        var paths = path.split(",");
        var results = [];
        for (var i = 0, len = paths.length; i < len; i++) {
        var p = paths[i].replace(trimRe, "");
        if (! cache[p]) {
        cache[p] = Ext.DomQuery.compile(p);
        if (! cache[p]) {
        throw p + " is not a valid selector";
        }
        }
        var result = cache[p](root);
        if (result && result != document) {
        results = results.concat(result);
        }
        }
        if (paths.length > 1) {
        return $wnd.GXT.___nodup(results);
        }
        return results;
        },
        selectNode: function(path, root) {
        return Ext.DomQuery.select(path, root)[0];
        },
        selectValue: function(path, root, defaultValue) {
        path = path.replace(trimRe, "");
        if (! valueCache[path]) {
        valueCache[path] = Ext.DomQuery.compile(path, "select");
        }
        var n = valueCache[path](root);
        n = n[0] ? n[0] : n;
        var v = (n && n.firstChild ? n.firstChild.nodeValue : null);
        return ((v === null || v === undefined || v === '') ? defaultValue : v);
        },
        selectNumber: function(path, root, defaultValue) {
        var v = Ext.DomQuery.selectValue(path, root, defaultValue || 0);
        return parseFloat(v);
        },
        is: function(el, ss) {
        if (typeof el == "string") {
        el = document.getElementById(el);
        }
        var isArray = (el instanceof Array);
        var result = Ext.DomQuery.filter(isArray ? el : [el], ss);
        return isArray ? (result.length == el.length) : (result.length > 0);
        },
        filter: function(els, ss, nonMatches) {
        ss = ss.replace(trimRe, "");
        if (! simpleCache[ss]) {
        simpleCache[ss] = Ext.DomQuery.compile(ss, "simple");
        }
        var result = simpleCache[ss](els);
        return nonMatches ? quickDiff(result, els) : result;
        },
        matchers: [
        {
        re: /^\.([\w-]+)/,
        select: 'n = $wnd.GXT.__byClassName(n, null, " {1} ");'
        },
        {
        re: /^\:([\w-]+)(?:\(((?:[^\s>\/]*|.*?))\))?/,
        select: 'n = $wnd.GXT.__byPseudo(n, "{1}", "{2}");'
        },
        {
        re: /^(?:([\[\{])(?:@)?([\w-]+)\s?(?:(=|.=)\s?['"]?(.*?)["']?)?[\]\}])/,
        select: 'n = $wnd.GXT.__byAttribute(n, "{2}", "{4}", "{3}", "{1}");'
        },
        {
        re: /^#([\w-]+)/,
        select: 'n = $wnd.GXT.__byId(n, null, "{1}");'
        },
        {
        re: /^@([\w-]+)/,
        select: 'return {firstChild:{nodeValue:$wnd.GXT.__attrValue(n, "{1}")}};'
        }
        ],
        operators: {
        "=": function(a, v) {
        return a == v;
        },
        "!=": function(a, v) {
        return a != v;
        },
        "^=": function(a, v) {
        return a && a.substr(0, v.length) == v;
        },
        "$=": function(a, v) {
        return a && a.substr(a.length - v.length) == v;
        },
        "*=": function(a, v) {
        return a && a.indexOf(v) !== - 1;
        },
        "%=": function(a, v) {
        return (a % v) == 0;
        },
        "|=": function(a, v) {
        return a && (a == v || a.substr(0, v.length + 1) == v + '-');
        },
        "~=": function(a, v) {
        return a && (' ' + a + ' ').indexOf(' ' + v + ' ') != - 1;
        }
        },
        pseudos: {
        "first-child": function(c) {
        var r = [], ri = - 1, n;
        for (var i = 0, ci; ci = n = c[i]; i++) {
        while ((n = n.previousSibling) && n.nodeType != 1);
        if (! n) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "last-child": function(c) {
        var r = [], ri = - 1, n;
        for (var i = 0, ci; ci = n = c[i]; i++) {
        while ((n = n.nextSibling) && n.nodeType != 1);
        if (! n) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "nth-child": function(c, a) {
        var r = [], ri = - 1;
        var m = nthRe.exec(a == "even" && "2n" || a == "odd" && "2n+1" || ! nthRe2.test(a) && "n+" + a || a);
        var f = (m[1] || 1) - 0, l = m[2] - 0;
        for (var i = 0, n; n = c[i]; i++) {
        var pn = n.parentNode;
        if (batch != pn._batch) {
        var j = 0;
        for (var cn = pn.firstChild; cn; cn = cn.nextSibling) {
        if (cn.nodeType == 1) {
        cn.nodeIndex = ++ j;
        }
        }
        pn._batch = batch;
        }
        if (f == 1) {
        if (l == 0 || n.nodeIndex == l) {
        r[++ ri] = n;
        }
        } else if ((n.nodeIndex + l) % f == 0) {
        r[++ ri] = n;
        }
        }
        return r;
        },
        "only-child": function(c) {
        var r = [], ri = - 1;
        ;
        for (var i = 0, ci; ci = c[i]; i++) {
        if (! prev(ci) && ! next(ci)) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "empty": function(c) {
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        var cns = ci.childNodes, j = 0, cn, empty = true;
        while (cn = cns[j]) {
        ++ j;
        if (cn.nodeType == 1 || cn.nodeType == 3) {
        empty = false;
        break;
        }
        }
        if (empty) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "contains": function(c, v) {
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        if ((ci.textContent || ci.innerText || '').indexOf(v) != - 1) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "nodeValue": function(c, v) {
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        if (ci.firstChild && ci.firstChild.nodeValue == v) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "checked": function(c) {
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        if (ci.checked == true) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "not": function(c, ss) {
        return Ext.DomQuery.filter(c, ss, true);
        },
        "any": function(c, selectors) {
        var ss = selectors.split('|');
        var r = [], ri = - 1, s;
        for (var i = 0, ci; ci = c[i]; i++) {
        for (var j = 0; s = ss[j]; j++) {
        if (Ext.DomQuery.is(ci, s)) {
        r[++ ri] = ci;
        break;
        }
        }
        }
        return r;
        },
        "odd": function(c) {
        return this["nth-child"](c, "odd");
        },
        "even": function(c) {
        return this["nth-child"](c, "even");
        },
        "nth": function(c, a) {
        return c[a - 1] || [];
        },
        "first": function(c) {
        return c[0] || [];
        },
        "last": function(c) {
        return c[c.length - 1] || [];
        },
        "has": function(c, ss) {
        var s = Ext.DomQuery.select;
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        if (s(ss, ci).length > 0) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "next": function(c, ss) {
        var is = Ext.DomQuery.is;
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        var n = next(ci);
        if (n && is(n, ss)) {
        r[++ ri] = ci;
        }
        }
        return r;
        },
        "prev": function(c, ss) {
        var is = Ext.DomQuery.is;
        var r = [], ri = - 1;
        for (var i = 0, ci; ci = c[i]; i++) {
        var n = prev(ci);
        if (n && is(n, ss)) {
        r[++ ri] = ci;
        }
        }
        return r;
        }
        }
        };
        }();
    }-*/;

    private native static void loadDomHelper() /*-{
        var Ext = $wnd.GXT.Ext;
        Ext.DomHelper = function(){
        var tempTableEl = null;
        var emptyTags = /^(?:br|frame|hr|img|input|link|meta|range|spacer|wbr|area|param|col)$/i;
        var tableRe = /^table|tbody|tr|td$/i;


        var createHtml = function(o){
        if(typeof o == 'string'){
        return o;
        }
        var b = "";
        if(!o.tag){
        o.tag = "div";
        }
        b += "<" + o.tag;
        for(var attr in o){
        if(attr == "tag" || attr == "children" || attr == "cn" || attr == "html" || typeof o[attr] == "function") continue;
        if(attr == "style"){
        var s = o["style"];
        if(typeof s == "function"){
        s = s.call();
        }
        if(typeof s == "string"){
        b += ' style="' + s + '"';
        }else if(typeof s == "object"){
        b += ' style="';
        for(var key in s){
        if(typeof s[key] != "function"){
        b += key + ":" + s[key] + ";";
        }
        }
        b += '"';
        }
        }else{
        if(attr == "cls"){
        b += ' class="' + o["cls"] + '"';
        }else if(attr == "htmlFor"){
        b += ' for="' + o["htmlFor"] + '"';
        }else{
        b += " " + attr + '="' + o[attr] + '"';
        }
        }
        }
        if(emptyTags.test(o.tag)){
        b += "/>";
        }else{
        b += ">";
        var cn = o.children || o.cn;
        if(cn){
        if(cn instanceof Array){
        for(var i = 0, len = cn.length; i < len; i++) {
        b += createHtml(cn[i], b);
        }
        }else{
        b += createHtml(cn, b);
        }
        }
        if(o.html){
        b += o.html;
        }
        b += "</" + o.tag + ">";
        }
        return b;
        };


        var createDom = function(o, parentNode){
        var el = document.createElement(o.tag||'div');
        var useSet = el.setAttribute ? true : false; // In IE some elements don't have setAttribute
        for(var attr in o){
        if(attr == "tag" || attr == "children" || attr == "cn" || attr == "html" || attr == "style" || typeof o[attr] == "function") continue;
        if(attr=="cls"){
        el.className = o["cls"];
        }else{
        if(useSet) el.setAttribute(attr, o[attr]);
        else el[attr] = o[attr];
        }
        }
        Ext.DomHelper.applyStyles(el, o.style);
        var cn = o.children || o.cn;
        if(cn){
        if(cn instanceof Array){
        for(var i = 0, len = cn.length; i < len; i++) {
        createDom(cn[i], el);
        }
        }else{
        createDom(cn, el);
        }
        }
        if(o.html){
        el.innerHTML = o.html;
        }
        if(parentNode){
        parentNode.appendChild(el);
        }
        return el;
        };

        var ieTable = function(depth, s, h, e){
        tempTableEl.innerHTML = [s, h, e].join('');
        var i = -1, el = tempTableEl;
        while(++i < depth){
        el = el.firstChild;
        }
        return el;
        };

        // kill repeat to save bytes
        var ts = '<table>',
        te = '</table>',
        tbs = ts+'<tbody>',
        tbe = '</tbody>'+te,
        trs = tbs + '<tr>',
        tre = '</tr>'+tbe;


        var insertIntoTable = function(tag, where, el, html){
        if(!tempTableEl){
        tempTableEl = document.createElement('div');
        }
        var node;
        var before = null;
        if(tag == 'td'){
        if(where == 'afterbegin' || where == 'beforeend'){ // INTO a TD
        return;
        }
        if(where == 'beforebegin'){
        before = el;
        el = el.parentNode;
        } else{
        before = el.nextSibling;
        el = el.parentNode;
        }
        node = ieTable(4, trs, html, tre);
        }
        else if(tag == 'tr'){
        if(where == 'beforebegin'){
        before = el;
        el = el.parentNode;
        node = ieTable(3, tbs, html, tbe);
        } else if(where == 'afterend'){
        before = el.nextSibling;
        el = el.parentNode;
        node = ieTable(3, tbs, html, tbe);
        } else{ // INTO a TR
        if(where == 'afterbegin'){
        before = el.firstChild;
        }
        node = ieTable(4, trs, html, tre);
        }
        } else if(tag == 'tbody'){
        if(where == 'beforebegin'){
        before = el;
        el = el.parentNode;
        node = ieTable(2, ts, html, te);
        } else if(where == 'afterend'){
        before = el.nextSibling;
        el = el.parentNode;
        node = ieTable(2, ts, html, te);
        } else{
        if(where == 'afterbegin'){
        before = el.firstChild;
        }
        node = ieTable(3, tbs, html, tbe);
        }
        } else{ // TABLE
        if(where == 'beforebegin' || where == 'afterend'){ // OUTSIDE the table
        return;
        }
        if(where == 'afterbegin'){
        before = el.firstChild;
        }
        node = ieTable(2, ts, html, te);
        }
        el.insertBefore(node, before);
        return node;
        };


        return {

        useDom : false,

        markup : function(o){
        return createHtml(o);
        },
        applyStyles : function(el, styles){
        if(styles){
        if(typeof styles == "string"){
        @com.extjs.gxt.ui.client.core.El::fly(Lcom/google/gwt/dom/client/Element;)(el).@com.extjs.gxt.ui.client.core.El::applyStyles(Ljava/lang/String;)(styles);
        }else if (typeof styles == "object"){
        for (var style in styles){
        @com.extjs.gxt.ui.client.core.El::fly(Lcom/google/gwt/dom/client/Element;)(el).@com.extjs.gxt.ui.client.core.El::applyStyles(Ljava/lang/String;)(styles[tyle]);
        }
        }else if (typeof styles == "function"){
        Ext.DomHelper.applyStyles(el, styles.call());
        }
        }
        },


        insertHtml : function(where, el, html){
        where = where.toLowerCase();
        if(el.insertAdjacentHTML){
        if(tableRe.test(el.tagName)){
        var rs;
        if(rs = insertIntoTable(el.tagName.toLowerCase(), where, el, html)){
        return rs;
        }
        }
        switch(where){
        case "beforebegin":
        el.insertAdjacentHTML('BeforeBegin', html);
        return el.previousSibling;
        case "afterbegin":
        el.insertAdjacentHTML('AfterBegin', html);
        return el.firstChild;
        case "beforeend":
        el.insertAdjacentHTML('BeforeEnd', html);
        return el.lastChild;
        case "afterend":
        el.insertAdjacentHTML('AfterEnd', html);
        return el.nextSibling;
        }
        throw 'Illegal insertion point -> "' + where + '"';
        }
        var range = el.ownerDocument.createRange();
        var frag;
        switch(where){
        case "beforebegin":
        range.setStartBefore(el);
        frag = range.createContextualFragment(html);
        el.parentNode.insertBefore(frag, el);
        return el.previousSibling;
        case "afterbegin":
        if(el.firstChild){
        range.setStartBefore(el.firstChild);
        frag = range.createContextualFragment(html);
        el.insertBefore(frag, el.firstChild);
        return el.firstChild;
        }else{
        el.innerHTML = html;
        return el.firstChild;
        }
        case "beforeend":
        if(el.lastChild){
        range.setStartAfter(el.lastChild);
        frag = range.createContextualFragment(html);
        el.appendChild(frag);
        return el.lastChild;
        }else{
        el.innerHTML = html;
        return el.lastChild;
        }
        case "afterend":
        range.setStartAfter(el);
        frag = range.createContextualFragment(html);
        el.parentNode.insertBefore(frag, el.nextSibling);
        return el.nextSibling;
        }
        throw 'Illegal insertion point -> "' + where + '"';
        },


        insertBefore : function(el, o, returnElement){
        return this.doInsert(el, o, returnElement, "beforeBegin");
        },


        insertAfter : function(el, o, returnElement){
        return this.doInsert(el, o, returnElement, "afterEnd", "nextSibling");
        },


        insertFirst : function(el, o, returnElement){
        return this.doInsert(el, o, returnElement, "afterBegin", "firstChild");
        },

        // private
        doInsert : function(el, o, returnElement, pos, sibling){
        var newNode;
        if(this.useDom){
        newNode = createDom(o, null);
        (sibling === "firstChild" ? el : el.parentNode).insertBefore(newNode, sibling ? el[sibling] : el);
        }else{
        var html = createHtml(o);
        newNode = this.insertHtml(pos, el, html);
        }
        return newNode;
        },


        append : function(el, o, returnElement){

        var newNode;
        if(this.useDom){
        newNode = createDom(o, null);
        el.appendChild(newNode);
        }else{
        var html = createHtml(o);
        newNode = this.insertHtml("beforeEnd", el, html);
        }
        return newNode;
        },


        overwrite : function(el, o, returnElement){
        el.innerHTML = createHtml(o);
        return el.firstChild;
        },


        createTemplate : function(o){
        var html = createHtml(o);
        return new Ext.Template(html);
        }
        };
        }();
    }-*/;

    private native static void loadTemplate() /*-{
        var Ext = $wnd.GXT.Ext;
        Ext.Template = function(html){
        var a = arguments;
        if(Ext.isArray(html)){
        html = html.join("");
        }else if(a.length > 1){
        var buf = [];
        for(var i = 0, len = a.length; i < len; i++){
        if(typeof a[i] == 'object'){
        Ext.apply(this, a[i]);
        }else{
        buf[buf.length] = a[i];
        }
        }
        html = buf.join('');
        }

        this.html = html;
        if(this.compiled){
        this.compile();
        }
        };


        Ext.Template.prototype = {

        applyTemplate : function(values){
        if(this.compiled){
        return this.compiled(values);
        }
        var useF = this.disableFormats !== true;
        var fm = Ext.util.Format, tpl = this;
        var fn = function(m, name, format, args){
        if(format && useF){
        if(format.substr(0, 5) == "this."){
        return tpl.call(format.substr(5), values[name], values);
        }else{
        if(args){
        // quoted values are required for strings in compiled templates,
        // but for non compiled we need to strip them
        // quoted reversed for jsmin
        var re = /^\s*['"](.*)["']\s*$/;
        args = args.split(',');
        for(var i = 0, len = args.length; i < len; i++){
        args[i] = args[i].replace(re, "$1");
        }
        args = [values[name]].concat(args);
        }else{
        args = [values[name]];
        }
        return fm[format].apply(fm, args);
        }
        }else{
        return values[name] !== undefined ? values[name] : "";
        }
        };
        return this.html.replace(this.re, fn);
        },

        set : function(html, compile){
        this.html = html;
        this.compiled = null;
        if(compile){
        this.compile();
        }
        return this;
        },

        disableFormats : false,

        re : /\{([\w-]+)(?:\:([\w\.]*)(?:\((.*?)?\))?)?\}/g,


        compile : function(){
        var fm = Ext.util.Format;
        var useF = this.disableFormats !== true;
        var sep = @com.extjs.gxt.ui.client.GXT::isGecko ? "+" : ",";
        var fn = function(m, name, format, args){
        if(format && useF){
        args = args ? ',' + args : "";
        if(format.substr(0, 5) != "this."){
        format = "fm." + format + '(';
        }else{
        format = 'this.call("'+ format.substr(5) + '", ';
        args = ", values";
        }
        }else{
        args= ''; format = "(values['" + name + "'] == undefined ? '' : ";
        }
        return "'"+ sep + format + "values['" + name + "']" + args + ")"+sep+"'";
        };
        var body;
        // branched to use + in gecko and [].join() in others
        if(@com.extjs.gxt.ui.client.GXT::isGecko){
        body = "this.compiled = function(values){ return '" +
        this.html.replace(/\\/g, '\\\\').replace(/(\r\n|\n)/g, '\\n').replace(/'/g, "\\'").replace(this.re, fn) +
        "';};";
        }else{
        body = ["this.compiled = function(values){ return ['"];
        body.push(this.html.replace(/\\/g, '\\\\').replace(/(\r\n|\n)/g, '\\n').replace(/'/g, "\\'").replace(this.re, fn));
        body.push("'].join('');};");
        body = body.join('');
        }
        eval(body);
        return this;
        },

        call : function(fnName, value, allValues){
        return this[fnName](value, allValues);
        },

        insertFirst: function(el, values, returnElement){
        return this.doInsert('afterBegin', el, values, returnElement);
        },

        insertBefore: function(el, values, returnElement){
        return this.doInsert('beforeBegin', el, values, returnElement);
        },

        insertAfter : function(el, values, returnElement){
        return this.doInsert('afterEnd', el, values, returnElement);
        },

        append : function(el, values, returnElement){
        return this.doInsert('beforeEnd', el, values, returnElement);
        },

        doInsert : function(where, el, values, returnEl){
        el = Ext.getDom(el);
        var newNode = Ext.DomHelper.insertHtml(where, el, this.applyTemplate(values));
        return returnEl ? Ext.get(newNode, true) : newNode;
        },

        overwrite : function(el, values, returnElement){
        el = Ext.getDom(el);
        el.innerHTML = this.applyTemplate(values);
        return returnElement ? Ext.get(el.firstChild, true) : el.firstChild;
        }
        };

        // backwards compat
        Ext.DomHelper.Template = Ext.Template;
    }-*/;
    
    private static native void loadXTemplate() /*-{
        var Ext = $wnd.GXT.Ext;
        Ext.XTemplate = function(){
        Ext.XTemplate.superclass.constructor.apply(this, arguments);
        var s = this.html;

        s = ['<tpl>', s, '</tpl>'].join('');

        var re = /<tpl\b[^>]*>((?:(?=([^<]+))\2|<(?!tpl\b[^>]*>))*?)<\/tpl>/;

        var nameRe = /^<tpl\b[^>]*?for="(.*?)"/;
        var ifRe = /^<tpl\b[^>]*?if="(.*?)"/;
        var execRe = /^<tpl\b[^>]*?exec="(.*?)"/;
        var m, id = 0;
        var tpls = [];

        while(m = s.match(re)){
        var m2 = m[0].match(nameRe);
        var m3 = m[0].match(ifRe);
        var m4 = m[0].match(execRe);
        var exp = null, fn = null, exec = null;
        var name = m2 && m2[1] ? m2[1] : '';
        if(m3){
        exp = m3 && m3[1] ? m3[1] : null;
        if(exp){
        fn = new Function('values', 'parent', 'xindex', 'xcount', 'with(values){ return '+(Ext.util.Format.htmlDecode(exp))+'; }');
        }
        }
        if(m4){
        exp = m4 && m4[1] ? m4[1] : null;
        if(exp){
        exec = new Function('values', 'parent', 'xindex', 'xcount', 'with(values){ '+(Ext.util.Format.htmlDecode(exp))+'; }');
        }
        }
        if(name){
        switch(name){
        case '.': name = new Function('values', 'parent', 'with(values){ return values; }'); break;
        case '..': name = new Function('values', 'parent', 'with(values){ return parent; }'); break;
        default: name = new Function('values', 'parent', 'with(values){ return '+name+'; }');
        }
        }
        tpls.push({
        id: id,
        target: name,
        exec: exec,
        test: fn,
        body: m[1]||''
        });
        s = s.replace(m[0], '{xtpl'+ id + '}');
        ++id;
        }
        for(var i = tpls.length-1; i >= 0; --i){
        this.compileTpl(tpls[i]);
        }
        this.master = tpls[tpls.length-1];
        this.tpls = tpls;
        };

        Ext.extend(Ext.XTemplate, Ext.Template, {
        // private
        re : /\{([\w-\.\#]+)(?:\:([\w\.]*)(?:\((.*?)?\))?)?(\s?[\+\-\*\\]\s?[\d\.\+\-\*\\\(\)]+)?\}/g,
        // private
        codeRe : /\{\[((?:\\\]|.|\n)*?)\]\}/g,

        // private
        applySubTemplate : function(id, values, parent, xindex, xcount){
        var t = this.tpls[id];
        if(t.test && !t.test.call(this, values, parent, xindex, xcount)){
        return '';
        }
        if(t.exec && t.exec.call(this, values, parent, xindex, xcount)){
        return '';
        }
        var vs = t.target ? t.target.call(this, values, parent) : values;
        parent = t.target ? values : parent;
        if(t.target && Ext.isArray(vs)){
        var buf = [];
        for(var i = 0, len = vs.length; i < len; i++){
        buf[buf.length] = t.compiled.call(this, vs[i], parent, i+1, len);
        }
        return buf.join('');
        }
        return t.compiled.call(this, vs, parent, xindex, xcount);
        },

        compileTpl : function(tpl){
        var fm = Ext.util.Format;
        var useF = this.disableFormats !== true;
        var sep = @com.extjs.gxt.ui.client.GXT::isGecko ? "+" : ",";
        var fn = function(m, name, format, args, math){
        if(name.substr(0, 4) == 'xtpl'){
        return "'"+ sep +'this.applySubTemplate('+name.substr(4)+', values, parent, xindex, xcount)'+sep+"'";
        }
        var v;
        if(name === '.'){
        v = 'values';
        }else if(name === '#'){
        v = 'xindex';
        }else if(name.indexOf('.') != -1){
        v = name;
        }else{
        v = "values['" + name + "']";
        }
        if(math){
        v = '(' + v + math + ')';
        }
        if(format && useF){
        args = args ? ',' + args : "";
        if(format.substr(0, 5) != "this."){
        format = "fm." + format + '(';
        }else{
        format = 'this.call("'+ format.substr(5) + '", ';
        args = ", values";
        }
        }else{
        args= ''; format = "("+v+" === undefined ? '' : ";
        }
        return "'"+ sep + format + v + args + ")"+sep+"'";
        };
        var codeFn = function(m, code){
        return "'"+ sep +'('+code+')'+sep+"'";
        };

        var tempBody = tpl.body;
        var tempTpl = tpl;
        var body;
        // branched to use + in gecko and [].join() in others
        if(@com.extjs.gxt.ui.client.GXT::isGecko){
        body = "var fm = $wnd.GXT.Ext.util.Format;var temp = function(values, parent, xindex, xcount){ return '" +
        tempBody.replace(/(\r\n|\n)/g, '\\n').replace(/'/g, "\\'").replace(this.re, fn).replace(this.codeRe, codeFn) +
        "';};";
        }else{
        body = ["var fm = $wnd.GXT.Ext.util.Format;var temp = function(values, parent, xindex, xcount){ return ['"];
        body.push(tempBody.replace(/(\r\n|\n)/g, '\\n').replace(/'/g, "\\'").replace(this.re, fn).replace(this.codeRe, codeFn));
        body.push("'].join('');};");
        body = body.join('');
        }
        eval(body);
        tempTpl.compiled = temp;
        return this;
        },

        applyTemplate : function(values){
        return this.master.compiled.call(this, values, {}, 1, 1);
        },

        compile : function(){return this;}

        });

        Ext.XTemplate.prototype.apply = Ext.XTemplate.prototype.applyTemplate;
    }-*/;
}
