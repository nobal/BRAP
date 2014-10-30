package main;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;

import brap.action.PageChangeDetector;
import brap.action.UserActionManager;
import brap.player.Player;
import brap.player.StandAlonePlayer;
import brap.tool.Browser;
import brap.tool.Utils;

import com.beust.jcommander.JCommander;

/**
 * This is the main program. The execution starts from this class.
 * 
 * @author nobal
 * 
 */
public class BrapMain {
	/**
	 * Logger
	 */
	private Logger LOGGER = Logger.getLogger(BrapMain.class.getName());

	/**
	 * Properties used in the program
	 */
	//private Properties props = new Properties();

	/**
	 * Command-line arguments
	 */
	private Params params = new Params();

	/**
	 * The browser
	 */
	private Browser browser;

	/*
	 * Seconds to wait before closing the browser
	 */
	private int waitSeconds = 20;

	public BrapMain(String[] args) {
		LOGGER.setLevel(Level.INFO);

		new JCommander(params, args);

		// If not given use the following default values
		if (params.oDir == null) {
			params.oDir = "output";
		}
		if (params.browser == null) {
			params.browser = "chrome";
		}
		if (params.browserBin == null) {
			params.browserBin = "drivers/chromedriver";
		}
		if(params.port<=0){
			params.port=4444;
		}
		if (params.profile != null) {
			//props.setProperty("profile", params.profile);
		}
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		/*
		args= new String[11];
		args[0]="-browserBin"; 
		args[1]="drivers/chrome"; 
		args[2]="-record"; 
		args[3]="-oDir"; 
		args[4]="output"; 
		args[5]="-scriptsDir"; 
		args[6]="scripts"; 
		args[7]="-file";
		args[8]="record.int";
		args[9]="-url"; 
		args[10]="http://www.abebooks.com/servlet/SearchEntry?cm_sp=TopNav-_-Home-_-Advs -genInfoFiles";
		*/		
		BrapMain tl = new BrapMain(args);

		/*
		//Record 
		tl.params.record = true;
		tl.params.url="http://www.abebooks.com/servlet/SearchEntry?cm_sp=TopNav-_-Home-_-Advs";
		tl.params.file = "expedia.int";
		tl.params.browser="chrome";//firefox
		tl.params.oDir="output";
		//tl.params.genInfoFiles=true;
		tl.params.scriptsDir="scripts";
		
		//Play; now it doesn't need any urls as url is extracted from interaction file
		tl.params.play=true;
		//tl.params.url="http://www.abebooks.com/servlet/SearchEntry?cm_sp=TopNav-_-Home-_-Advs";
		tl.params.file = "output/newInt.txt";
		tl.params.browser="chrome";//firefox
		tl.params.oDir="output";	
					
			
		//Stand alone 
		tl.params.player=true;
		tl.params.url="http://www.abebooks.com/servlet/SearchEntry?cm_sp=TopNav-_-Home-_-Advs";
		//tl.params.file = "output/expedia.int";
		tl.params.browser="chrome";//firefox
		tl.params.oDir="output";	
		*/
		
		if (tl.params.help) {
			Params.showHelp();
			return;
		}	
		
		//validate parameters
		{
			StringBuilder validationMsg = new StringBuilder();

			// script directory
			File scriptDir = new File(tl.params.scriptsDir);
			if (!scriptDir.isDirectory() || !scriptDir.exists()) {
				validationMsg
						.append("* The script directory doesn't exist or is invalid. Check -scriptsDir option. \n");
			}

			// while recording, we should get the URL and op directory
			if (tl.params.record) {
				if (tl.params.url == null)
					validationMsg.append("* The URL is null.\n");
				
				File oDir = new File(tl.params.oDir);
				if (!oDir.isDirectory() || !oDir.exists()) {
					validationMsg
							.append("The  output directory doesn't exist or is invalid.\n");
					return;
				}
			}


			if(tl.params.file==null){
				validationMsg.append("The interaction file is null. Please check -file option");
			}
			else if(tl.params.play) {
				File file = new File(tl.params.file);
				if (file.isDirectory() || !file.exists()) {
					validationMsg.append("The interaction file "
							+ tl.params.file
							+ " doesn't exist or is invalid.\n");
				}
			}
			// Check browserbin for browsers other than firefox (it doesn't need
			// to be specified)
			File browserBin = new File(tl.params.browserBin);
			if (browserBin.isDirectory() || !browserBin.exists()) {
				validationMsg.append("* The driver of the browser "
						+ tl.params.browser + " doesn't exist or is invalid\n");
			}
						
			String validationMessg = validationMsg.toString().trim();

			if (!validationMessg.isEmpty()) {
				System.out
						.println("One or more issues were detected, please try again: \n"
								+ validationMessg);
				return;
			}
		}
		
		
		if (tl.params.play) { 
			tl.play(tl.params.url);
		} 
		else if (tl.params.record) {
			tl.record(tl.params.url);
		} 
		else if (tl.params.playerserver) {
			tl.launchPlayer(tl.params.port);
		}
		 
		System.out.println("Done!");
		System.exit(0);
	}

