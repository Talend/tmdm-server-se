/********************************************************************
 * 
 * GLOBAL PROTOTYPES and FUNCTIONS
 * 
 ********************************************************************/
Ext.ux.TabCloseMenu = function(){
    var tabs, menu, ctxItem;
    this.init = function(tp){
        tabs = tp;
        tabs.on('contextmenu', onContextMenu);
    }

    function onContextMenu(ts, item, e){
        if(!menu){ // create context menu on first right click
            menu = new Ext.menu.Menu([{
                id: tabs.id + '-close',
                text: 'Close',
                handler : function(){
                    tabs.remove(ctxItem);
                }
            },{
                id: tabs.id + '-close-others',
                text: 'Close Others',
                handler : function(){
                    tabs.items.each(function(item){
                        if(item.closable && item != ctxItem){
                            tabs.remove(item);
                        }
                    });
                }
            },{
                id: tabs.id + '-close-all',
                text: 'Close All',
                handler : function(){
                    tabs.items.each(function(item){
                        if(item.closable){
                            tabs.remove(item);
                        }
                    });
                }
            },{
                id: tabs.id + '-close-cancel',
                text: 'Cancel'
            }]);
        }
        ctxItem = item;
        var items = menu.items;
        items.get(tabs.id + '-close').setDisabled(!item.closable);
        var disableOthers = true;
        tabs.items.each(function(){
            if(this != item && this.closable){
                disableOthers = false;
                return false;
            }
        });
        items.get(tabs.id + '-close-others').setDisabled(disableOthers);
        menu.showAt(e.getPoint());
    }
}; 

String.prototype.ellipse = function(maxLength){
    if(this.length > maxLength){
        return this.substr(0, maxLength-5) + '...';
    }
    return this;
}
String.prototype.replaceAll=function(s1, s2) { 
	return this.replace(new RegExp(s1,"g"), s2); 
}

if(![].indexOf){
    Array.prototype.indexOf = function(obj, start){
        for(var i=(start||0); i<this.length; i++){
            if(this[i]==obj){
                return i;
            }
        }
    }
}

String.prototype.trim = function () {
	return this.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");
};

String.prototype.endWith=function(str){
    if(str==null||str==""||this.length==0||str.length>this.length)
      return false;
    if(this.substring(this.length-str.length)==str)
      return true;
    else
      return false;
    return true;
};

String.prototype.startWith=function(str){
    if(str==null||str==""||this.length==0||str.length>this.length)
      return false;
    if(this.substr(0,str.length)==str)
      return true;
    else
      return false;
    return true;
};


var g_oHtmlEncodeElement;

function jsStringEscape(text){
	if (!!text){
		return text.replaceAll("\"","\\\"").replaceAll("\'","\\\'");
	}
	return "";
}

function htmlEscape(text, isAttrValue)
{
    g_oHtmlEncodeElement = g_oHtmlEncodeElement || document.createElement("div");
    g_oHtmlEncodeElement.innerText = g_oHtmlEncodeElement.textContent = text;
    if (isAttrValue){
    	return g_oHtmlEncodeElement.innerHTML.replaceAll("\"","&quot;").replaceAll("\'","&#039;");
    }
    return g_oHtmlEncodeElement.innerHTML;
}

function htmlUnescape(html)
{
    g_oHtmlEncodeElement = g_oHtmlEncodeElement || document.createElement("div");
    g_oHtmlEncodeElement.textContent = g_oHtmlEncodeElement.innerText = "";
    g_oHtmlEncodeElement.innerHTML = html;
    return g_oHtmlEncodeElement.innerText || g_oHtmlEncodeElement.textContent;
}

function isArray(value) {
      return value &&
      typeof value === 'object' &&
      typeof value.length === 'number' &&
      typeof value.splice === 'function' &&
      !(value.propertyIsEnumerable('length'));
}

Ext.namespace("Ext.ux");
Ext.ux.comboBoxRenderer = function(combo) {
  return function(value) {
    var idx = combo.store.find(combo.valueField, value);
    var rec = combo.store.getAt(idx);
	return (rec == null ? '' : rec.get(combo.displayField) );    
  };
}

