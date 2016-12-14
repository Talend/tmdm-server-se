amalto.namespace("amalto.itemsbrowser");
amalto.itemsbrowser.NavigatorPanel = function() {
	var NAVIGATOR_NODE_IN_ENTITY_TYPE = 1;
	var NAVIGATOR_NODE_OUT_ENTITY_TYPE = 2;
	var NAVIGATOR_NODE_VALUE_TYPE = 3;
	var NAVIGATOR_MENU_DETAIL_COLOR = "rgb(214, 39, 40)";
	var NAVIGATOR_MENU_IN_COLOR = "rgb(44, 160, 44)";
	var NAVIGATOR_MENU_OUT_COLOR = "rgb(31, 119, 180)";
	var NAVIGATOR_MENU_SETTINGS_COLOR = "rgb(255, 127, 14)";
	var NAVIGATOR_MENU_DISABLE_COLOR = "#A9A9A9";
	var NAVIGATOR_NODE_IMAGE_INBOUND = "secure/img/navigator_relation_in.png";
	var NAVIGATOR_NODE_IMAGE_OUTBOUND = "secure/img/navigator_relation_out.png";
	var NAVIGATOR_NODE_IMAGE_DATA = "secure/img/navigator_data.png";
	var NAVIGATOR_NODE_IMAGE_SELECTED_DATA = "secure/img/navigator_selected_data.png";
	var NAVIGATOR_NODE_IMAGE_COLLAPSED_DATA = "secure/img/navigator_collapsed_data.png";
	var filterValue = '';
	var nodeLabelLength=20;
	var image_diameter = 16;
	var image_offset = image_diameter / 2;
	var image_padding = image_diameter / 4;
	var class_index;
	var class_array;
	
	var svg;
	var rect;
	var message;
	
	// input parameter
	var restServiceUrl;
	var id;
	var concept
	var cluster;
	var language;
	
	// Menu
	var dataset;
	var pie;
	var piedata;
	var outerRadius = 48;
	var innerRadius = 25;
	var arc;
	
	// Type Cluster
	var typeCluster;
	var diagonal;
	var typeLinks;
	var typeNodes;
	var typeLink;
	var typeNode;
	var type_text_dx = 40;
	var type_text_dy = -10;
	
	// Data Force
	var width = 800;
	var height = 800;
	var force;
	var nodeList = [];
	var linkList = [];
	var container;
	var links;
	var nodes;
	var link;
	var node;
	var link_text;
	var node_text;
	var node_concept_text;
	var drag;
	var selectNode;
	var selectTypeNode;
	var showNode;
	
	function paint() {
		node = node.data(nodes);
		link = link.data(links);
		
		link.style("stroke-width", function(link){
			if (link.navigator_line_display) {
				return 1;
			} else {
				return 0;
			}
		});
		
		link.enter()
		.append("line")
		.style("stroke-width", 1)
		.style("stroke", "#ccc")
		.attr("marker-start",function(link, i) {
			if (link.target.navigator_node_relation == NAVIGATOR_NODE_IN_ENTITY_TYPE && isSupportArrow()) {
				return "url(#arrow_in)";
			} else {
				return "";
			}
		}).attr("marker-end",function(link, i) {
			if (link.target.navigator_node_relation == NAVIGATOR_NODE_OUT_ENTITY_TYPE && isSupportArrow()) {
				return "url(#arrow_out)";
			} else {
				return "";
			}
		});
		
		node.attr("width", function(d) {
			if (d.navigator_node_display) {
				return image_diameter;
			} else {
				return 0;
			};
		}).attr("height", function(d) {
			if (d.navigator_node_display) {
				return image_diameter;
			} else {
				return 0;
			};
		});
		
		node.enter()
		.append("image")
		.attr("class", function(d) {
			return "image_" + getClassIndex(d);
		}).attr("width", image_diameter)
		.attr("height",image_diameter)
		.attr("identifier", function(d) {
			return getIdentifier(d);
		}).attr("xlink:href", function(d) {
			return getImage(d);
		}).each(function(d, i) {
			d3.select(this).call(drag);
			d3.select(this).on("click", showMenu);
			d3.select(this).on("dblclick", dblclick);
			d3.select(this).on("mouseover", mouseover);
			d3.select(this).on("mouseout", mouseout);
		});
		
		link_text = link_text.data(links);					
		link_text.text(function(link) {
			if (link.navigator_line_display) {
				return link.navigator_line_label;
			} else {
				return "";
			}
		});
		
		link_text.enter()
		.append("text")
		.style("font-weight","bold")
		.style("fill-opacity", "0.0")
		.text(function(link) {
			return link.navigator_line_label;
		});
		
		node_text = node_text.data(nodes);					
		node_text.text(function(d) {
			if (d.navigator_node_display) {
				return d.navigator_node_short_label;
			} else {
				return "";
			}
		});
		
		node_text.enter()
		.append("text")
		.style("fill", "black")
		.attr("dx", image_offset)
		.attr("dy", -5)
		.text(function(d) {
			return d.navigator_node_short_label;
		});
		
		
		node_concept_text = node_concept_text.data(nodes);
		node_concept_text.text(function(d) {
			if (d.navigator_node_display) {
				return d.navigator_node_concept;
			} else {
				return "";
			}
		});
		node_concept_text.enter()
		.append("text")
		.style("font-weight","bold")
		.style("fill-opacity", "0.0")
		.attr("dx", function(d){
			return -(d.navigator_node_concept.length * 3.5);
		}).attr("dy", image_diameter)
		.text(function(d) {
			return d.navigator_node_concept_label;
		});
		force.start();
	}
	
	function initDataNode(data) {
		nodes = eval('(' + data
				+ ')');
		nodes[0].navigator_node_short_label = generateNodeLabel(nodes[0].navigator_node_label);
		nodes[0].children = new Array();
		nodes[0].navigator_node_expand = true;
		nodes[0].navigator_node_display = true;
		links = [];
		force = d3.layout.force().nodes(nodes)
				.links(links).size(
						[ width, height ])
				.linkDistance(120).charge([ -400 ]);
		drag = force.drag().origin(function(d) {
			d.fixed = true;
			hiddenTypeCluster();
			return d;
		}).on("dragstart", dragstarted)
        .on("drag", dragged)
        .on("dragend", dragended);

		links = force.links();
		nodes = force.nodes();
		link = container.append("g").attr("id",
				"navigator_data_link_group")
				.selectAll(".link");
		node = container.append("g").attr("id",
				"navigator_data_node_group")
				.selectAll("image");
		link_text = container.append("g").attr(
				"id", "navigator_text_link_group")
				.selectAll(".linetext");
		node_text = container.append("g").attr(
				"id", "navigator_text_node_group")
				.selectAll(".nodetext");
		node_concept_text = container.append("g").attr(
				"id", "navigator_concept_text_node_group")
				.selectAll(".nodetext");
		force.on("tick", tick);
		paint();
	}
	
	function paintDataNode(newNodes,navigator_node_relation) {
		for ( var i = 0; i < newNodes.length; i++) {
			var node = newNodes[i];
			var newNode = {
				x : selectNode.x
						+ getRandomInt(-15,
								15),
				y : selectNode.y
						+ getRandomInt(-15,
								15),
				navigator_node_ids : node.navigator_node_ids,
				navigator_node_concept : selectTypeNode.navigator_node_concept,
				navigator_node_foreignkey_path : node.navigator_node_foreignkey_path,
				navigator_node_field_foreignkey_path : node.navigator_node_field_foreignkey_path,
				navigator_node_concept_label : node.navigator_node_concept_label,
				navigator_node_type : node.navigator_node_type,
				navigator_node_relation : navigator_node_relation,
				navigator_node_label : node.navigator_node_label,
				navigator_node_short_label : generateNodeLabel(node.navigator_node_label),
				navigator_node_expand : false,
				navigator_node_display : true
			};
			selectNode.nodeChildren.push(newNode);
			newNode.parentNode = selectNode;
			nodes.push(newNode);
			var newLink = {
				source : selectNode,
				target : newNode,
				navigator_node_type : node.navigator_node_type,
				navigator_line_label : selectTypeNode.navigator_line_label,
				navigator_node_concept : node.navigator_node_concept,
				navigator_node_foreignkey_path : node.navigator_node_foreignkey_path,
				navigator_node_field_foreignkey_path : node.navigator_node_field_foreignkey_path,
				navigator_line_display : true
			};
			selectNode.linkChildren.push(newLink);
			links.push(newLink);
			if (navigator_node_relation == NAVIGATOR_NODE_OUT_ENTITY_TYPE) {
				selectNode.page[selectTypeNode.navigator_node_foreignkey_path].ids
				.shift();
			}
		}
		paint();
	}

	function paintTypeCluster(root) {
		hiddenTypeCluster();
		typeCluster.size([ root.children.length < 5 ? 240 : root.children.length * image_diameter * 1.5, 150  ])
		var elementId;
		var text_x;
		var text_anchor;
		var marker_direction;
		var marker_position;
		if (root.navigator_node_type === NAVIGATOR_NODE_IN_ENTITY_TYPE) {
			text_x = -image_offset - image_padding;
			text_anchor = "end";
			elementId = "cluster_type_in_group";
			marker_direction = "url(#arrow_in)";
			marker_position = "marker-start";
		} else if (root.navigator_node_type === NAVIGATOR_NODE_OUT_ENTITY_TYPE) {
			text_x = image_offset + image_padding;
			text_anchor = "start";
			elementId = "cluster_type_out_group";
			marker_direction = "url(#arrow_out)";
			marker_position = "marker-end";
		}
		if (svg.select("#" + elementId)[0][0] === null) {
			var typeGroup = container.append("g").attr("id",
					elementId)
			var typeNodes = typeCluster.nodes(root);
			var typeLinks = typeCluster.links(typeNodes);
			var xOffset = selectNode.x - root.y;
			var yOffset = selectNode.y - root.x;

			typeNode = typeGroup.selectAll("image").data(typeNodes);
			typeNode.enter()
			.append("g")
			.style('font', '12px sans-serif')
			.attr('transform',function(node) {
				node.x = node.x + yOffset;
				node.y = node.y + xOffset;
				if (root.navigator_node_type === NAVIGATOR_NODE_IN_ENTITY_TYPE) {
					if (node.name !== 'root') {
						node.y = selectNode.x + (selectNode.x - node.y);
					}
				}
				return 'translate(' + node.y + ',' + node.x + ')';
			});

			typeNode.each(function(d, i) {
				if (d.navigator_node_concept !== 'root') {
					d3.select(this).append("image")
					.attr("width",image_diameter)
					.attr("height",image_diameter)
					.attr("x", "-8px")
					.attr("y", "-8px")
					.attr("identifier",function(d) {
						return getIdentifier(d);
					}).attr("xlink:href",function(d) {
						return getImage(d);
					}).on("click", click)
					.on("mouseover", type_mouseover)
					.on("mouseout", type_mouseout);				
				}
			});

			for ( var i = 0; i < typeLinks.length; i++) {
				var d = typeLinks[i];
				if (node.name !== 'root') {
					if (root.navigator_node_type === NAVIGATOR_NODE_IN_ENTITY_TYPE) {
						d.source.y = d.source.y - image_offset
								- image_padding;
						d.target.y = d.target.y + image_offset
								+ image_padding;
					} else if (root.navigator_node_type === NAVIGATOR_NODE_OUT_ENTITY_TYPE) {
						d.source.y = d.source.y + image_padding;
						d.target.y = d.target.y - image_offset
								- image_padding;
					}
				}
			}
			typeLink = typeGroup.selectAll(".link").data(typeLinks);
			typeLink.enter().append("path")
			.style("fill", "none")
			.style("stroke-width", 1)
			.style("stroke","#ccc")
			.attr(marker_position,marker_direction)
			.attr("d", diagonal);

			typeNode.append("text")
			.attr("dx", function(node) {
				return text_x;
			}).attr("dy", 4)
			.style("text-anchor", function(d) {
				return text_anchor;
			}).text(function(d) {
				if (d.navigator_node_concept !== 'root') {
					return d.navigator_node_label;
				}
			});

			typeLink.enter().append("text")
			.style("font-weight","bold")
			.style("fill-opacity", "0.0")
			.text(function(d) {
				return d.target.navigator_line_label;
			}).attr("x", function(d) {
				return (d.source.y + d.target.y) / 2;
			}).attr("y", function(d) {
				return (d.source.x + d.target.x) / 2;
			});
		} else {
			svg.select("#" + elementId).remove();
		}
	}

	function menuClick(arc) {
		if ('detail' === arc.data.name) {
			hiddenTypeCluster();
			if (NAVIGATOR_NODE_VALUE_TYPE == selectNode.navigator_node_type) {
				amalto.navigator.Navigator.openRecord(
						selectNode.navigator_node_ids,
						selectNode.navigator_node_concept);
				if (showNode !== undefined) {
					svg.selectAll(".image_" + getClassIndex(showNode))
					.attr("xlink:href",function(node) {
						if (node.expanded != undefined && !node.expanded) {
							return NAVIGATOR_NODE_IMAGE_COLLAPSED_DATA;
						} else {
							return NAVIGATOR_NODE_IMAGE_DATA;
						}
					});
				}
				svg.selectAll(".image_" + getClassIndex(selectNode))
				.attr("xlink:href",function(node) {
					if (node.expanded != undefined && !node.expanded) {
						return NAVIGATOR_NODE_IMAGE_COLLAPSED_DATA;
					} else {
						return NAVIGATOR_NODE_IMAGE_SELECTED_DATA;
					}
				});
				showNode = selectNode;
			}
		} else if ('in' === arc.data.name) {
			if (selectNode.expanded == undefined || selectNode.expanded) {
				Ext.Ajax.request({
					url : restServiceUrl
						+ '/data/'
						+ cluster
						+ '/inBoundTypes/'
						+ selectNode.navigator_node_concept
						+ '/'
						+ encodeURIComponent(selectNode.navigator_node_ids),
					method : 'GET',
					params : {
						parentType : selectNode.parentNode != null ? selectNode.parentNode.navigator_node_concept : "",
						parentIds : selectNode.parentNode != null ? selectNode.parentNode.navigator_node_ids : "",
						language : language
					},
					success : function(response, options) {
						var newNodes = eval('('
						+ response.responseText
						+ ')');
						var root = {
							"navigator_node_concept" : "root",
							"navigator_node_type" : NAVIGATOR_NODE_IN_ENTITY_TYPE,
							"children" : newNodes
						};
						paintTypeCluster(root);
					},
					failure : function(response, options) {
						handleFailure(response);
					}
				});
			} else {
				svg.select("#menu_group").remove();
			}
		} else if ('out' === arc.data.name) {
			if (selectNode.expanded == undefined || selectNode.expanded) {
				Ext.Ajax
						.request({
							url : restServiceUrl
									+ '/data/'
									+ cluster
									+ '/outBoundTypes/'
									+ selectNode.navigator_node_concept
									+ '/'
									+ encodeURIComponent(selectNode.navigator_node_ids),
							method : 'GET',
							params : {
								parentType : selectNode.parentNode != null ? selectNode.parentNode.navigator_node_concept : "",
								parentIds : selectNode.parentNode != null ? selectNode.parentNode.navigator_node_ids : "",
								language : language
							},
							success : function(response, options) {
								var newNodes = eval('('
										+ response.responseText
										+ ')');
								var root = {
									"navigator_node_concept" : "root",
									"navigator_node_type" : NAVIGATOR_NODE_OUT_ENTITY_TYPE,
									"children" : newNodes
								};
								paintTypeCluster(root);
							},
							failure : function(response, options) {
								handleFailure(response);
							}
						});
			} else {
				svg.select("#menu_group").remove();
			}
		} else if ('settings' === arc.data.name) {
			hiddenTypeCluster();
			if (NAVIGATOR_NODE_VALUE_TYPE == selectNode.navigator_node_type) {
				amalto.navigator.Navigator.showSettingWindow();
			}
		}
	}

	function click(d, i) {
		hiddenTypeCluster();
		selectTypeNode = d;
		if (NAVIGATOR_NODE_IN_ENTITY_TYPE == d.navigator_node_type) {
			if (selectNode.page === undefined) {
				selectNode.page = new Object();
				selectNode.nodeChildren = new Array();
				selectNode.linkChildren = new Array();
			}
			if (selectNode.page[d.navigator_node_foreignkey_path] === undefined) {
				var pageObject = new Object();
				pageObject.start = 0
				selectNode.page[d.navigator_node_foreignkey_path] = pageObject;
			}
			if ((selectNode.page[d.navigator_node_foreignkey_path].start == 0 || selectNode.page[d.navigator_node_foreignkey_path].start < (selectNode.page[d.navigator_node_foreignkey_path].total + amalto.navigator.Navigator.getPageSize()))) {
				Ext.Ajax
						.request({
							url : restServiceUrl + '/data/'
									+ cluster + '/inBoundRecords/'
									+ d.navigator_node_concept,
							method : 'GET',
							params : {
								foreignKeyConcept : selectNode.navigator_node_concept,
								foreignKeyPath : d.navigator_node_foreignkey_path,
								foreignKeyValue : encodeURIComponent(d.navigator_node_foreignkey_value),
								filterValue : filterValue,
								parentType : selectNode.parentNode != null ? selectNode.parentNode.navigator_node_concept : "",
								parentIds : selectNode.parentNode != null ? selectNode.parentNode.navigator_node_ids : "",
								start : selectNode.page[d.navigator_node_foreignkey_path].start,
								limit : amalto.navigator.Navigator.getPageSize(),
								language : language
							},
							success : function(response, options) {
								var resultObject = eval('('
										+ response.responseText
										+ ')');
								resultObject.result[0].navigator_node_field_foreignkey_path = d.navigator_node_foreignkey_path;
								selectNode.expanded = true;
								if (selectNode.page[d.navigator_node_foreignkey_path].start == 0) {
									selectNode.page[d.navigator_node_foreignkey_path].total = resultObject.totalCount;
								}
								selectNode.page[d.navigator_node_foreignkey_path].start = selectNode.page[d.navigator_node_foreignkey_path].start
								+ amalto.navigator.Navigator.getPageSize();
								if (d.navigator_node_hasPrimaryKeyInfo) {
									amalto.navigator.Navigator.handleNodeLabel(Ext.encode(resultObject.result),NAVIGATOR_NODE_IN_ENTITY_TYPE);
								} else {
									paintDataNode(resultObject.result,NAVIGATOR_NODE_IN_ENTITY_TYPE);
								}
							},
							failure : function(response, options) {
								handleFailure(response);
							}
						});
			}
		} else if (NAVIGATOR_NODE_OUT_ENTITY_TYPE == d.navigator_node_type) {
			if (selectNode.page === undefined) {
				selectNode.page = new Object();
				selectNode.nodeChildren = new Array();
				selectNode.linkChildren = new Array();
			}
			if (selectNode.page[d.navigator_node_foreignkey_path] === undefined) {
				var pageObject = new Object();
				pageObject.pageNumber = 1
				pageObject.ids = d.navigator_node_ids;
				selectNode.page[d.navigator_node_foreignkey_path] = pageObject;
			}
			var idArray = [];
			for (i = 0; (i < amalto.navigator.Navigator.getPageSize() && i < selectNode.page[d.navigator_node_foreignkey_path].ids.length); i++) {
				idArray[i] = selectNode.page[d.navigator_node_foreignkey_path].ids[i];
			}
			if (idArray.length > 0) {
				Ext.Ajax
						.request({
							url : restServiceUrl + '/data/'
									+ cluster + '/records/'
									+ d.navigator_node_concept,
							method : 'GET',
							params : {
								ids : idArray,
								language : language
							},
							success : function(response, options) {
								selectNode.expanded = true;
								if (d.navigator_node_hasPrimaryKeyInfo) {
									amalto.navigator.Navigator.handleNodeLabel(response.responseText,NAVIGATOR_NODE_OUT_ENTITY_TYPE);
								} else {
									var responseValue = eval('('
											+ response.responseText
											+ ')');
									responseValue[0].navigator_node_foreignkey_path = d.navigator_node_foreignkey_path;
									paintDataNode(responseValue,NAVIGATOR_NODE_OUT_ENTITY_TYPE);
								}
							},
							failure : function(response, options) {
								handleFailure(response);
							}
						});
			}
		}
	}

	var dblclick = function(d, i) {
//		d.fixed = false;
		 if (d.nodeChildren) {
			 d.expanded = false
			 collapseNode(d);
				svg
				.selectAll(
						".image_"
								+ getClassIndex(d))
				.attr("xlink:href",function(node) {
					return getImage(node);
				});
		 } else {
			 d.expanded = true
			 svg
				.selectAll(
						".image_"
								+ getClassIndex(d))
				.attr("xlink:href",function(node) {
					return getImage(node);
				});
			 expandNode(d);
		}
		paint();
		svg.select("#menu_group").remove();
	}
	
	var expandNode = function(d) {
		 if (d._nodeChildren != undefined && d._linkChildren != undefined && d.expanded == true) {
			d.nodeChildren = d._nodeChildren;
			d.linkChildren = d._linkChildren;
			 for (var i=0;i<d.nodeChildren.length;i++) {
				 svg.selectAll(".image_"+ getClassIndex(d.nodeChildren[i]))
				.attr("xlink:href",function(node) {
					return getImage(node);
				});
				nodes[d.nodeChildren[i].index].navigator_node_display = true;
			 	expandNode(d.nodeChildren[i]);
			 }
			for (var j=0;j<d.linkChildren.length;j++) {
				links[d.linkChildren[j].target.index - 1].navigator_line_display = true;
			}
		    d._nodeChildren = null;
		 }
	}
	
	var collapseNode = function(d) {
		 if (d.nodeChildren != undefined) {
			 d._nodeChildren = d.nodeChildren;
			 d._linkChildren = d.linkChildren;
			 for (var i=0;i<d.nodeChildren.length;i++) {
				 nodes[d.nodeChildren[i].index].navigator_node_display = false;
				 collapseNode(d.nodeChildren[i]);
			 }
			 for (var j=0;j<d.linkChildren.length;j++) {
				 links[d.linkChildren[j].target.index - 1].navigator_line_display = false;
			 }
			 d.nodeChildren = null;
			 d.linkChildren = null;
		 }
	}

	var getRandomInt = function(min, max) {
		return Math.floor(Math.random() * (max - min + 1) + min);
	}

	function showMenu(d, i) {
		selectNode = d;
		if (d3.event.defaultPrevented) return;
		hiddenTypeCluster();
		var elementId = "menu_group";
		var arcs = container.append("g").attr("id", elementId)
				.selectAll("g").data(piedata).enter().append("g")
				.attr("transform",
						"translate(" + (d.x) + "," + (d.y) + ")");
		var path = arcs.append("path").attr("fill", function(d, i) {
			return getMenuColor(d);
		}).transition().ease("elastic").duration(750).attrTween(
				"d", arcTween);
		arcs.append("text").attr("transform", function(d) {
			return "translate(" + arc.centroid(d) + ")";
		}).attr("text-anchor", "middle").text(function(d) {
			return d.data.label;
		});
		arcs.on("click", menuClick);
		// arcs.on("mouseout", menuMouseout);
	}

	function arcTween(d) {
		var i = d3.interpolate({
			value : 0
		}, d);
		return function(t) {
			return arc(i(t));
		};
	}

	function mouseover(d, i) {
		d3.select(this).transition().duration(750)
		.attr("width", 20).attr("height", 20);
		link_text.style("fill-opacity", function(link) {
			if (link.navigator_line_display) {
				if (link.source === d || link.target === d) {
					return 1.0;
				} else {
					return 0.0;
				}
			}
		});
		node_text.text(function(node){
			if (node.navigator_node_display) {
				if (node == d) {
					return node.navigator_node_label;
				} else {
					return node.navigator_node_short_label
				}
			}
		});
		node_concept_text.style("fill-opacity", function(node) {
			if (node.navigator_node_display) {
				if (node == d) {
					return 1.0;
				} else {
					return 0.0;
				}
			}
		});
	}

	function type_mouseover(d, i) {
		typeLink.style("fill-opacity", function(link) {
			if (link.source === d || link.target === d) {
				return 1.0;
			} else {
				return 0.0;
			}
		});
	}

	function mouseout(d, i) {
		d3.select(this).transition().duration(250).attr("width",
				image_diameter).attr("height", image_diameter);
		link_text.style("fill-opacity", "0.0");
		node_concept_text.style("fill-opacity", "0.0");
		node_text.text(function(node){
			if (node.navigator_node_display) {
				return node.navigator_node_short_label;
			}
		});
	}

	function type_mouseout(d, i) {
		typeLink.style("fill-opacity", "0.0");
	}

	function tick() {
		node
				.forEach(function(d, i) {
					d.x = d.x - image_diameter / 2 < 0 ? image_diameter / 2
							: d.x;
					d.x = d.x + image_diameter / 2 > width ? width
							- image_diameter / 2 : d.x;
					d.y = d.y - image_diameter / 2 < 0 ? image_diameter / 2
							: d.y;
					d.y = d.y + image_diameter / 2 + image_offset > height ? height
							- image_diameter / 2 - image_offset
							: d.y;
				});
		link.attr(
				"x1",
				function(d) {
					var mid_x = (d.source.x + d.target.x) / 2;
					return d.source.x < mid_x ? d.source.x
							+ image_offset : d.source.x
							- image_offset;
				}).attr(
				"y1",
				function(d) {
					var mid_y = (d.source.y + d.target.y) / 2;
					return d.source.y < mid_y ? d.source.y
							+ image_offset : d.source.y
							- image_offset;
				}).attr(
				"x2",
				function(d) {
					var mid_x = (d.source.x + d.target.x) / 2;
					return d.target.x < mid_x ? d.target.x
							+ image_offset : d.target.x
							- image_offset;
				}).attr(
				"y2",
				function(d) {
					var mid_y = (d.source.y + d.target.y) / 2;
					return d.target.y < mid_y ? d.target.y
							+ image_offset : d.target.y
							- image_offset;
				})
		node.attr("x", function(d) {
			return d.x - image_offset;
		}).attr("y", function(d) {
			return d.y - image_offset;
		});
		link_text.attr("x", function(d) {
			return (d.source.x + d.target.x) / 2;
		}).attr("y", function(d) {
			return (d.source.y + d.target.y) / 2;
		});
		node_text.attr("x", function(d) {
			return d.x;
		}).attr("y", function(d) {
			return d.y + image_diameter / 2;
		});
		node_concept_text.attr("x", function(d) {
			return d.x;
		}).attr("y", function(d) {
			return d.y + image_diameter / 2;
		});
	}

	function zoomed() {
		container.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
	}
	
	function dragstarted(d) {
        d3.event.sourceEvent.stopPropagation();
        d3.select(this).classed("dragging", true);
        force.start();
    }

    function dragged(d) {
    	d3.select(this).attr("cx", d.x = d3.event.x).attr("cy", d.y = d3.event.y);
    }

    function dragended(d) {
    	d3.select(this).classed("dragging", false);
    }

	function getImage(o) {
		if (NAVIGATOR_NODE_IN_ENTITY_TYPE == o.navigator_node_type) {
			return NAVIGATOR_NODE_IMAGE_INBOUND;
		} else if (NAVIGATOR_NODE_OUT_ENTITY_TYPE == o.navigator_node_type) {
			return NAVIGATOR_NODE_IMAGE_OUTBOUND;
		} else if (o.expanded != undefined && o.expanded == false) {
			return NAVIGATOR_NODE_IMAGE_COLLAPSED_DATA;
		} else if (showNode != undefined && getIdentifier(o) == getIdentifier(showNode)) {
			return NAVIGATOR_NODE_IMAGE_SELECTED_DATA;
		} else {
			return NAVIGATOR_NODE_IMAGE_DATA;
		}
	}

	function getIdentifier(o) {
		var identifier;
		if (o.navigator_node_concept !== undefined) {
			identifier = 'navigator_' + o.navigator_node_concept;
		}
		if (o.navigator_node_type == NAVIGATOR_NODE_VALUE_TYPE && o.navigator_node_ids !== undefined) {
			identifier = identifier + '_' + o.navigator_node_ids;
		}
		if(o.navigator_node_type == NAVIGATOR_NODE_IN_ENTITY_TYPE){
			identifier = identifier + '_' + o.navigator_line_label;
		}
		return identifier;
	}
	
	function getClassIndex(o) {
		var identifier = getIdentifier(o);
		for (var i=0;i<class_array.length;i++) {
			if (class_array[i] == identifier) {
				return i;
			}
		}
		class_array[class_index] = identifier;
		return class_index++;
	}

	function hiddenTypeCluster() {
		if (selectNode !== undefined) {
			svg.select("#cluster_type_in_group").remove();
			svg.select("#cluster_type_out_group").remove();
			svg.select("#menu_group").remove();
		}
	}

	function generateNodeLabel(value) {
		if (value != null) {
			if (value.length > nodeLabelLength) {
				return value.substr(0,nodeLabelLength) + '...'
			} else {
				return value;
			}
		} else {
			return '';
		}
	}

	function generateArrowMarker() {
		var arrowMarker_in = container.append("marker").attr("id",
				"arrow_in").attr("markerUnits", "strokeWidth")
				.attr("markerWidth", "10").attr("markerHeight",
						"10").attr("viewBox", "0 0 12 12").attr(
						"refX", "6").attr("refY", "6").attr(
						"orient", "auto");

		var arrow_path_in = "M2,6 L10,2 L6,6 L10,10 L2,6";
		arrowMarker_in.append("path").attr("d", arrow_path_in)
				.style("stroke-width", 1).style("fill", "#ccc");
		var arrowMarker_out = container.append("marker").attr("id",
				"arrow_out").attr("markerUnits", "strokeWidth")
				.attr("markerWidth", "10").attr("markerHeight",
						"10").attr("viewBox", "0 0 12 12").attr(
						"refX", "6").attr("refY", "6").attr(
						"orient", "auto");

		var arrow_path_out = "M2,2 L10,6 L2,10 L6,6 L2,2";
		arrowMarker_out.append("path").attr("d", arrow_path_out)
				.style("stroke-width", 1).style("fill", "#ccc");
	}
	
	function isSupportArrow() {
		return !((os.isWin7 || os.isWin8 || os.isWin81) && browser.isIE)
	}
	
	var os = (function() {
		var UserAgent = navigator.userAgent.toLowerCase();
		return {
		    isWin2K     : /windows nt 5.0/.test(UserAgent),
		    isXP      : /windows nt 5.1/.test(UserAgent),
		    isVista     : /windows nt 6.0/.test(UserAgent),
		    isWin7     : /windows nt 6.1/.test(UserAgent),
		    isWin8     : /windows nt 6.2/.test(UserAgent),
		    isWin81     : /windows nt 6.3/.test(UserAgent)
		};
	}());
	
	var browser = (function() {
		var UserAgent = navigator.userAgent.toLowerCase();
		return {
			isUc   : /ucweb/.test(UserAgent),
		    isChrome : /chrome/.test(UserAgent.substr(-33,6)),
		    isFirefox : /firefox/.test(UserAgent),
		    isOpera  : /opera/.test(UserAgent),
		    isSafire : /safari/.test(UserAgent) && !/chrome/.test(UserAgent),
		    is360   : /360se/.test(UserAgent),
		    isBaidu  : /bidubrowser/.test(UserAgent),
		    isSougou : /metasr/.test(UserAgent),
		    isIE   : /msie/.test(UserAgent),
		    isIE6   : /msie 6.0/.test(UserAgent),
		    isIE7   : /msie 7.0/.test(UserAgent),
		    isIE8   : /msie 8.0/.test(UserAgent),
		    isIE9   : /msie 9.0/.test(UserAgent),
		    isIE10  : /msie 10.0/.test(UserAgent),
		    isIE11  : /msie 11.0/.test(UserAgent),
		    isLB   : /lbbrowser/.test(UserAgent),
		    isWX   : /micromessenger/.test(UserAgent),
		    isQQ   : /qqbrowser/.test(UserAgent)
		};
	}());
	
	function handleFailure(response) {
		if (sessionExpired(response)) {
			amalto.navigator.Navigator.sessionExpired();
		} else {
			if (response.status == 0) {
				Ext.MessageBox.alert('Error', message.getMsg("server_error"));
			} else {
				Ext.MessageBox.alert('Error', response.responseText);
			}
		}
		
	}
	
	function sessionExpired(response) {
		if (response.status == 401) {
			return true;
		} else {
			return false;
		}
	}
	
	function getMenuColor(d) {
		if ('detail' === d.data.name) {
			return NAVIGATOR_MENU_DETAIL_COLOR;
		} else if ('in' === d.data.name) {
			if (selectNode.expanded == undefined || selectNode.expanded) {
				return NAVIGATOR_MENU_IN_COLOR;
			} else {
				return NAVIGATOR_MENU_DISABLE_COLOR;
			}
		} else if ('out' === d.data.name) {
			if (selectNode.expanded == undefined || selectNode.expanded) {
				return NAVIGATOR_MENU_OUT_COLOR;
			} else {
				return NAVIGATOR_MENU_DISABLE_COLOR;
			}
		} else if ('settings' === d.data.name) {
			return NAVIGATOR_MENU_SETTINGS_COLOR;
		}
	}
	
	return {
		initUI : function(restServiceUrlParameter, idParameter, conceptParameter,clusterParameter, hasPrimaryKeyInfo,languageParameter) {
			selectNode = undefined;
			showNode = undefined;
			restServiceUrl = restServiceUrlParameter;
			id = idParameter;
			concept = conceptParameter;
			cluster = clusterParameter;
			language = languageParameter;
			amalto.itemsbrowser.NavigatorPanel.bundle = new Ext.i18n.Bundle({
				bundle : 'Message',
				path : 'secure/resources',
				lang : language
			});
			amalto.itemsbrowser.NavigatorPanel.bundle.onReady(function() {
				message = amalto.itemsbrowser.NavigatorPanel.bundle;
				dataset = [ {
					label : message.getMsg("menu_out_label"),
					name : 'out',
					value : 25
				}, {
					label : message.getMsg("menu_settings_label"),
					name : 'settings',
					value : 25
				}, {
					label : message.getMsg("menu_in_label"),
					name : 'in',
					value : 25
				}, {
					label : message.getMsg("menu_detail_label"),
					name : 'detail',
					value : 25
				} ];
				var zoom = d3.behavior.zoom()
			    .center([width / 2, height / 2])
			    .scaleExtent([1, 10])
			    .on("zoom", zoomed);
				svg = d3.select("#navigator").append("svg")
					  .attr("width",2000)
					  .attr("height", 2000).append("g").call(zoom).on('dblclick.zoom', null);
				rect = svg.append("rect")
					  .attr("width",Ext.get("navigator").dom.style.width)
					  .attr("height",Ext.get("navigator").dom.style.height)
					  .style("fill", "none")
					  .style("stroke", "#b5b8c8")
					  .style("pointer-events", "all");
				rect.on("click", hiddenTypeCluster);
				container = svg.append("g");
	
				pie = d3.layout.pie().value(function(d) {
					return d.value;
				});
				pie.startAngle(0.8);
				pie.endAngle(7.1);
				piedata = pie(dataset);

				arc = d3.svg.arc()
					  .innerRadius(innerRadius)
					  .outerRadius(outerRadius);

				typeCluster = d3.layout.cluster();
				diagonal = d3.svg.diagonal().projection(function(d) {
					return [ d.y, d.x ];
				});
				class_index = 0;
				class_array = new Array();
				generateArrowMarker();
				var ids = new Array(id);
				Ext.Ajax.request({
					url : restServiceUrl + '/data/' + cluster+ '/records/' + concept,
					method : 'GET',
					params : {
						ids : ids,
						language : language
					},
					success : function(response, options) {
						if (hasPrimaryKeyInfo) {
							amalto.navigator.Navigator.handleNodeLabel(response.responseText,0);
						} else {
							initDataNode(response.responseText);
						}
					},
					failure : function(response, options) {
						handleFailure(response);
					}
				});
			});
		},
	
		resize : function() {
			if (rect != undefined) {
				rect.attr("width",Ext.get("navigator").dom.style.width)
				.attr("height",Ext.get("navigator").dom.style.height);
			}
		},
		
		initDataNode : function(data) {
			initDataNode(data);
		},
		
		paintInDataNode : function(data) {
			paintDataNode(eval('(' + data + ')'),NAVIGATOR_NODE_IN_ENTITY_TYPE);
		},
		
		paintOutDataNode : function(data) {
			paintDataNode(eval('(' + data + ')'),NAVIGATOR_NODE_OUT_ENTITY_TYPE);
		}
	}
}();