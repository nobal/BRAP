package main;

import com.beust.jcommander.Parameter;

/**
 * Command-line parameters
 */
public class Params {
	@Parameter(names = "-log", description="Level of verbosity in logging")
	int log = 1;
	
	@Parameter(names="-url", description = "Url to fill in")
	String url = null;
	
	@Parameter(names="-file", description = "File containing values to fill into url")
	String file = null;
	
	@Parameter(names="-browser", description = "Type of browser e.g. chrome, firefox")
	String browser = null;
	
	@Parameter(names="-browserBin", description = "Full path of the selenium driver")
	String browserBin = null;
	
	@Parameter(names="-oDir", description = "Output Directory e.g. output")
	String oDir = null;
	
	@Parameter(names="-scriptsDir", description = "Directory containing JavaScripts")
	String scriptsDir="scripts";
		
	@Parameter(names="-play", description = "Fill in values from an interaction file (e.g. actions.int)")
	boolean play = false;
	
	@Parameter(names="-record", description = "Enable BRAP recording")
	boolean record = false;
	
	@Parameter(names = "--help", help = true)
	boolean help;
	
	@Parameter(names = "-port", help = true)
	int port;
	
	@Parameter(names = "-genInfoFiles", help = true)
	boolean genInfoFiles=false;

	
	@Parameter(names="-profile", description = "Profile for the browser")
	String profile= null;
	
	@Parameter(names="-playerserver", description = "Launch a player in server mode")
	boolean playerserver=false;
	
	public  String validateParameters(){
		String result=""; 
		if(url==null || url.isEmpty()){
			result = "-url is either null or empty.\n";
		}
		if(file==null || file.isEmpty()){
			result = "-file is is either null or empty.\n";
		}
		if(browser==null || browser.isEmpty()){
			result = "-browser is is either null or empty.\n";
		}
		
		
		
		return result;
	}
	
	
	public static void showHelp(){
		String help="java -jar BRAP.jar -browserBin drivers/chrome -[record|play] -oDir output[default] -scriptDir scripts -file recorded.int -url http://www.expedia.com" +
				"\nOptional parameters:" +
				"\n\t -browser [chrome|firefox]" +
				"\n\t -profile  profile file name for the browser" +
				"\n\t -port port number for the recorder. Default 4444 " +
				"\n\t -genInfoFiles true|false. Default false. If true, it generates .info files \n";
		System.out.println(help);
	}
	
}
