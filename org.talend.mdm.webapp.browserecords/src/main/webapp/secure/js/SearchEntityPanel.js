amalto.namespace("amalto.itemsbrowser");

amalto.itemsbrowser.SearchEntityPanel = function(config) {	
	Ext.applyIf(this, config);	
	var lineageEntities = config.lineageEntities;
	var ids = config.ids;
	var dataObject = config.dataObject;
	var language = config.language;
	this.initUIComponents();
	amalto.itemsbrowser.SearchEntityPanel.superclass.constructor.call(this);
	var entityCB = Ext.getCmp("entityCB");
	entityCB.setValue(this.lineageEntities[0]);
};

var mate = document.getElementById("gwt:property");
language = mate.content.split("=")[1];
if (language == 'zh') {
    language = 'zh_CN';
}

amalto.itemsbrowser.SearchEntity = {};
amalto.itemsbrowser.SearchEntity.bundle =  new Ext.i18n.Bundle({bundle:'SearchEntity', path:'secure/resources', lang:language});
amalto.itemsbrowser.SearchEntity.bundle.onReady(function(){
	var searchEntityBundle = amalto.itemsbrowser.SearchEntity.bundle;
	Ext.extend(amalto.itemsbrowser.SearchEntityPanel, Ext.Panel, {
	    initPageSize : 20,
	    criteria : "",
		initUIComponents : function() {
	
	    Ext.apply(Ext.form.VTypes, {  
	         dateRange: function(val, field){  
	             if(field.dateRange){  
	                 var beginId = field.dateRange.begin;  
	                 this.beginField = Ext.getCmp(beginId);  
	                 var endId = field.dateRange.end;  
	                 this.endField = Ext.getCmp(endId);  
	                 var beginDate = this.beginField.getValue();  
	                 var toDate = this.endField.getValue();  
	             } 
	             
	             if(beginDate!=""&&toDate!=""){
	            	 if(beginDate <= toDate){  
		                 return true;  
		             }else{  
		                 return false;  
		             }
	             }
	             
	             return true;
	         },    
	         dateRangeText: 'Start Data can not be greater than End Date '  
	    });  

	    Ext.override(Ext.menu.DateMenu, {
	    	autoWidth : function(){
	    		var el = this.el;
	    		el.setWidth(this.width);
	    		if(check(/msie/)){
	    			el.setWidth(180);
	    			var t = el.dom.offsetWidth;
	    			el.setWidth(this.ul.getWidth() + el.getFrameWidth("lr"));
	    		}
	    	}
	    });
	    
		this.recordType = Ext.data.Record.create([
	        {name: "date", mapping : "date", type: "string"},
	        {name: "entity", mapping : "entity", type: "string"},
	        {name: "key", mapping : "key"}
		 ]);
			 
		this.store1 = new Ext.data.Store({
			 proxy: new Ext.data.DWRProxy(ItemsBrowserInterface.getItems, true),
	         reader: new Ext.data.ListRangeReader( 
				{id:'keys', totalProperty:'totalSize',root: 'data'}, this.recordType),
	         remoteSort: false
		});
			
		this.store1.on('beforeload', 
	        function(button, event) {
				this.onBeforeloadStore();
			}.createDelegate(this)
	    );
		
		this.store1.on('load', 
	            function(button, event) {
					Ext.getCmp("searchEntityPagingToolbar").loading.setIconClass("x-tbar-done");
				}
		);

		this.gridPanel1 = new Ext.grid.GridPanel({
			id:"searchEntityGridPanel",
			store : this.store1,
			border: false,
			loadMask:true, 
			layout : "fit",
			region : "center",
			selModel : new Ext.grid.RowSelectionModel({}),
			columns : [
				{
					hidden : false,
					header : searchEntityBundle.getMsg("LABEL_DATE"),
					dataIndex : "date",
					sortable : true
				},
				{
					hidden : false,
					header : searchEntityBundle.getMsg("LABEL_ENTITY"),
					dataIndex : "entity",
					sortable : true
				},
				{
					hidden : false,
					header :  searchEntityBundle.getMsg("LABEL_KEY"),
					dataIndex :"key",
					sortable : true
				}
			],
			listeners:
	   	    {
				'rowdblclick' : function(grid,rowIndex, e ){
					var record = grid.getStore().getAt(rowIndex);
					var ids = record.data.key;
					var entity = record.data.entity;
					//@yguo, should be open the record
					amalto.itemsbrowser.ItemsBrowser.editItemDetails(searchEntityBundle.getMsg("BROWSE_RECORDS"), ids, entity,
						function() {});
					amalto.core.doLayout();
					
					
				}
	   	    },
			bbar : new Ext.PagingToolbar({
				id:"searchEntityPagingToolbar",
				displayMsg: searchEntityBundle.getMsg("LABEL_DISPLAYING")+' {0} - {1} '+searchEntityBundle.getMsg("LABEL_OF")+' {2}',
				displayInfo: true,
				store : this.store1,
				xtype : "paging",
				emptyMsg : searchEntityBundle.getMsg("LABEL_NO_RESULT"),
				pageSize : this.initPageSize,
				items:[ 
		        	new Ext.Toolbar.Separator(),
		        	new Ext.Toolbar.TextItem(searchEntityBundle.getMsg("LABEL_LINES_PER_PAGE") + " :"),
		        	new Ext.form.TextField({
						id:'updateRLineMaxItems',
						value:this.initPageSize,
						width:30,
						listeners: {
		                	'specialkey': function(a, e) {
					            if(e.getKey() == e.ENTER) {
			                		var lineMax = DWRUtil.getValue('updateRLineMaxItems');
									if(lineMax==null || lineMax=="")lineMax=20;
									Ext.getCmp("searchEntityPagingToolbar").pageSize=parseInt(lineMax);
									Ext.getCmp("searchEntityGridPanel").store.reload({params:{start:0, limit:lineMax}});
					            } 
							},
							'change':function(field,newValue,oldValue){
	                            
	                            if(newValue != oldValue){
	                                lineMax = newValue;
	                                if(lineMax==null || lineMax=="") 
	                                    lineMax=20;
	                                Ext.getCmp("searchEntityPagingToolbar").pageSize=parseInt(lineMax);
	                                Ext.getCmp("searchEntityGridPanel").store.reload({params:{start:0, limit:lineMax}});
	                            }
	                        
	                        }
		                }
		            })
		        ]
			})
		});
			
		Ext.apply(this, {			
			layout : "border",
			title : searchEntityBundle.getMsg("LABEL_SEARCH_DATA"),
			items : [this.gridPanel1, {
				frame : false,
				height : 170,
				layout : "fit",
				split : false,
				header : false,
				collapsible : true,
				border: false,
				items : [{
					height : 30,
					layout : "column",
					items : [{
						columnWidth : ".5",
						layout : "form",
						items : [{
							id : 'entityCB',
							name : "entity",
							fieldLabel : searchEntityBundle.getMsg("LABEL_ENTITY"),
							store: this.lineageEntities,
							xtype : "combo",
							allowBlank : false,
							editable: false,
							triggerAction : 'all',
							listeners : {
	                           'specialkey' : function(field, event) {
	                           	                  this.onSearchKeyClick(field, event);
	                                          }.createDelegate(this)
	                                    }
						}, 
						{
							id : "fromDate",
							name : "fromDate",
							fieldLabel : searchEntityBundle.getMsg("LABEL_FROM"),
							xtype : "datefield",
							format : "Y-m-d H:i:s",
							width: 150,
							readOnly : false,
							listeners : {
	                           'specialkey' : function(field, event) {
	                           	                  this.onSearchKeyClick(field, event);
	                                          }.createDelegate(this)
	                                    },
	                       vtype: 'dateRange',
	                       dateRange: {begin: 'fromDate', end: 'toDate'}
						},
						{
							name : "keyWords",
							//emptyText : "Select a source...",
							fieldLabel :searchEntityBundle.getMsg("LABEL_KEYWORDS"),
							xtype : "textfield",
							displayField:'text',
					        valueField:'value',   
					        typeAhead: true,
					        triggerAction: 'all',
					        mode: 'local',
					        listeners : {
	                           'specialkey' : function(field, event) {
	                           	                  this.onSearchKeyClick(field, event);
	                                          }.createDelegate(this)
	                                    }
						}],
						border : false
					}, {
						columnWidth : ".5",
						layout : "form",
						items : [{
							name : "key",
							fieldLabel : searchEntityBundle.getMsg("LABEL_KEY"),
							xtype : "textfield",
							listeners : {
	                           'specialkey' : function(field, event) {
	                           	                  this.onSearchKeyClick(field, event);
	                                          }.createDelegate(this)
	                                    }
						}, 
						{
							id : "toDate",
							name : "toDate",
							fieldLabel : searchEntityBundle.getMsg("LABEL_TO"),
							xtype : "datefield",
							format : "Y-m-d H:i:s",
							width: 150,
							readOnly : false,
							listeners : {
	                           'specialkey' : function(field, event) {
	                           	                  this.onSearchKeyClick(field, event);
	                                          }.createDelegate(this)
	                                    },
	                        vtype: 'dateRange',
	                        dateRange: {begin: 'fromDate', end: 'toDate'}
						}],
						border : false
					}],
					border : false
				}],
				region : "north",
				bodyStyle:'padding:5px',
				buttons : [{
					handler : function(button, event) {
						this.onResetBtnClick(button, event);
					}.createDelegate(this),
					text : searchEntityBundle.getMsg("LABEL_RESET")
				},{
					handler : function(button, event) {
						this.onSearchBtnClick(button, event);
					}.createDelegate(this),
					text : searchEntityBundle.getMsg("LABEL_SEARCH")
				},{	
					handler: function() {					
						var curcriteria = this.getRequestParam();
						var fkvalue = this.ids;
			   	    	
			   			if(fkvalue != "") {
			   				curcriteria += ",fkvalue:'[" + fkvalue +"]'";
			   			}
			   			
			   			var dataObject = this.dataObject;
			   			
			   			if(dataObject != "") {
			   				curcriteria += ",dataObject:'" + dataObject +"'";
			   			}
			   			
			   			if(curcriteria != ""){
			   				curcriteria = curcriteria.substring(1)
			   				curcriteria = "{" + curcriteria + "}";
			   			}
			   			
						this.exporting(curcriteria);
					}.createDelegate(this),
					text : searchEntityBundle.getMsg("LABEL_EXPORT")
				}]
				}],
				id : "searchEntityPanel",
				closable:true,
				border:false
			});	
		},
		
		exporting:function(myParams){
		//FIXME: It seem don't define datacluster-select in this project
		    var cluster;
			if (document.getElementById('datacluster-select') != null){
				cluster = DWRUtil.getValue('datacluster-select');
			}else{
				cluster = '';
			}
			window.location.href="browserecords/ExportingServlet?cluster=" + cluster + "&params=" + encodeURIComponent(myParams);	
		},
	    
		initListData : function(itemsBroswer){
			this.isItemsBrowser = itemsBroswer;
			this.store1.load({params:{start:0, limit:this.initPageSize}});
	    },
	    
	    doSearchList : function(){
			var pageSize=Ext.getCmp("searchEntityPagingToolbar").pageSize;
			this.store1.reload({params:{start:0, limit:pageSize}});
	    },
	    
	    onSearchBtnClick : function(button, event){
			this.doSearchList();
	    },
	    
	    onSearchKeyClick : function(field, event){
	    	if (event.getKey() == Ext.EventObject.ENTER) {
		      this.doSearchList();
		    }
			
	    },
	    
	    setSearchCriteria : function(conceptValue,keyValue,keyWordsValue,startDateValue,endDateValue){
			if(conceptValue != '')DWRUtil.setValue('entity',conceptValue);
			if(keyValue != '')DWRUtil.setValue('key',keyValue);
			if(keyWordsValue != '')DWRUtil.setValue('keyWords',keyWordsValue);
			if(startDateValue != '')DWRUtil.setValue('fromDate',startDateValue);
			if(endDateValue != '')DWRUtil.setValue('toDate',endDateValue);
	    },
	    
	    onResetBtnClick : function(button, event){
			DWRUtil.setValue('key','');
			DWRUtil.setValue('keyWords','');
	        DWRUtil.setValue('fromDate','');
	        DWRUtil.setValue('toDate','');
	        this.criteria = "";
	    },
	    
	    getRequestParam : function(){
	    	var requestParam="";

	    	var entity = DWRUtil.getValue('entity');
			if(entity != "")requestParam += ",entity:'" + entity + "'";
			var key = DWRUtil.getValue('key');
			if(key != "")requestParam += ",key:'" + key + "'";
			var keyWords = DWRUtil.getValue('keyWords');
			if(keyWords != "")requestParam += ",keyWords:'" + keyWords + "'";
			var fromDate = DWRUtil.getValue('fromDate');
			if(fromDate != "") requestParam += ",fromDate:'" + fromDate + "'";
			var toDate = DWRUtil.getValue('toDate');
			if(toDate != "") requestParam += ",toDate:'" + toDate + "'";
			if(this.isItemsBrowser == true) requestParam += ",itemsBrowser:'" + this.isItemsBrowser +"'";
			
			return requestParam;
	    }.createDelegate(this),
	    
	    onBeforeloadStore : function(){    	    	
	   	 	this.criteria = this.getRequestParam();
	   	 	//@temp yguo, get the key  
	    	var fkvalue = this.ids;
	    	
			if(fkvalue != "") {
				this.criteria += ",fkvalue:'[" + fkvalue +"]'";
			}

			var dataObject = this.dataObject;
			
			if(dataObject != "") {
				this.criteria += ",dataObject:'" + dataObject +"'";
			}
			
			if(this.criteria != ""){
				this.criteria = this.criteria.substring(1)
				this.criteria = "{" + this.criteria + "}";
			}
			
	        Ext.apply(this.store1.baseParams,{
	          regex: this.criteria
	        });
	    },
	    
	    afterRender : function() {
	    	amalto.itemsbrowser.SearchEntityPanel.superclass.afterRender.call(this);  
			var ua = navigator.userAgent.toLowerCase();
			check = function(r){ 
				return r.test(ua); 
			};
			
			if(check(/chrome/) || check(/safari/)){
			    var datePickerCSS = ".x-date-picker {width: 175px;}";
			    Ext.util.CSS.createStyleSheet(datePickerCSS,'datePickerStyle');
			};
		}
	}
	); 
});
