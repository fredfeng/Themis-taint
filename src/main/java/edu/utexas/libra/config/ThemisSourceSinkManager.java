package edu.utexas.libra.config;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.AccessPathFactory;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.SourceInfo;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.internal.AbstractJimpleIntBinopExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIfStmt;

/**
 * SourceManager implementation for Themis
 * 
 * @author Yu Feng
 */
public class ThemisSourceSinkManager implements ISourceSinkManager {
	private Collection<String> sources;
	private Collection<String> sinks;

	private Collection<String> returnTaintMethods;
	private Collection<String> parameterTaintMethods;
	
	/**
	 * Creates a new instance of the {@link DefaultSourceSinkManager} class
	 * 
	 * @param sources
	 *            The list of methods to be treated as sources
	 * @param sinks
	 *            The list of methods to be treated as sins
	 */
	public ThemisSourceSinkManager(Collection<String> sources, Collection<String> sinks) {
		this(sources, sinks, null, null);
	}

	/**
	 * Creates a new instance of the {@link DefaultSourceSinkManager} class
	 * 
	 * @param sources
	 *            The list of methods to be treated as sources
	 * @param sinks
	 *            The list of methods to be treated as sinks
	 * @param parameterTaintMethods
	 *            The list of methods whose parameters shall be regarded as
	 *            sources
	 * @param returnTaintMethods
	 *            The list of methods whose return values shall be regarded as
	 *            sinks
	 */
	public ThemisSourceSinkManager(Collection<String> sources, Collection<String> sinks, Collection<String> parameterTaintMethods, Collection<String> returnTaintMethods) {
		this.sources = sources;
		this.sinks = sinks;
		this.parameterTaintMethods = (parameterTaintMethods != null) ? parameterTaintMethods : new HashSet<String>();
		this.returnTaintMethods = (returnTaintMethods != null) ? returnTaintMethods : new HashSet<String>();
	}

	/**
	 * Sets the list of methods to be treated as sources
	 * 
	 * @param sources
	 *            The list of methods to be treated as sources
	 */
	public void setSources(List<String> sources) {
		this.sources = sources;
	}

	/**
	 * Sets the list of methods to be treated as sinks
	 * 
	 * @param sinks
	 *            The list of methods to be treated as sinks
	 */
	public void setSinks(List<String> sinks) {
		this.sinks = sinks;
	}
	
	@Override
	public SourceInfo getSourceInfo(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg) {
		
		SootMethod callee = sCallSite.containsInvokeExpr() ?
				sCallSite.getInvokeExpr().getMethod() : null;
		
		AccessPath targetAP = null;
		if (callee != null && sources.contains(callee.toString())) {
			if (callee.getReturnType() != null 
					&& sCallSite instanceof DefinitionStmt) {
				// Taint the return value
				Value leftOp = ((DefinitionStmt) sCallSite).getLeftOp();
				targetAP = AccessPathFactory.v().createAccessPath(leftOp, true);
			}
			else if (sCallSite.getInvokeExpr() instanceof InstanceInvokeExpr) {
				// Taint the base object
				Value base = ((InstanceInvokeExpr) sCallSite.getInvokeExpr()).getBase();
				targetAP = AccessPathFactory.v().createAccessPath(base, true);
			}
		}
		// Check whether we need to taint parameters
		else if (sCallSite instanceof IdentityStmt) {
			IdentityStmt istmt = (IdentityStmt) sCallSite;
			if (istmt.getRightOp() instanceof ParameterRef) {
				ParameterRef pref = (ParameterRef) istmt.getRightOp();
				SootMethod currentMethod = cfg.getMethodOf(istmt);
				if (parameterTaintMethods.contains(currentMethod.toString()))
					targetAP = AccessPathFactory.v().createAccessPath(currentMethod.getActiveBody()
							.getParameterLocal(pref.getIndex()), true);
			}
		}
		if (sCallSite instanceof JAssignStmt) {
			JAssignStmt jas = (JAssignStmt) sCallSite;
			Value leftOp = jas.getLeftOp();
			if (jas.getRightOp() instanceof FieldRef) {
				FieldRef fr = (FieldRef) jas.getRightOp();
				if (sources.contains(fr.getField().getSignature()))
					targetAP = AccessPathFactory.v().createAccessPath(leftOp, true);
			}
		}

		if (targetAP == null)
			return null;
		
		// Create the source information data structure
		return new SourceInfo(targetAP);
	}

	@Override
	public boolean isSink(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg,
			AccessPath ap) {
		// Check whether values returned by the current method are to be
		// considered as sinks
		if (this.returnTaintMethods != null
				&& sCallSite instanceof ReturnStmt
				&& this.returnTaintMethods.contains(cfg.getMethodOf(sCallSite).getSignature()))
			return true;
		
		// Check whether the callee is a sink
		if(sCallSite instanceof JIfStmt) {
			JIfStmt jif = (JIfStmt) sCallSite;
			AbstractJimpleIntBinopExpr cond = (AbstractJimpleIntBinopExpr)jif.getCondition();
			return true;
		}
		
		if (this.sinks != null
				&& sCallSite.containsInvokeExpr()
				&& this.sinks.contains(sCallSite.getInvokeExpr().getMethod().getSignature())) {
			InvokeExpr iexpr = sCallSite.getInvokeExpr();
			
			
			// Check that the incoming taint is visible in the callee at all
			if (SystemClassHandler.isTaintVisible(ap, iexpr.getMethod())) {
				// If we don't have an access path, we can only over-approximate
				if (ap == null)
					return true;
				
				// The given access path must at least be referenced somewhere in the sink
				if (!ap.isStaticFieldRef()) {
					for (int i = 0; i < iexpr.getArgCount(); i++)
						if (iexpr.getArg(i) == ap.getPlainValue()) {
							if (ap.getTaintSubFields() || ap.isLocal())
								return true;
						}
					if (iexpr instanceof InstanceInvokeExpr)
						if (((InstanceInvokeExpr) iexpr).getBase() == ap.getPlainValue())
							return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Sets the list of methods whose parameters shall be regarded as taint
	 * sources
	 * 
	 * @param parameterTaintMethods
	 *            The list of methods whose parameters shall be regarded as
	 *            taint sources
	 */
	public void setParameterTaintMethods(List<String> parameterTaintMethods) {
		this.parameterTaintMethods = parameterTaintMethods;
	}

	/**
	 * Sets the list of methods whose return values shall be regarded as taint
	 * sinks
	 * 
	 * @param returnTaintMethods
	 *            The list of methods whose return values shall be regarded as
	 *            taint sinks
	 */
	public void setReturnTaintMethods(List<String> returnTaintMethods) {
		this.returnTaintMethods = returnTaintMethods;
	}

	
}
