package brap.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import brap.form.Form;
import brap.form.FormElement;
import brap.form.FormExtractor;
import brap.tool.Browser;
import brap.tool.Utils;

public class UserActionManager {
	/**
	 * Logger
	 */
	private Logger LOGGER = Logger.getLogger(UserActionManager.class.getName());
	public static List<String> browserEventLog;
	private List<Form> forms;
	private static UserActionListenerServer server;
	private Browser browser;
	private int port;
	private UserActionLogger actionlogger;
	private String interactionLogFile;
	private String csvFile;
	private String workingDir;
	PageChangeDetector detector;
	private boolean genInfoFiles=false;
	private int pageDetectorActivationTime = 2;//activate at every 2 seconds
	private String scriptsFolder;

	private static boolean isJQueryInjected = false;

	private static int pageNumber = 1;

	public static int waitTimeInSecondsAfterPageChanged = 4;

	public UserActionManager(int localServerPort, Browser browser,
			String eventLogFile, String workingDir, boolean genInfoFiles, String csvFile,String scriptsFolder) throws IOException {
		this.interactionLogFile = eventLogFile;
		this.workingDir = workingDir;
		this.csvFile = csvFile;
		this.port = localServerPort;
		this.browser = browser;
		this.genInfoFiles=genInfoFiles;
		this.scriptsFolder=scriptsFolder;
	}

	/**
	 * This will be called when PageChangeDetector detects a page change
	 */
	public void reportPageChangedInfo() {
		actionlogger.logAction("# Page Changed", true);
		processShownTell(this.csvFile, this.interactionLogFile);
		// try {
		// Q: How long should we wait ?
		// Answer1 : 4 seconds
		// LOGGER.info("Delaying processing for "+waitTimeInSecondsAfterPageChanged+" seconds to allow page change");
		// Thread.sleep(1000 * waitTimeInSecondsAfterPageChanged );
		// Answer2:
		// As soon as the page is changed, the browser loads new page
		// It takes time to load content
		// Now check time to time, be stable tell that it is changed.

		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

	}

	public void processShownTell() {
		processShownTell(this.csvFile, this.interactionLogFile);
	}

	private void processShownTell(String csvFile, String interactionLogFile) {
		try {

			// 1. Attach jQuery so that we can inject our script
			LOGGER.info("Attaching Scripts (e.g. jQuery) from :"+ this.scriptsFolder);
			try{
				attachJQuery(browser.getWebDriver(),this.scriptsFolder);
			}catch(Exception e){
				LOGGER.info("Failed to attach jQuery. Possible Reason:"+e.getMessage());
				 throw e;
			}

			// Extract page source and info if desired
			if(genInfoFiles){
				LOGGER.info("Saving page Information");
				savePageInformation(browser.getWebDriver());
			}

			// 4. Extract and record forms in the browser.
			// get the forms
			//LOGGER.info("Extracting Forms");
			//forms = FormExtractor.getForms(browser, false);
			// check if page has form tags
			//boolean hasForms = (forms != null && forms.size() > 0) ? true
			//		: false;
			//LOGGER.info("Has forms ?:" + hasForms);

			//if (hasForms) {
			//	LOGGER.info("TOTAL FORMS: " + forms.size());
			//	for (Form form : forms) {
			//		FormExtractor.extractFieldsAndBasicInfoOnly(form);
			//	}
			//}

			// 5. Attach events to listen
			// attachJQueryEvents(browser.getWebDriver());

			// 6. Be ready to log actions
			// 6.1 create an action logger
			actionlogger = new UserActionLogger(interactionLogFile, forms);

			// 6.2 start a server to listen inputs from browser.
			if (server == null) {
				server = new UserActionListenerServer(this.port, actionlogger);
			}
			// 6.3 Now start the page change detector
			detector = new PageChangeDetector(this, browser.getWebDriver(), pageDetectorActivationTime );
			detector.start();

			LOGGER.info("Ready to accept user interactions");

		} catch (Exception e) {
			e.printStackTrace();
			PageChangeDetector.stop = true;
			UserActionListenerServer.closeServer();
			browser.close();
		}

	}

	public static void playUserInteractions(Browser browser,
			String interactionFileName) {
		List<String> lines = Utils.readLines(interactionFileName);
		for (String line : lines) {
			if (line.startsWith("#")) {
				continue;
			} else {
				Map<String, String> parsedOp = InteractionFileParser
						.parseLine(line);
				String id = parsedOp.get("id");
				String name = parsedOp.get("name");
				String type = parsedOp.get("type");
				String value = parsedOp.get("value");

				WebElement we = null;
				if (!id.isEmpty()) {
					we = browser.getWebDriver().findElementById(id);
				} else if (!name.isEmpty()) {
					we = browser.getWebDriver().findElementByName(name);
				}

				if (we != null && type.equalsIgnoreCase("checkbox")
						|| type.equalsIgnoreCase("radio")
						|| type.equalsIgnoreCase("image")
						|| type.equalsIgnoreCase("a")) {
					we.click();
				} else if (we != null && type.equalsIgnoreCase("text")
						|| type.equalsIgnoreCase("textarea")
						|| type.equalsIgnoreCase("select")
						|| type.equalsIgnoreCase("select-one")) {
					we.clear();
					we.sendKeys(value);
				}

			}
		}

	}