Ext.ux.LocaleMap = function(M) {
	   this.map = M || {};
	};
Ext.extend(Ext.ux.LocaleMap, Ext.util.Observable, {
	   get : function(key) {
	      //var value = this.map[key] || (key + ' not found!');
	      var value = this.map[key] || ''; 
	      if(arguments.length > 1 && value.toString().indexOf('{') >= 0) {
	         value = new Ext.Template(value).apply(Array.prototype.slice.call(arguments, 1));
	      }
	      return value;
	   }
});

Ext.BLANK_IMAGE_URL = 'secure/ext-2.2/resources/images/default/s.gif';

var LOGOUT = {
	'fr':'D&eacute;connexion',
	'en':'Logout'
}

var LOADING={
	'fr':'Chargement...',
	'en':'Loading...'
}

var CLOSECONFIRM={
	'fr':'Si vous fermez l\'onglet maintenant, vos modifications seront perdues. Êtes-vous sûr de vouloir continuer ?',
	'en':'If you close the tab now, your changes will be lost. Are you sure you want to proceed?'
}

/********************************************************************
* Hot Loads a Resource
*********************************************************************/
function loadScript(scriptID, src, verifiedObjectName, callbackFirstTime, callbackFollowingTimes) {
	//log("loadScript loading "+src);		
	//first check if it already loaded
	if ( isDefined('window',verifiedObjectName)) {
		//log("Script "+scriptID+" already loaded");
		if (callbackFollowingTimes != undefined) callbackFollowingTimes.call();
		return;
	}
	//Begin by creating a new Loader instance:
	var loader = new YAHOO.util.YUILoader();
    //log("Trying to fetch "+getServerPath()+"/"+src);
	//Add the module to YUILoader
    loader.addModule({
        name: scriptID, //module name; must be unique
        type: "js", //can be "js" or "css"
        fullpath: "../../../../"+src, //can use a fullpath instead
        varName: verifiedObjectName //replaces the verifier function in 2.4.0
        //verifier: checkScript //deprecated for 2.4.0  - the verifier function we just defined
		//requires: ['yahoo', 'event'] //if this module had dependencies, we could define here
    });
	//include the new script
    loader.require(scriptID); 
    //new for 2.4.0 - specify call back function
    loader.onSuccess = callbackFirstTime;
	//Insert Script on the page, passing in our callback:
    loader.insert(); //do not pass callback in 2.4.0 - use onSuccess
};


/**
 * Synchronously Load a Javascript Or Css 
 */
function loadResource(pathFromRootContext,verifiedObjectName) {
	log('Load resource '+pathFromRootContext);
	var type ='html';
	if(pathFromRootContext.substr(pathFromRootContext.length-3,3) == 'css' ) type = 'css';
	if(pathFromRootContext.substr(pathFromRootContext.length-2,2) == 'js' ) type = 'js';
	var completed = false; 

	function loaderCallback() {
		completed = true;
	}
	
	function load() {
		var timestamp = new Date().getTime();
		if ( (type=='js') && isDefined('window',verifiedObjectName)) return;
		//Begin by creating a new Loader instance:
		var loader = new YAHOO.util.YUILoader();
		//Add the module to YUILoader
	    loader.addModule({
	        name: pathFromRootContext + "?timestamp=" + timestamp, //module name; must be unique
	        type: type,
	        fullpath: "../../../../"+pathFromRootContext + "?timestamp=" + timestamp, //can use a fullpath instead
	        varName: verifiedObjectName //replaces the verifier function in 2.4.0 - ignored if css
	    });
		//include the new script
	    loader.require(pathFromRootContext + "?timestamp=" + timestamp); 
	    //new for 2.4.0 - specify call back function
	    loader.onSuccess = loaderCallback;
		//Insert Script on the page, passing in our callback:
	    loader.insert(); //do not pass callback in 2.4.0 - use onSuccess
	}
	
	function wait() {
		if (! completed) setTimeout(wait, '200');
	} 
	
	load();
	wait();
};

