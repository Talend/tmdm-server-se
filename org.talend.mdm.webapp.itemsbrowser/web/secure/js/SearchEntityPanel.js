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

Ext.extend(amalto.itemsbrowser.SearchEntityPanel, Ext.Panel, {
    initPageSize : 20,
    criteria : "",
	initUIComponents : function() {
	    
	var  LABEL_NO_RESULT =    {
	    'fr' : 'Pas de résultat',
	    'en' : 'No result'
	};
	
	var LABEL_DISPLAYING = {
		'fr':'Enregistrements affichés : ',
		'en':'Displaying records: '
	};
	
	var LABEL_OF = {
		'fr':'sur',
		'en':'of'
	};
	
	var  LABEL_LINES_PER_PAGE =    {
	    'fr' : 'Nombre de lignes par page',
	    'en' : 'Number of lines per page'
	};
	
	var BROWSE_RECORDS = {
	    'fr' : 'Accès aux données->',
	    'en' : 'BrowseRecords->'
	};
	
	var LABEL_SEARCH_DATA = {
		    'fr' : 'Recherche de données',
		    'en' : 'Search Data'
	};
	
	var LABEL_SEARCH_PANEL = {
		    'fr' : 'Panneau de recherche',
		    'en' : 'Search Panel'
	};
	
	var LABEL_ENTITY = {
		    'fr' : 'Entité',
		    'en' : 'Entity'
	};
	
	var LABEL_FROM = {
		    'fr' : 'De',
		    'en' : 'From'
	};
	
	var LABEL_TO = {
		    'fr' : 'A',
		    'en' : 'To'
	};
	
	var LABEL_KEYWORDS = {
		    'fr' : 'Mots clés',
		    'en' : 'Keywords'
	};
	
	var LABEL_RESET = {
		    'fr' : 'Réinitialiser',
		    'en' : 'Reset'
	};
	
	var LABEL_SEARCH = {
		    'fr' : 'Rechercher',
		    'en' : 'Search'
	};
	
	var LABEL_EXPORT = {
		    'fr' : 'Exporter',
		    'en' : 'Export'
	};
	
	var LABEL_DATE = {
		    'fr' : 'Date',
		    'en' : 'Date'
	};
	
	var LABEL_KEY = {
		    'fr' : 'Clé',
		    'en' : 'Key'
	};
	
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
				header : LABEL_DATE[language],
				dataIndex : "date",
				sortable : true
			},
			{
				hidden : false,
				header : LABEL_ENTITY[language],
				dataIndex : "entity",
				sortable : true
			},
			{
				hidden : false,
				header :  LABEL_KEY[language],
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
				amalto.itemsbrowser.ItemsBrowser.editItemDetails(BROWSE_RECORDS[language], ids, entity,
					function() {});
				amalto.core.doLayout();
				
				
			}
   	    },
		bbar : new Ext.PagingToolbar({
			id:"searchEntityPagingToolbar",
			displayMsg: LABEL_DISPLAYING[language]+' {0} - {1} '+LABEL_OF[language]+' {2}',
			displayInfo: true,
			store : this.store1,
			xtype : "paging",
			emptyMsg :LABEL_NO_RESULT[language],
			pageSize : this.initPageSize,
			items:[ 
	        	new Ext.Toolbar.Separator(),
	        	new Ext.Toolbar.TextItem(LABEL_LINES_PER_PAGE[language] + " :"),
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
		title : LABEL_SEARCH_DATA[language],
		items : [this.gridPanel1, {
			frame : false,
			height : 170,
			layout : "fit",
			split : false,
			title : LABEL_SEARCH_PANEL[language],
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
						fieldLabel : LABEL_ENTITY[language],
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
						fieldLabel : LABEL_FROM[language],
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
						fieldLabel :LABEL_KEYWORDS[language],
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
						fieldLabel : LABEL_KEY[language],
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
						fieldLabel : LABEL_TO[language],
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
				text : LABEL_RESET[language]
			},{
				handler : function(button, event) {
					this.onSearchBtnClick(button, event);
				}.createDelegate(this),
				text : LABEL_SEARCH[language]
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
				text : LABEL_EXPORT[language]
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
		window.location.href="/itemsbrowser/secure/ExportingServlet?cluster=" + cluster + "&params=" + myParams;	
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
    }
}
); 