	// New code to handle multiple jQueries
	public static void attachJQuery(RemoteWebDriver driver,String scriptDir) throws FileNotFoundException {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		File scriptFolder = new File(scriptDir );	
		InputStream stream = new FileInputStream(scriptFolder.getAbsolutePath()+File.separator+"jquery.min.js");
		String jQuery = Utils.convertStreamToString(stream) + "\n";
		stream = new FileInputStream(scriptFolder.getAbsolutePath()+File.separator+"BRAP.js");
		String brapJS = Utils.convertStreamToString(stream) + "\n";
		jse.executeScript(jQuery + brapJS);
	}

	private void savePageInformation(RemoteWebDriver driver) {
		File dir = new File(this.workingDir);
		List<WebElement> inputs=new ArrayList<WebElement>();
		List<WebElement> buttons=new ArrayList<WebElement>();
		List<WebElement> select=new ArrayList<WebElement>();
		List<WebElement> textarea=new ArrayList<WebElement>();
		List<WebElement> elements=new ArrayList<WebElement>();
		List<WebElement> filteredElements=new ArrayList<WebElement>();

		LOGGER.info("Extracting field types (input, button, select, textarea)");
		try {
			inputs = driver.findElementsByTagName("input");

		} catch (Exception e) {

		}
		try {
			buttons = driver.findElementsByTagName("button");
		} catch (Exception e) {

		}
		try {
			select = driver.findElementsByTagName("select");

		} catch (Exception e) {

		}
		try {
			textarea = driver.findElementsByTagName("textarea");
		} catch (Exception e) {

		}
		
		elements.addAll(inputs);
		elements.addAll(buttons);
		elements.addAll(textarea);
		elements.addAll(select);
		
		LOGGER.info("Extracted total: "+elements.size()+" elements");
		filteredElements=new ArrayList<WebElement>();
		for(WebElement we:elements){
			if(we.isDisplayed()){
				filteredElements.add(we);
			}
		}
		LOGGER.info("Total visible(Displayed) elements: "+filteredElements.size()+". Now getting their attributes.");

		
		StringBuffer sb1 = new StringBuffer();
		
		String header="";
		for(String h:UserActionLogger.HEADERS){
			header+=h+"\t";
		}
		header=header.trim()+"\n";
		sb1.append(header);

		for(WebElement we:filteredElements){
			FormElement fe = new FormElement(we);
			try {
				// "URL","TYPE","ID","NAME","EVENT","VALUE","FORMID","FEGID","FEID","ATTID","OPTIONS","TOP","LEFT","WIDTH","HEIGHT","TAG"
				Map<String,String> parsed = new HashMap<String,String>();
				FormExtractor.extractFormElementInformation(fe);	
				parsed.put("FORMID", "0");
				parsed.put("FEGID", "0");
				parsed.put("FEID", "0");
				parsed.put("URL",driver.getCurrentUrl());		
				parsed.put("TAG",fe.getTagName());				
				parsed.put("TYPE",fe.getType());				
				parsed.put("NAME",fe.getName());				
				parsed.put("ID",fe.getId());
				parsed.put("TOP",fe.getLocation().y+"");
				parsed.put("LEFT",fe.getLocation().x+"");
				parsed.put("HEIGHT",fe.getElement().getSize().height+"");				
				parsed.put("WIDTH",fe.getElement().getSize().width+"");
				parsed.put("ATTID",Utils.getAttribute(fe.getElement(), "attid"));
				String vals = new String();
				if(fe.getType().equalsIgnoreCase("select")){
					List<WebElement> options = we.findElements(By.tagName("option"));
					for (WebElement opt : options) {
						vals +=  opt.getText().replaceAll("\\s+", " ").trim()+";";
						}
				}else{
					vals=fe.getText().replaceAll("\\s+", " ").trim();
				}
				
				parsed.put("VALUE",fe.getSelectedValue());
				parsed.put("OPTIONS",vals);		
				String newFormat="";
				for(String h:UserActionLogger.HEADERS){
					 if(parsed.containsKey(h)){
						 newFormat=newFormat+"\t"+parsed.get(h).trim();
					 }else{
						 newFormat=newFormat+"\t"+" ";
					 }			
				}
				sb1.append(newFormat.trim()+"\n");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Utils.printToFile(dir.getAbsolutePath() + File.separator + "info"
				+ pageNumber, sb1.toString());

		Utils.printToFile(dir.getAbsolutePath() + File.separator + "P"
				+ pageNumber, driver.getPageSource());
		pageNumber++;
	}

}