	public void openBrowser(String url,String type,String browserBin,String profile) {
		try {
			browser = new Browser(url,type,browserBin,profile);
			Thread.sleep(5);

		} catch (Exception e) {
			LOGGER.severe(e + "");
		}
	}
	
	/**
	 * @param browser
	 * @param second
	 */
	public void closeBrowser(int second) {
		long seconds = second * 1000L;
		try {
			Thread.sleep(seconds);
			browser.close();
		} catch (InterruptedException e) {
			// e.printStackTrace();
		}
	}

	private void record(String url) {		
		if (url.equals(null)) {
			LOGGER.info("The URL is null. Quitting now!");
			return;
		}
		LOGGER.info("Processing URL: " + url);
		File oDir = new File(params.oDir);
		File scriptDir = new File(params.scriptsDir);

		LOGGER.info("Output directory is: " + oDir.getAbsolutePath());
		if (!oDir.isDirectory() || !oDir.exists()) {
			LOGGER.info("The  output directory doesn't exist or invalid. Quitting now!");
			return;
		}
		if (!url.contains("://")) {
			url = "http://" + url;
		}
		String userActionLogFile = oDir.getAbsolutePath() + File.separator+  params.file;
		try {
			// Open the browser and load the URL
			openBrowser(url,params.browser,params.browserBin,params.profile);
			
			// Now handle UserActionManager to capture user events		
			UserActionManager uaManager = new UserActionManager(params.port, browser,
					userActionLogFile, params.oDir,params.genInfoFiles, oDir.getAbsolutePath() + File.separator+params.file,scriptDir.getAbsolutePath());
			uaManager.processShownTell();
			
			LOGGER.info("Press any key to quit the program.");
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			PageChangeDetector.stop = true;
			closeBrowser(PageChangeDetector.sleepTime);
		} catch (Exception e) {
			System.err.println(e);
		}		
	}

	public void play(String url) {
		String interactionFile = params.file;	 
		if (url!=null && !url.contains("://"))
			url = "http://" + url;

		// open the browser and load the URL
		openBrowser(url,params.browser,params.browserBin,params.profile);
		WebDriver driver = browser.getWebDriver();

		// load the values
		LOGGER.info("Loading values from: " + interactionFile);
		List<String> lines = Utils.readLines(params.file);
		LOGGER.info("Starting URL:" + url + ", Total Size: " + lines.size());
		String results = "";
		
		// There should be at least two lines to proceed (header line plus recorded line)
		if (lines.size() < 2) {
			LOGGER.info("No data for playing interactions.");
			return;			
		} else {
			if (url == null) {
				url = lines.get(1).split("\t")[0];
				if (!url.contains("://"))
					url = "http://" + url;
			}
		}
		Player player = new Player(driver);

		results = player.playActions(lines, url);
		// Print the result in the console
		System.out.println(results);
		
		LOGGER.info("Press any key to quit the program.");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		PageChangeDetector.stop = true;
		closeBrowser(PageChangeDetector.sleepTime);

	}
	
	/**
	 * Launches a browser and waits for HTTP requests to process the recordings
	 * format:
	 * http://localhost:port/brap?q=URL_ENCODED_CONTENT_OF_INTERACTION_FILE
	 * (insert \n between lines before encoding)
	 * 
	 * @param port
	 */
	private void launchPlayer(int port) {
		// open the browser, load nothing
		String url = "about:blank";
		openBrowser(url,params.browser,params.browserBin,params.profile);
		StandAlonePlayer player = new StandAlonePlayer(browser.getWebDriver(),
				port);
		player.run();
	}
}