//Synchronously Load a Javascript Or Css with callback

function loadResource(pathFromRootContext,verifiedObjectName,loaderCallback) {
	//log('Load resource '+pathFromRootContext);
	var type ='html';
	if(pathFromRootContext.substr(pathFromRootContext.length-3,3) == 'css' ) type = 'css';
	if(pathFromRootContext.substr(pathFromRootContext.length-2,2) == 'js' ) type = 'js';
	
	function loaderFailure() {
		Ext.Msg.alert("ERROR",  "There was an error loading resource from server side! ")
	}
	
	function load() {
		if ( (type=='js') && isDefined('window',verifiedObjectName)) return;
		//Begin by creating a new Loader instance:
		var loader = new YAHOO.util.YUILoader();
		//Add the module to YUILoader
	    loader.addModule({
	        name: pathFromRootContext, //module name; must be unique
	        type: type,
	        fullpath: "../../../../"+pathFromRootContext, //can use a fullpath instead
	        varName: verifiedObjectName //replaces the verifier function in 2.4.0 - ignored if css
	    });
		//include the new script
	    loader.require(pathFromRootContext); 
	    //new for 2.4.0 - specify call back function
	    loader.onSuccess = loaderCallback;
	    loader.onFailure = loaderFailure;
		//Insert Script on the page, passing in our callback:
	    loader.insert(); //do not pass callback in 2.4.0 - use onSuccess
	}
	
	load();
};


/********************************************************************
* Utilities / defined / Local
*********************************************************************/				


 function getContextPath() {
	return document.getElementById('contextPath').value;
};

function getServerPath() {
	return document.getElementById('serverPath').value;
};

		
function isDefined(objectName, variables){
	d=variables.split(".");
	ev = objectName;
    for (j= 0; j<d.length; j=j+1) {
    	ev+="['"+d[j]+"']";
    }
	return (typeof eval(ev) != 'undefined');
};

function initLocaleMap(language,locales){
	  var localesMap = new Ext.ux.LocaleMap(locales);
	  var localeMap = new Ext.ux.LocaleMap(localesMap.get(language));
	  return localeMap;
	}
		
				
/********************************************************************
* Logging Stuff
*********************************************************************/				

var debugWindow = null;
				
function log(msg) {
	log(msg,"debug");
};

function log(msg,level) {
	if (isDefined('window','console')) {
		console.log(msg,level,'');
		return;
	}
	//for less capable browsers....
   /* if ((debugWindow == null) || (debugWindow.closed)) {
      debugWindow = window.open("","debugconsole","scrollbars=yes,resizable=yes,height=100,width=300");
      debugWindow.document.open("text/html", "replace");
    }
    debugWindow.document.writeln('<br/>'+msg);
    debugWindow.scrollTo(0,10000);
    debugWindow.focus();
    // debugWindow.document.close();  // uncomment this if you want to see only last message , not all the previous messages
     */
};


/********************************************************************
 * amalto Namespace
 ********************************************************************/


/* Taken From YAHOO */
var amalto = function(){
	return {
		namespace: function() {
		    var a=arguments, o=null, i, j, d;
		    for (i=0; i<a.length; i=i+1) {
		        d=a[i].split(".");
		        o=amalto;
		
		        // amalto is implied, so it is ignored if it is included
		        for (j=(d[0] == "amalto") ? 1 : 0; j<d.length; j=j+1) {
		            o[d[j]]=o[d[j]] || {};
		            o=o[d[j]];
		        }
		    }
		    return o;
		}
	}
}();

/**
 * Amalto Namespaces
 */
amalto.namespace("amalto");


/**
  * The namespace of a module is amalto.b2box.modulename where modulename is the Application Name stripped of spaces 
 */
 
//amalto.namespace("amalto");


/**
 * See the YAHOO Module pattern @  http://yuiblog.com/blog/2007/06/12/module-pattern/
 */
 
