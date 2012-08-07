/*
 * @include  "/com.amalto.webapp.core/web/secure/ext.ux/DWRProxy.js"
 * @include  "/com.amalto.webapp.core/web/secure/js/core.js"
 * @include  "/talend.webapp.v3.updatereport/web/secure/js/UpdateReportPanel.js"
 */
 
amalto.namespace("amalto.updatereport");

amalto.updatereport.UpdateReport = function () {	
	
    loadResource("/updatereport/secure/js/UpdateReportPanel.js", "");
    
    loadResource("/updatereport/secure/js/DataLogViewer.js", "");

    loadResource("/updatereport/secure/js/XmlTreeLoader.js", "");

    loadResource("/updatereport/secure/js/ClassTableLayout.js", "");

    loadResource("/updatereport/secure/js/DocumentHistoryPanel.js", "");

    loadResource("/updatereport/secure/js/HistoryViewer.js", "");

    loadResource("/updatereport/secure/css/UpdateReport.css", "" );

    var updateReportPanel;
    
    function initUI(){

    	var isFirstTime=false;
		var tabPanel = amalto.core.getTabPanel();
		updateReportPanel = tabPanel.getItem('UpdateReportPanel');

		if(updateReportPanel == undefined){
			isFirstTime=true;
			updateReportPanel=new amalto.updatereport.UpdateReportPanel();			
			tabPanel.add(updateReportPanel);
		}
        
        updateReportPanel.show();
		updateReportPanel.doLayout();
		amalto.core.doLayout();
		
		return isFirstTime;
		
    };
	
	function browseUpdateReport(){
		amalto.updatereport.bundle = new Ext.i18n.Bundle({bundle:'UpdateReport', path:'/updatereport/secure/resources', lang:language});
		amalto.updatereport.bundle.onReady(function(){
			// init UI
			initUI();
			// init data
			updateReportPanel.initListData();
		});
	};
	
	function browseUpdateReportWithSearchCriteria(dataObject, ids, itemsBroswer){
	    amalto.updatereport.bundle = new Ext.i18n.Bundle({bundle:'UpdateReport', path:'/updatereport/secure/resources', lang:language});
		amalto.updatereport.bundle.onReady(function(){
			var isFirstTime=initUI();
			// set search criteria
			updateReportPanel.setSearchCriteria(dataObject,ids,"","","","");
			// load data
			if(isFirstTime){
				updateReportPanel.initListData(itemsBroswer);
			}else{
				updateReportPanel.doSearchList(itemsBroswer);
			}
		});
	}


 	return {
 		
		init: function() {browseUpdateReport();},
		browseUpdateReportWithSearchCriteria: function(dataObject,ids,itemsBrower) {browseUpdateReportWithSearchCriteria(dataObject,ids,itemsBrower);}
 	}
}();
