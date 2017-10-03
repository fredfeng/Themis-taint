package edu.utexas.libra.config;

import java.util.LinkedList;
import java.util.List;

import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;

public class SootConfigForThemis implements IInfoflowConfig{

	@Override
	public void setSootOptions(Options options) {
		// explicitly include packages for shorter runtime:
		List<String> excludeList = new LinkedList<String>();
		//excludeList.add("java.*");
		excludeList.add("sun.misc.*");
		excludeList.add("android.*");
		//excludeList.add("org.apache.*");
		excludeList.add("soot.*");
		//excludeList.add("javax.servlet.*");
		options.set_exclude(excludeList);
		Options.v().set_no_bodies_for_excluded(true);
		options.set_output_format(Options.output_format_none);
	}

}