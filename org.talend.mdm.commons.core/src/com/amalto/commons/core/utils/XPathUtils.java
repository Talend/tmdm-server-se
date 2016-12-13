/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.commons.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.amalto.commons.core.utils.xpath.JXPathContext;
import com.amalto.commons.core.utils.xpath.JXPathException;
import com.amalto.commons.core.utils.xpath.ri.Compiler;
import com.amalto.commons.core.utils.xpath.ri.JXPathCompiledExpression;
import com.amalto.commons.core.utils.xpath.ri.QName;
import com.amalto.commons.core.utils.xpath.ri.compiler.Constant;
import com.amalto.commons.core.utils.xpath.ri.compiler.CoreFunction;
import com.amalto.commons.core.utils.xpath.ri.compiler.CoreOperation;
import com.amalto.commons.core.utils.xpath.ri.compiler.Expression;
import com.amalto.commons.core.utils.xpath.ri.compiler.ExtensionFunction;
import com.amalto.commons.core.utils.xpath.ri.compiler.LocationPath;
import com.amalto.commons.core.utils.xpath.ri.compiler.NodeNameTest;
import com.amalto.commons.core.utils.xpath.ri.compiler.NodeTest;
import com.amalto.commons.core.utils.xpath.ri.compiler.NodeTypeTest;
import com.amalto.commons.core.utils.xpath.ri.compiler.Operation;
import com.amalto.commons.core.utils.xpath.ri.compiler.Path;
import com.amalto.commons.core.utils.xpath.ri.compiler.ProcessingInstructionTest;
import com.amalto.commons.core.utils.xpath.ri.compiler.Step;
import com.amalto.commons.core.utils.xpath.ri.compiler.VariableReference;

public class XPathUtils {


	/**
	 * Factors an XPath according to a Map of pivots and create new pivots as needed<br/>
	 * <br/>
	 * @param targetXPath
	 * @param pivotsMap
	 * 		A Map of xPath with the pivot name as the key and the xPath as the value
	 * @return
	 * 		The factored compiled expression
	 * @throws JXPathException
	 */
	public static Expression factor(
		String targetXPath,
		LinkedHashMap<String, String> pivotsMap
	)  throws JXPathException{
		return factor(targetXPath, pivotsMap, true, true);
	}


	/**
	 * Factors an XPath according to a Map of pivots and optionally create new pivots<br/>
	 * <br/>
	 * @param targetXPath
	 * 		The target xPath as an xPath String
	 * @param pivotsMap
	 * 		A Map of xPath with the pivot name as the key and the xPath as the value
	 * @param forceAncestors
	 * @param createPivots
	 * @return
	 * 		The factored compiled expression
	 * @throws JXPathException
	 */
	public static Expression factor(
		String targetXPath,
		LinkedHashMap<String, String> pivotsMap,
		boolean forceAncestors,
		boolean createPivots
	)  throws JXPathException{

		//Compile the target
		Expression targetPath = compileXPath(targetXPath);

		return factorExpression(targetPath, pivotsMap, forceAncestors, createPivots);
	}


	/**
	 * Factors an XPath according to a Map of pivots and optionally create new pivots<br/>
	 * <br/>
	 * @param targetXPath
	 * 		The target xPath as an {@link Expression}
	 * @param pivotsMap
	 * 		A Map of xPath with the pivot name as the key and the xPath as the value
	 * @param forceAncestors
	 * @param createPivots
	 * @return
	 * 		The factored compiled expression
	 * @throws JXPathException
	 */
	public static Expression factorExpression(
		Expression targetXPath,
		LinkedHashMap<String, String> pivotsMap,
		boolean forceAncestors,
		boolean createPivots
	)  throws JXPathException{

		//check the pivots map and compile it
		LinkedHashMap<String, LocationPath> compiledPivotsMap = new LinkedHashMap<String, LocationPath>();
		for(Entry<String, String> pivot : pivotsMap.entrySet()) {
			Expression pivotExpr = compileXPath(pivot.getValue());
			if (! (pivotExpr instanceof LocationPath)) {
				throw new JXPathException("The pivot '"+pivot.getKey()+"' must be a 'simple' path. ["+pivot.getValue()+"]");
			}
			compiledPivotsMap.put(pivot.getKey(), (LocationPath)pivotExpr);
		}

		//factor
		Expression result = factor(targetXPath, compiledPivotsMap, forceAncestors, createPivots);

		//copy compiled Pivots Map back in to pivots map
		if (createPivots) {
			for(Entry<String, LocationPath> pivot : compiledPivotsMap.entrySet()) {
				pivotsMap.put(pivot.getKey(), pivot.getValue().toString());
            }
		}

		return result;
	}