amalto.core = function () {

	/********************************************************************
	 * 
	 * PRIVATE Properties and Methods
	 * 
	 ********************************************************************/
	

	/************
	* Layout
	*************/
	var mainViewport;
	
	function initUI() {
		language = DWRUtil.getValue('languageSelect');
		// initialize state manager, we will use cookies
		Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
		
		// Init the Quick Tips.  Any tag-based quick tips will start working.
		Ext.QuickTips.init();

		// Apply a set of config properties to the singleton
		Ext.apply(Ext.QuickTips.getQuickTip(), {
		    maxWidth: 200,
		    minWidth: 100,
		    showDelay: 50,
		    trackMouse: false
		});
		
		
		mainViewport = new Ext.Viewport({
			id: 'mainViewport',
			layout:'border',
		    items: [
			    {
			        title: '',
			        region: 'north',
			        contentEl: 'header', 
			        xtype: 'panel',
			        autoHeight: true,
			        titlebar: false,
			        collapsible: false,
			        animCollapse: false
			    },
			    {
			    	id:'status',
			        title: '',
			        xtype: 'panel',
			        contentEl: 'statusdiv',
			        region: 'south',
			        height: 22,
			        titlebar: false,
			        collapsible: false,
			        animCollapse: false    
			    },
			    {
			        title: 'Menus',
			        xtype: 'panel',
			        region: 'west',
			        contentEl: 'menus', 
			       	width: 150,
			       	maxWidth: 230,
			       	autoScroll:true,
			        titlebar: true,
			        collapsible: true,
			        animCollapse: true,
			        split: true //not documented: renders the panel resizeable
			    },
			    {
			        title: 'Actions',
			        xtype: 'panel',
			        region: 'east',
			        id: 'actions', 
			       	width: 150,
			       	maxWidth: 230,
			       	bodyStyle:'padding:5px',
			        titlebar: true,
			        collapsible: true,
			        animCollapse: true,   
			        split: true //not documented: renders the panel resizeable
			    },				
			    {
					id : 'centerTabPanel',
						region : 'center',
						xtype : 'tabpanel',
						contentEl : 'centerdiv',
						border : true,
						bodyborder : true,
						layoutOnTabChange : true,
						resizeTabs : false,
						enableTabScroll:true,
						defaults: {autoScroll:true},
						plugins : new Ext.ux.TabCloseMenu(),
						listeners : {

								'beforeremove' : function(c, e) {
								if (e.isdirty == true) {
									//return Ext.MessageBox.confirm(
									return confirm(
													//'confirm',
													 CLOSECONFIRM[language])
								}
								else
								return true;
							},'beforetabchange': function(tabPanel, newTab, currentTab){
                               if(currentTab!=undefined&&currentTab!=null){
                                 var currentTabId=currentTab.id;
                                 if(currentTabId.startWith('itemDetailsdiv')){
                                   //FIXME: suppose to use a hidden textfield, however I can't focus on a hidden tf 
                                   var defaultComp=Ext.getCmp('processCombo'+currentTabId.substring(14));
                                   if(defaultComp!=undefined&&defaultComp!=null)defaultComp.focus();
                                 }
                               }
                               return true;
                            }
						},
						defaults : {
							hideMode : 'offsets'
						}
					}
		    ]
		});
		
		mainViewport.show();
		
	
		amalto.core.working('Loading menus');
	   //load menus
		loadMenus();
		loadWelcome();
		amalto.core.ready();		
		//load actions
		amalto.actions.loadActions();
	
	    //update status panel back
//		statusPanel.getEl().addClass('done');
//		statusPanel.setContent('');
		
		
		//add username info
		LayoutInterface.getUsernameAndUniverse(function(result){
			var nameAndUniverse = new Array()
			nameAndUniverse = result.toLocaleString().split(",");
			var name = nameAndUniverse[0];
			var universe = "HEAD";
			if(""!=nameAndUniverse[1]&&nameAndUniverse[1]!=null)
				universe = nameAndUniverse[1];
			
			/*var html = "Hello"+", "+name+", "+"you are connected to the Universe ["+universe+"]";
			if(language=='fr') html = "Bonjour"+","+" "+name+","+" "+"vous êtes connecté à l'Univers ["+universe+"]";*/
			var html = name+ "<br/>";
			if(universe!='UNKNOWN'){
				if(language=='fr') {
					html += "connecté à:["+universe+"]";
				}
				else {
					html += "connected to: ["+universe+"]";
				}
			}
			
			DWRUtil.setValue('username-div',html);
		});
		
		
		var btn = new Ext.Button({
			applyTo:'logout-btn',
			handler: function(){
			    LayoutInterface.logout(function() {
			    });
				var contextPath = DWRUtil.getValue('contextPath');
				window.location.href=contextPath+"/secure?action=logout";
			}, 
			text: LOGOUT[language]
		});
		btn.render();
		mainViewport.doLayout(); 
	}
	
	
	/*
	var working = function(){
		statusPanel.getEl().addClass('done');
		setTimeout("statusPanel.setContent('')",3000);
	}
	*/
	
	var saveConfig = function(){
		var cluster = DWRUtil.getValue('datacluster-select');
		var model = DWRUtil.getValue('datamodel-select');
		LayoutInterface.setClusterAndModel(cluster,model,function(result){
			alert(result);
		});
	}
	
	
	
	
	
	
	/********************
	 * Actions/Menus handling
	 ********************/
	var MENUS;
	var GWT_MENUS = {};

	/*
	 * Load Actions/Menus
	 */	
	function loadMenus() {
		var menus = new Array();
		LayoutInterface.getMenus(
			language,
			function(result){
				var menus = eval("(" +result+")");
				
				var pmenus = Ext.get('menus');
			    // delegate clicks on the lists
			    pmenus.mon('click', menuClicked);
			    
			    //create menus template
			    var tplClosed = new Ext.Template(
			    	'<a id="menu-{id}" href="{url}"><span id="menu-{id}-span" class="body">{name}<br><span class="desc">{desc}</span></span></a>'+
			    	'<div id="menu-{id}-div" class="sub-body" style="display: none;"/>'
			    );
			   	var tplNone = new Ext.Template(
			    	'<a id="menu-{id}" href="{url}"><span class="body">{name}<br><span class="desc">{desc}</span></span></a>'
			    );
			    
			    var previousLevel = 1;
			    var doms = new Array();
			    doms[0] = pmenus.dom;
	  
			    // go through the menus and add them to the list
			    for(var index=0; index<menus.length; index++) {
			    	var menu = menus[index];
			    	if (menu.application=='') { 		    		//a menu with submenus
			    		tplClosed.append(doms[menu.level-1], menu);
			    		//update doms
			    		var divId = 'menu-'+menu.id+'-div';
			    		doms[menu.level] =Ext.get(divId).dom;
			    		//make IE display the image centered by loading the image now (rather than on initialization)
			    		$("menu-"+menu.id+"-span").style.backgroundImage="url(/core/secure/img/tree-closed.gif)";
			    		$("menu-"+menu.id+"-span").style.backgroundRepeat="no-repeat";
			    		$("menu-"+menu.id+"-span").style.backgroundPositionX="0px";
			    		$("menu-"+menu.id+"-span").style.backgroundPositionY="9px";
			    	} else {
			    	
			    	    if(menu.level>1){
			    	    menu.name="&nbsp;&nbsp;"+menu.name;
			    	    }
			    		
			    		//tplNone.append(doms[menu.level-1], menu);
			    		var menuitem = document.createElement("a");
			    		var menuid=menu.id //must
			    		var showmenuurl=(menu.url==null||menu.url==undefined)?"":menu.url;
			    		var showmenuname=(menu.name==null||menu.name==undefined)?"":menu.name;
			    		var showmenudesc=(menu.desc==null||menu.desc==undefined)?"":menu.desc;
			    		menuitem.setAttribute("href",showmenuurl);
                        menuitem.setAttribute("id","menu-"+menuid);
                        
                        //alert(menu.context+"."+menu.application);
                        //switch image
                        var imageIconPart='';
                        var toCheckMenuID=menu.context+"."+menu.application;
                        if(menu.icon!=null){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/imageserver/"+menu.icon+"?width=16&height=16\"/></span>";
			    		}else if(toCheckMenuID=='itemsbrowser.ItemsBrowser'||toCheckMenuID=='viewbrowser.ViewBrowser'||toCheckMenuID=='itemsbrowser2.ItemsBrowser2'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/browse.png\"/></span>";
                        }else if(toCheckMenuID=='crossreferencing.CrossReferencing'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/crossref.png\"/></span>";
                        }else if(toCheckMenuID=='hierarchical.GroupingHierarchy'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/grouping_hier.png\"/></span>";
                        }else if(toCheckMenuID=='ehierarchical.DerivedHierarchy'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/derived_hier.png\"/></span>";
                        }else if(toCheckMenuID=='usersandroles.Users'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/manage_users.png\"/></span>";
                        }else if(toCheckMenuID=='reporting.Reporting'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/reporting.png\"/></span>";
                        }else if(toCheckMenuID=='SynchronizationItem.SynchronizationItem'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/synchro_item.png\"/></span>";
                        }else if(toCheckMenuID=='SynchronizationAction.SynchronizationAction'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/synchronize.png\"/></span>";
                        }else if(toCheckMenuID=='ItemsTrash.ItemsTrash'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/trash.png\"/></span>";
                        }else if(toCheckMenuID=='journal.Journal'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/updatereport.png\"/></span>";
                        }else if(toCheckMenuID=='workflowtasks.WorkflowTasks'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/workflowtasks.png\"/></span>";
                        }else if(toCheckMenuID=='datastewardship.Datastewardship'){
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/stewardship.png\"/></span>";
                        }else{
                        	//default menus icon
                        	imageIconPart="<span style=\"padding-right:8px;\"><IMG SRC=\"/talendmdm/secure/img/menu/default.gif\"/></span>";
                        }	
                        
                        menuitem.innerHTML="<span class=\"body\">"+imageIconPart+showmenuname+"<br><span class=\"desc\">"+showmenudesc+"</span></span>";                        
                        doms[menu.level-1].appendChild(menuitem);

			    		//var menuId = 'menu-'+menu.id;
			    		//doms[menu.level] =Ext.get(menuId).dom;
			    		doms[menu.level] =menuitem;
			    		if(toCheckMenuID=='itemsbrowser2.ItemsBrowser2'){
			    			menu.disabled = true;
			    			YAHOO.util.Dom.addClass(menuitem, 'x-item-disabled');
			    			GWT_MENUS[toCheckMenuID] = {"menu": menu, "menuitem": menuitem};
			    		}
			    		
			    	}
			    }
				 MENUS = menus;			
			}
		);
	}//loadMenus

	/*
	 * load welcome page.
	 */
	function loadWelcome() {
		amalto.core.loadMainScript("welcome", "Welcome",
				function() {
					var initFunction = "amalto.welcome.Welcome.init()";
					setTimeout(initFunction,'50');
				}
		);
	}
	
	/*
	 * The Action itself
	 */
	var menuClicked = function(e) {
		//stop defauklt bhaviour
		e.preventDefault();
		// find the "a" element that was clicked
		var a = e.getTarget('a',false);
		if(a){
			
			//update display of menu in menu Panel
			YAHOO.util.Dom.removeClass(Ext.get('menus').dom.getElementsByTagName('a'), 'selected');
			YAHOO.util.Dom.addClass(a.id, 'selected');  //.substr(5)
			
			//run the menu associated
			
			var menuId = a.id.substr(5);
			var menu = MENUS[menuId-1];
			if (menu.disabled){
				return;
			}
			
			if (menu.application == '') {
				//A menu Holder
				if ($("menu-"+menu.id+"-div").style.display=="none") {
					$("menu-"+menu.id+"-div").style.display="block";
					$("menu-"+menu.id+"-span").style.background="url(/core/secure/img/tree-opened.gif) no-repeat 0px 9px;";
				} else {
					$("menu-"+menu.id+"-div").style.display="none";
					$("menu-"+menu.id+"-span").style.background="url(/core/secure/img/tree-closed.gif) no-repeat 0px 9px;";				
				}
			}else {
				DWREngine.setAsync(false);
				LayoutInterface.isShowMsg({
					callback:function(show) {
						if(show) {
							Ext.MessageBox.show({
						           msg: 'Parsing informations of license, please wait...',
						           progressText: 'Loading',
						           width:300,
						           wait:false,
						           waitConfig: {interval:200}
						        });
						}
					},
					errorHandler:function(errorString, exception) { 
						Ext.MessageBox.hide();
						alert('Error:'+ errorString);
					}
				});
				
				LayoutInterface.isExpired({
					callback:function(isExpired){
					Ext.MessageBox.hide();
					if(!isExpired) {
						//load the script if necessary
						amalto.core.loadMainScript(
								menu.context,
								menu.application,
								function() {
									var initFunction = "amalto."+menu.context+"."+menu.application.replace(/\s/g,'')+".init()";
									setTimeout(initFunction,'50');
								}
						);
					}
				},
				errorHandler:function(errorString, exception) { 
					Ext.MessageBox.hide();
					alert('Error:'+ errorString);
				}  
				});
				
				DWREngine.setAsync(true);
			}//else   
		}//findTarget
	}
	



	/********************************************************************
	 * 
	 * PUBLIC Properties and Methods
	 * 
	 ********************************************************************/
	return  { 
		ROOT_CONTEXT: "core",
		language: "en",
		LOG_LEVEL: "DEBUG", //DEBUG,  INFO,  NONE
		layout: "",
		debugWindow: null,

		
		/*************************
	 	* Status Display
	 	*************************/
		
		working: function(message){
			Ext.get('status').removeClass('ready');
			Ext.get('status').addClass('working');
			Ext.get('status').update(message);
		},
		ready: function(message){
			if ((message==null) || (message=='')) message='';
			Ext.get('status').removeClass('working');
			Ext.get('status').addClass('ready');
			Ext.get('status').update(message);
			setTimeout(function() {Ext.get('status').update('');},4000);
		},
			
			
		loadMainScript: function(context, application, callback) {
			var app = application.replace(/\s/g,'');
			var scriptID = context+"."+app;
			var src = '/'+context+'/secure/js/'+app+'.js';
			//register the intermediate context namespace to avoid issues
			amalto.namespace("amalto."+context);
			loadScript(scriptID,src,"amalto."+context+"."+app, callback, callback);
		},

		loadDWRScript: function(context, interfaceName, callback) {
			var app = interfaceName;
			var scriptID = "dwr."+context+"."+app;
			var src = '/'+context+'/secure/dwr/interface/'+app+'Interface.js';
			loadScript(scriptID,src,app+"Interface",callback, callback);
		},
		
		/*************************
	 	* Initialization
	 	*************************/
		
		logout:  function() {
			alert("test");
			LayoutInterface.logout(function(){
				document.location.reload;
			});
			
		},
		
		
		/**
		 * Returns the center Tab Panel component
	 	*/
		getTabPanel: function() {
			return mainViewport.getComponent('centerTabPanel');
		},
		
		/**
		 * Forces the wiewport to re layout
	 	*/
		doLayout: function() {
			mainViewport.doLayout();
		},
		
		
		init: function () {
			initUI();
		},//init()
		
		switchLanguage: function() {
			language = DWRUtil.getValue('languageSelect');
			var contextPath =  DWRUtil.getValue('contextPath');
			window.location.href=contextPath+"/secure?language="+language;
		},
		enabledGwtMenuItem: function(toCheckMenuID){
			try {
				if (GWT_MENUS != null){
					var menuObj = GWT_MENUS[toCheckMenuID];
					if (menuObj != undefined){
						menuObj.menu.disabled = false;
						YAHOO.util.Dom.removeClass(menuObj.menuitem, 'x-item-disabled');
						return true;
					}
				}
				return false;
			} catch (e) {
				return false;	
			}
		}
		
	}//PUBLIC
	
}();

 
YAHOO.util.Event.on(window, 'load', amalto.core.init);



