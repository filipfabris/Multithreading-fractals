package hr.fer.zemris.commons;

import org.apache.commons.cli.*;

public class InitComonsCli {
	
	public static Options init() {
		
		Options options = new Options();
		
	    Option worker = Option.builder("w").longOpt("workers")
	            .argName("count")
	            .hasArg()
	            .required(false) //NotRequired
	            .desc("set number of workers").build();
	    options.addOption(worker);
	    
	    Option tracks = Option.builder("t").longOpt("tracks")
	            .argName("count")
	            .hasArg()
	            .required(false)
	            .desc("set number of tracks").build();
	    options.addOption(tracks);
		
		return options;
	}

}