	/**
	 * Factors an XPath according to a Map of pivots and optionally create new pivots<br/>
	 * <br/>
	 * @param targetXPath
	 * 		The Target Xpath as an {@link Expression}
	 * @param pivotsMap
	 * 		A Map of xPath with the pivot name as the key and the xPath as the value compiled to a {@link LocationPath}
	 * @param forceAncestors
	 * @param createPivots
	 * @return
	 * 		The factored compiled expression
	 * @throws JXPathException
	 */
	public static Expression factor(
		Expression targetPath,
		LinkedHashMap<String, LocationPath> pivotsMap,
		boolean forceAncestors,
		boolean createPivots
	)  throws JXPathException{

		org.apache.log4j.Logger.getLogger(XMLUtils.class).trace("factor() "+targetPath);

	    //Constant, Operation, Path, VariableReference
		if (targetPath instanceof Constant) {
			return targetPath;

		} else if (targetPath instanceof Operation) {
			Operation o = (Operation)((Operation)targetPath).clone(true);
			//process arguments
			Expression[] arguments = o.getArguments();
            if (arguments != null) {
            	for (int j = 0; j < arguments.length; j++) {
            		arguments[j] = factor(arguments[j], pivotsMap, forceAncestors, createPivots);
                }
            }
            return o;

		} else if (targetPath instanceof Path) {
			Path p = (Path)targetPath;
			//loop on pivots an try to factor
			boolean factored = false;
			Path newPath = null;
			for(Entry<String, LocationPath> pivot : pivotsMap.entrySet()) {
				 newPath = factorPath(
					p,
					pivot.getValue(),
					pivot.getKey(),
					forceAncestors
				);
				if (newPath != null && ! newPath.equals(p)) {
					factored = true;
					break;
				}
			}
			if (! factored && createPivots && (p instanceof LocationPath)) {
				String name = "$pivot"+pivotsMap.size();

//				//create a pivot from the path without the last predicates
//				LocationPath newPivotPath = ((LocationPath)p).clone(true);
//				Step lastStep = newPivotPath.getSteps()[newPivotPath.getSteps().length-1];
//				Expression[] lastPredicates = lastStep.getPredicates();
//				lastStep.setPredicates(null);
//				pivotsMap.put(name, newPivotPath);
//				org.apache.log4j.Logger.getLogger(XPathUtils.class).trace(
//					"factor() --> adding pivot '"+name+"': "+newPivotPath.toString()
//				);
//
//				//build a path from the new pivot
//				Step step = new Step(
//					Compiler.AXIS_CHILD,
//		    		new NodeNameTest(new QName(name)),
//		    		lastPredicates
//		    	);

				//Create a pivot from the path
				LocationPath newPivotPath = ((LocationPath)p).clone(true);
				pivotsMap.put(name, newPivotPath);
				org.apache.log4j.Logger.getLogger(XPathUtils.class).trace(
					"factor() --> adding pivot '"+name+"': "+newPivotPath.toString()
				);

				//build a path from the new pivot
				Step step = new Step(
					Compiler.AXIS_CHILD,
		    		new NodeNameTest(new QName(name)),
		    		new Expression[0]
		    	);

				return new LocationPath(false, new Step[]{step});
			}
			return newPath;

		} else if (targetPath instanceof VariableReference) {
			return targetPath;
		}

		//should never happen
		return targetPath;


	}


	/**
	 * Attempt to re-factor an xPath from a pivot.
	 * @param xPathTarget
	 * @param xPathPivot
	 * @param pivotName
	 * @param forceAncestors
	 * @return
	 * 		The new xPath if it could be re-factored, the old one otherwise
	 * @throws JXPathException
	 */
	public static Expression factor(
		String xPathTarget,
		String xPathPivot,
		String pivotName,
		boolean forceAncestors
	)  throws JXPathException{

		LinkedHashMap<String, String> pivotsMap = new LinkedHashMap<String, String>();
		pivotsMap.put(xPathPivot, pivotName);

		return factor(xPathTarget, pivotsMap, forceAncestors, false);

	}



	public static Path factorPath(
		Path targetPath,
		LocationPath pivotPath,
		String pivotName,
		boolean forceAncestors
	)  throws JXPathException{

		Collection<Expression> remainingStepPredicates = new ArrayList<Expression>();

		Step[] pivotSteps = pivotPath.getSteps();
		if (pivotSteps == null) return targetPath;

		Step[] steps = targetPath.getSteps();
		if (steps == null) return targetPath;

		int i = 0;
		for (i = 0; i < steps.length && i < pivotSteps.length; i++) {
            Step step = steps[i];
            Step pivotStep = pivotSteps[i];
            remainingStepPredicates = new ArrayList<Expression>();

            org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() --"+(i+1)+"-comparing '"+step+"' with pivot step '"+pivotStep+"'");

            if (step.getAxis() ==  pivotStep.getAxis()) {
            	org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() ------axis identical");
            	if (step.getNodeTest().equals(pivotStep.getNodeTest())) {
            		org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() ------node step identical");
            		//check if all pivot predicates are included in this step predicates
            		ArrayList<Expression> pivotPredicates = new ArrayList<Expression>(Arrays.asList(pivotStep.getPredicates()));
            		Collection<Expression> stepPredicates = Arrays.asList(step.getPredicates());
            		for (Iterator<Expression> iterator = stepPredicates.iterator(); iterator.hasNext();) {
                        Expression predicate = iterator.next();
                        org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() --------looking at step predicate ["+predicate+"]");
                        //find in pivot predicates
                        Expression foundPivotPredicate = null;
                        for (Iterator<Expression> iterator2 = pivotPredicates.iterator(); iterator2.hasNext();) {
                            Expression pivotPredicate = iterator2.next();
                            org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() ----------comparing with pivot predicate ["+pivotPredicate+"]");
                            if (predicate.equals(pivotPredicate)) {
                            	foundPivotPredicate = pivotPredicate;
                            	break;
                            }
                        }
                        if (foundPivotPredicate != null) {
                        	//remove from pivot predicates
                        	pivotPredicates.remove(foundPivotPredicate);
                        } else {
                        	//add to remaining step predicates
                        	remainingStepPredicates.add(predicate);
                        }
                    }
            		//check if there are no more pivot Predicates
            		if (pivotPredicates.size() == 0) {
            			//then the pivot Step is included in the step
            			if (remainingStepPredicates.size() ==0) {
            				//identical
            				org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() --------predicates identical");
            				continue;
            			} else {
            				//as a special case if there are more predicates on the last step, we are happy
            				if (i == pivotSteps.length -1) {
            					org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() ------predicates identical but with additional predicates");
            					continue;
            				}
            				//different
            			}
            		}
            	}
            }
            org.apache.log4j.Logger.getLogger(XPathUtils.class).trace("factor() ----different");
            break;
		}

		//no match on any step, this cannot be factored
		if (i==0) {
			return targetPath;
		}

		//the pivot is only partially included in the target,
		//but we do not force ancestors: return the targets
		if (i < pivotSteps.length) {
			if (! forceAncestors) return targetPath;
		}

        //the steps are different
        //end of the process, the steps are different
    	//start with the pivot
    	Step step1 = new Step(
    		Compiler.AXIS_CHILD,
    		new NodeNameTest(new QName(pivotName)),
    		remainingStepPredicates.toArray(new Expression[remainingStepPredicates.size()])
    	);
    	LocationPath newPath = new LocationPath(
    		false,
    		new Step[] {step1}
    	);
    	//add /.. to commonPath for all remaining pivotSteps
    	int remaining = pivotSteps.length - i;
    	for (int j = 0; j < remaining; j++) {
    		if (pivotSteps[j].getAxis()==Compiler.AXIS_CHILD || pivotSteps[j].getAxis()==Compiler.AXIS_ATTRIBUTE) {
        		Step predecessor = new Step(
        			Compiler.AXIS_PARENT,
        			new NodeTypeTest(Compiler.NODE_TYPE_NODE),
        			null
        		);
                newPath.addStep(predecessor);
    		}
        }
    	//add all remaining target Path steps
    	for (int j = i; j < steps.length; j++) {
    		newPath.addStep(steps[j]);
        }

    	return newPath;

	}


	/**
	 * Compiles a xPath in a String format to a compiled {@link Expression}
	 * @param xPath
	 * 		The xPath as a String
	 * @return
	 * 		The compiled xPath
	 */
	public static Expression compileXPath(String xPath) {
		return ((JXPathCompiledExpression)JXPathContext.compile(xPath)).getExpression();
	}


	/**
	 * Dumps to a String an XPath Expression
	 * @param xPath
	 * @return
	 */
	public static String dump(String xPath) {
		return "Dump of '"+xPath+"'\n"+dump(compileXPath(xPath), "" , "");
	}

	private static String dump(Expression e, String dump, String level) {

		String res = dump;

	    //Constant, Operation, Path, VariableReference
		if (e instanceof Constant) {
			Constant c = (Constant)e;
			res+=level+" constant.: "+c.getValue()+"\n";

		} else if (e instanceof Operation) {
			Operation o = (Operation)e;
		    //CoreFunction, CoreOperation, ExtensionFunction
			if (o instanceof CoreFunction) {
				CoreFunction cf = (CoreFunction)o;
				res+=level+" core function: "+cf.getFunctionName()+"\n";
			} else if (o instanceof CoreOperation) {
				CoreOperation co = (CoreOperation)o;
				res+=level+" core operation: "+co.getSymbol()+"\n";
			} if (o instanceof ExtensionFunction) {
				ExtensionFunction ef = (ExtensionFunction)o;
				res+=level+" core operation: "+ef.getFunctionName()+"\n";
			}
			//process arguments
			Expression[] arguments = o.getArguments();
            if (arguments != null) {
            	for (int j = 0; j < arguments.length; j++) {
            		res+=dump(arguments[j],dump,level+"--");
                }
            }

		} else if (e instanceof Path) {
			Path p = (Path)e;
			Step[] steps = p.getSteps();
			res+=level+" path: "+steps.length+" steps\n";
			if (steps != null) {
				for (int i = 0; i < steps.length; i++) {
	                Step step = steps[i];
	                res+=level+"--"+" Step: "+i+"\n";

	                int axis = step.getAxis();
	                res+=level+"----"+" axis: "+Step.axisToString(axis)+"\n";

	                NodeTest nodeTest = step.getNodeTest();
	                //NodeNameTest, NodeTypeTest, ProcessingInstructionTest
	                if (nodeTest instanceof NodeNameTest) {
	                	NodeNameTest nnt = (NodeNameTest)nodeTest;
	                	res+=level+"----"+" node name test: "+nnt.toString()+"\n";
	    			} else if (nodeTest instanceof NodeTypeTest) {
	    				NodeTypeTest ntt = (NodeTypeTest)nodeTest;
	    				res+=level+"----"+" node type test: "+ntt.toString()+"\n";
	    			} else if (nodeTest instanceof ProcessingInstructionTest) {
	    				ProcessingInstructionTest pit = (ProcessingInstructionTest)nodeTest;
	    				res+=level+"----"+" processing instruction test: "+pit.toString()+"\n";
	    			}

	                Expression[] predicates = step.getPredicates();
	                if (predicates != null) {
	                	res+=level+"----"+" processing "+predicates.length+" predicates\n";
	                	for (int j = 0; j < predicates.length; j++) {
	                		res+=dump(predicates[j],dump,level+"------");
                        }
	                }

                }
			}

		} else if (e instanceof VariableReference) {
			VariableReference cr = (VariableReference)e;
			res+=level+" variable ref.: "+cr.getVariableName()+"\n";
		}

		return res;

	}




	/******************************************************************
	 *
	 * TESTS
	 *
	 ******************************************************************/


	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getLogger("com.amalto.commons.core.utils.XPathUtils").setLevel(Level.ALL);
		testDump();
		System.out.println("\n\n\n");
		testPathFactor();
		System.out.println("\n\n\n");
		testFullFactor();
    }

	public static void testDump() {
		System.out.println(dump("gx_uoms[unece/text()='VAL']/generix/text()"));
		System.out.println();
		System.out.println(dump("concat(generix/text(),'a')"));
		System.out.println();
		System.out.println(dump("generix/text()"));
		System.out.println();
		System.out.println(dump("concat(/generix/../text(),'a')"));
		System.out.println();
		System.out.println(dump("toto/@attribute"));
		System.out.println();
		System.out.println(dump("\n"+
        "concat("+"\n"+
        "    	gx_costcenters[code/text()='aaa']/section/text(),"+"\n"+
        "       '$$$',"+"\n"+
        "       gx_costcenters[code/text()='a']/projet/text()"+"\n"+
        "     )"+"\n"
        ));
		System.out.println();
		System.out.println(dump("if(@discount,'a','b')"));

	}

	public static void testFullFactor() {
		{//Standard with additional predicates
    		String pivotName = "pivot0";
    		String pivot = "gx_uoms";
    		String target = "gx_uoms[unece='3']/generix";
    		LinkedHashMap<String, String> pivotsMap = new LinkedHashMap<String, String>();
    		pivotsMap.put(pivotName, pivot);
    		boolean forceAncestors = false;
    		boolean createPivots = true;
    		System.out.println(
    			"Pivot("+pivotName+"): "+pivot+"\n"+
    			"Target: "+target+"\n"+
    			"Force: "+forceAncestors+"\n"+
    			"Create: "+createPivots+"\n"+
    			"Result: "+factor(target, pivotsMap, forceAncestors, createPivots)
    		);
    		for (Entry<String,String> newPivot : pivotsMap.entrySet()) {
    			System.out.println("Pivots List '"+newPivot.getKey()+"': "+newPivot.getValue());
            }
    		System.out.println("++++++++++++++++");
		}
		{//Operation
    		String pivotName = "pivot0";
    		String pivot = "gx_uoms";
    		String target = "concat(gx_uoms[unece='3']/generix, gx_uoms[unece='3']/description)";
    		LinkedHashMap<String, String> pivotsMap = new LinkedHashMap<String, String>();
    		pivotsMap.put(pivotName, pivot);
    		boolean forceAncestors = false;
    		boolean createPivots = true;
    		System.out.println(
    			"Pivot("+pivotName+"): "+pivot+"\n"+
    			"Target: "+target+"\n"+
    			"Force: "+forceAncestors+"\n"+
    			"Create: "+createPivots+"\n"+
    			"Result: "+factor(target, pivotsMap, forceAncestors, createPivots)
    		);
    		for (Entry<String,String> newPivot : pivotsMap.entrySet()) {
    			System.out.println("Pivots List '"+newPivot.getKey()+"': "+newPivot.getValue());
            }
    		System.out.println("++++++++++++++++");
		}
		{//Create Pivots
    		String pivotName = "pivot0";
    		String pivot = "gx_uoms";
    		String target = "concat(gx_uoms[unece='3']/generix, gx_other[unece='3']/description, gx_other[unece='3']/description[a='1'])";
    		LinkedHashMap<String, String> pivotsMap = new LinkedHashMap<String, String>();
    		pivotsMap.put(pivotName, pivot);
    		boolean forceAncestors = true;
    		boolean createPivots = true;
    		System.out.println(
    			"Pivot("+pivotName+"): "+pivot+"\n"+
    			"Target: "+target+"\n"+
    			"Force: "+forceAncestors+"\n"+
    			"Create: "+createPivots+"\n"+
    			"Result: "+factor(target, pivotsMap, forceAncestors, createPivots)
    		);
    		for (Entry<String,String> newPivot : pivotsMap.entrySet()) {
    			System.out.println("Pivots List '"+newPivot.getKey()+"': "+newPivot.getValue());
            }
    		System.out.println("++++++++++++++++");
		}

	}

	public static void testPathFactor() {
		{//Standard with additional predicates
    		String pivotName = "$";
    		String pivot = "gx_uoms";
    		String target = "gx_uoms[unece='3']/generix";
    		boolean forceAncestors = false;
    		LocationPath pivotPath = (LocationPath)compileXPath(pivot);
    		Path targetPath = (Path)compileXPath(target);
    		System.out.println(
    			"Pivot("+pivotName+"): "+pivot+"\n"+
    			"Target: "+target+"\n"+
    			"Force: "+forceAncestors+"\n"+
    			"Result: "+factorPath(targetPath, pivotPath, pivotName, forceAncestors)+"\n"
    		);
		}

		{//With ancestors
    		String pivotName = "$";
    		String pivot = "gx_uoms[unece='3']/child";
    		String target = "gx_uoms[unece='3']/generix";
    		boolean forceAncestors = true;
    		LocationPath pivotPath = (LocationPath)compileXPath(pivot);
    		Path targetPath = (Path)compileXPath(target);
    		System.out.println(
    			"Pivot("+pivotName+"): "+pivot+"\n"+
    			"Target: "+target+"\n"+
    			"Force: "+forceAncestors+"\n"+
    			"Result: "+factorPath(targetPath, pivotPath, pivotName, forceAncestors)+"\n"

    		);
		}

		{//confusing
    		String pivotName = "$";
    		String pivot = "gx_uoms[unece='3']/generix[xx='rr']/u";
    		String target = "gx_uoms[unece='3']/generix";
    		boolean forceAncestors = false;
    		LocationPath pivotPath = (LocationPath)compileXPath(pivot);
    		Path targetPath = (Path)compileXPath(target);
    		System.out.println(
    			"Pivot("+pivotName+"): "+pivot+"\n"+
    			"Target: "+target+"\n"+
    			"Force: "+forceAncestors+"\n"+
    			"Result: "+factorPath(targetPath, pivotPath, pivotName, forceAncestors)+"\n"

    		);
		}

	}

}



